/*
 * Copyright (C) 2004-2013  exedio GmbH (www.exedio.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.exedio.cope.patch;

import static com.exedio.cope.misc.TimeUtil.toMillies;
import static java.lang.System.nanoTime;
import static java.util.Objects.requireNonNull;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Stage
{
	@SuppressWarnings("LoggerInitializedWithForeignClass")
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final int stageNumber;
	private final String txName;
	private final LinkedHashMap<String,Patch> patches;

	Stage(final int stageNumber)
	{
		this.stageNumber = stageNumber;
		this.txName = "patch stage " + stageNumber + ' ';
		this.patches = new LinkedHashMap<>();
	}

	void put(final String id, final Patch patch)
	{
		patches.put(id, patch);
	}

	int run(final JobContext ctx)
	{
		final Model model = PatchRun.TYPE.getModel();

		runLock.lock(); // blocks until available
		try
		{
			final LinkedHashMap<String,Patch> patches = getPatchesPending();
			if(patches.isEmpty())
				return 0;

			ctx.stopIfRequested();
			final String host = getHost();
			final String savepoint = getSavepoint(model);
			final int numberOfPatches = patches.size();
			final PatchMutex mutex = seizeMutex(host, savepoint, numberOfPatches);

			int numberOfPatch = 1;
			int result = 0;
			for(final Map.Entry<String, Patch> entry : patches.entrySet())
			{
				final String id = entry.getKey();

				ctx.stopIfRequested();
				logger.info("s{} run {}/{} {}", new Object[]{stageNumber, numberOfPatch, numberOfPatches, id});
				if(ctx.supportsMessage())
					ctx.setMessage("run s" + stageNumber + ' ' + numberOfPatch + '/' + numberOfPatches + ' ' + id);
				numberOfPatch++;

				final Patch patch = entry.getValue();
				final boolean isTransactionally = patch.isTransactionally();
				try
				{
					final long start = nanoTime();
					if(isTransactionally)
					{
						model.startTransaction(txName + id);
						patch.run(ctx);
					}
					else
					{
						patch.run(ctx);
						model.startTransaction(txName + id + " log");
					}
					//noinspection ResultOfObjectAllocationIgnored persistent object
					new PatchRun(id, stageNumber, isTransactionally, host, savepoint, toMillies(nanoTime(), start));
					model.commit();
					result++;
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
				ctx.incrementProgress();
			}

			releaseMutex(mutex);
			return result;
		}
		finally
		{
			runLock.unlock();
		}
	}

	private PatchMutex seizeMutex(
			final String host,
			final String savepoint,
			final int numberOfPatches)
	{
		logger.info("s{} mutex seize for {} patches", stageNumber, numberOfPatches);
		try(TransactionTry tx = startTransaction("mutex seize"))
		{
			return tx.commit(
					new PatchMutex(stageNumber, host, savepoint, numberOfPatches));
		}
	}

	private void releaseMutex(final PatchMutex mutex)
	{
		logger.info("s{} mutex release", stageNumber);
		try(TransactionTry tx = startTransaction("mutex release"))
		{
			mutex.deleteCopeItem();
			tx.commit();
		}
	}

	DoneResult getDone()
	{
		final boolean lockAcquired = runLock.tryLock(); // returns immediately
		if(!lockAcquired)
			return DoneResult.RUNNING;

		final LinkedHashMap<String,Patch> pending;
		try
		{
			pending = getPatchesPending();
		}
		finally
		{
			runLock.unlock();
		}
		return
				pending.isEmpty()
				? DoneResult.DONE
				: DoneResult.PENDING;
	}

	private LinkedHashMap<String,Patch> getPatchesPending()
	{
		final LinkedHashMap<String,Patch> result = new LinkedHashMap<>(patches);

		try(TransactionTry tx = startTransaction("query"))
		{
			final List<String> idsDone = new Query<>(PatchRun.patch).search();
			tx.commit();
			result.keySet().removeAll(idsDone);
		}
		return result;
	}

	private final ReentrantLock runLock = new ReentrantLock();

	private TransactionTry startTransaction(final String name)
	{
		return PatchRun.TYPE.getModel().startTransactionTry(txName + name);
	}

	private static String getHost()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch(final UnknownHostException ignored)
		{
			return "<UnknownHostException>";
		}
	}

	private String getSavepoint(final Model model)
	{
		final String result;
		try
		{
			result = model.getSchemaSavepoint();
		}
		catch(final SQLException e)
		{
			logger.error("savepoint", e);
			return "FAILURE: " + e.getMessage();
		}
		logger.info("s{} savepoint {}", stageNumber, result);
		return result;
	}

	void preempt()
	{
		final LinkedHashMap<String,Patch> patches = getPatchesPending();
		if(patches.isEmpty())
			return;

		final String host = getHost();
		final int numberOfPatches = patches.size();
		final PatchMutex mutex = seizeMutex(host, null, numberOfPatches);

		try(TransactionTry tx = startTransaction("preempt"))
		{
			for(final Map.Entry<String, Patch> entry : patches.entrySet())
			{
				final Patch patch = entry.getValue();
				//noinspection ResultOfObjectAllocationIgnored persistent object
				new PatchRun(entry.getKey(), stageNumber, patch.isTransactionally(), host);
			}
			tx.commit();
		}

		releaseMutex(mutex);
	}

	boolean preempt(final String patchId)
	{
		final Patch patch = patches.get(patchId);
		requireNonNull(patch); // cannot happen, is tested by calling code

		final boolean patchRunExists;
		try (TransactionTry tx = startTransaction("query"))
		{
			patchRunExists = PatchRun.forPatch(patchId) != null;
			tx.commit();
		}

		if (patchRunExists)
			return false;

		final String host = getHost();
		final PatchMutex mutex = seizeMutex(host, null, 1);

		try (TransactionTry tx = startTransaction("preempt"))
		{
			//noinspection ResultOfObjectAllocationIgnored persistent object
			new PatchRun(patchId, stageNumber, patch.isTransactionally(), host);
			tx.commit();
		}

		releaseMutex(mutex);

		return true;
	}

	Map<String,Patch> getPatches()
	{
		return Collections.unmodifiableMap(patches);
	}
}
