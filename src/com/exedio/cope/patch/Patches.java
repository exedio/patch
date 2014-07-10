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
import com.exedio.cope.TypeSet;
import com.exedio.cope.util.JobContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Patches
{
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final LinkedHashMap<String,Patch> patches;

	// TODO stages
	// TODO cluster-proof lock against concurrent execution

	Patches(final LinkedHashMap<String,Patch> patchesDescending)
	{
		final ArrayList<String> ids = new ArrayList<String>(patchesDescending.keySet());
		Collections.reverse(ids);
		this.patches = new LinkedHashMap<String,Patch>();
		for(final String id : ids)
			patches.put(id, patchesDescending.get(id));
	}

	public void run(final JobContext ctx)
	{
		if(ctx==null)
			throw new NullPointerException("ctx");

		final Model model = PatchRun.TYPE.getModel();
		synchronized(runLock)
		{
			final HashSet<String> idsDone;
			try(TransactionTry tx = model.startTransactionTry("patch query"))
			{
				final List<String> list = new Query<String>(PatchRun.patch).search();
				tx.commit();
				idsDone = new HashSet<String>(list);
			}

			for(final Map.Entry<String, Patch> entry : patches.entrySet())
			{
				final String id = entry.getKey();
				if(idsDone.contains(id))
					continue;

				ctx.stopIfRequested();
				// TODO ctx message
				logger.info("patch {}", id);

				final Patch patch = entry.getValue();
				final boolean isTransactionally = patch.isTransactionally();
				try
				{
					final long start = nanoTime();
					if(isTransactionally)
					{
						model.startTransaction("patch " + id);
						patch.run(ctx);
					}
					else
					{
						patch.run(ctx);
						model.startTransaction("patch " + id + " log");
					}
					new PatchRun(id, isTransactionally, toMillies(nanoTime(), start));
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
				ctx.incrementProgress();
			}
		}
	}

	private final Object runLock = new Object();

	public static final TypeSet types = new TypeSet(PatchRun.TYPE);

	public static Patch stale(final String id)
	{
		PatchRun.patch.check(id);
		return new StalePatch(id);
	}

	private static final class StalePatch implements Patch
	{
		private final String id;

		StalePatch(final String id)
		{
			this.id = id;
		}

		@Override
		public String getID()
		{
			return id;
		}

		@Override
		public boolean isTransactionally()
		{
			return false;
		}

		@Override
		public void run(final JobContext ctx)
		{
			throw new RuntimeException(
					"stale patch " + id + " is supposed to been run already, " +
					"therefore cannot be run again.");
		}

		@Override
		public String toString()
		{
			return id;
		}
	}

	public List<String> getNonStaleIDs()
	{
		final ArrayList<String> result = new ArrayList<String>();
		for(final Map.Entry<String, Patch> entry : patches.entrySet())
			if(!(entry.getValue() instanceof StalePatch))
				result.add(entry.getKey());

		Collections.reverse(result);
		return Collections.unmodifiableList(result);
	}
}
