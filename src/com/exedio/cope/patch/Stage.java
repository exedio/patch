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

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Stage
{
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final int stageNumber;
	private final LinkedHashMap<String,Patch> patches;

	Stage(final int stageNumber)
	{
		this.stageNumber = stageNumber;
		this.patches = new LinkedHashMap<>();
	}

	public void put(final String id, final Patch patch)
	{
		patches.put(id, patch);
	}

	void run(final JobContext ctx)
	{
		final Model model = PatchRun.TYPE.getModel();
		final String txName = "patch stage " + stageNumber + ' ';

		synchronized(runLock)
		{
			final LinkedHashMap<String,Patch> patches = new LinkedHashMap<>(this.patches);

			try(TransactionTry tx = model.startTransactionTry(txName + "query"))
			{
				final List<String> idsDone = new Query<>(PatchRun.patch).search();
				tx.commit();
				patches.keySet().removeAll(idsDone);
			}
			if(patches.isEmpty())
				return;

			ctx.stopIfRequested();
			final String savepoint = getSavepoint(model);

			final int numberOfPatches = patches.size();
			logger.info("s{} mutex seize for {} patches", stageNumber, numberOfPatches);
			final PatchMutex mutex;
			try(TransactionTry tx = model.startTransactionTry(txName + "mutex seize"))
			{
				mutex = new PatchMutex(stageNumber, savepoint, numberOfPatches);
				tx.commit();
			}

			int numberOfPatch = 1;
			for(final Map.Entry<String, Patch> entry : patches.entrySet())
			{
				final String id = entry.getKey();

				ctx.stopIfRequested();
				logger.info("s{} run {}/{} {}", new Object[]{stageNumber, numberOfPatch++, numberOfPatches, id});
				if(ctx.supportsMessage())
					ctx.setMessage("run s" + stageNumber + ' ' + id);

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
					new PatchRun(id, stageNumber, isTransactionally, savepoint, toMillies(nanoTime(), start));
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
				ctx.incrementProgress();
			}

			logger.info("s{} mutex release", stageNumber);
			try(TransactionTry tx = model.startTransactionTry(txName + "mutex release"))
			{
				mutex.deleteCopeItem();
				tx.commit();
			}
		}
	}

	private final Object runLock = new Object();

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
		for(final Map.Entry<String, Patch> entry : patches.entrySet())
		{
			final Patch patch = entry.getValue();
			new PatchRun(entry.getKey(), stageNumber, patch.isTransactionally());
		}
	}


	Map<String,Patch> getPatches()
	{
		return Collections.unmodifiableMap(patches);
	}
}
