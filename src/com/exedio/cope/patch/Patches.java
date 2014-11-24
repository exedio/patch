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

import com.exedio.cope.TransactionTry;
import com.exedio.cope.TypeSet;
import com.exedio.cope.util.JobContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Patches
{
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final TreeMap<Integer,Stage> stages;

	Patches(final LinkedHashMap<String,Patch> patchesDescending)
	{
		final ArrayList<String> ids = new ArrayList<>(patchesDescending.keySet());
		Collections.reverse(ids);
		this.stages = new TreeMap<>();
		for(final String id : ids)
		{
			final Patch patch = patchesDescending.get(id);
			final int stageNumber = patch.getStage();
			Stage stage = stages.get(stageNumber);
			if(stage==null)
			{
				stage = new Stage(stageNumber);
				stages.put(stageNumber, stage);
			}
			stage.put(id, patch);
		}
	}

	public void run(final JobContext ctx)
	{
		if(ctx==null)
			throw new NullPointerException("ctx");

		for(final Map.Entry<Integer,Stage> entry : stages.entrySet())
		{
			entry.getValue().run(ctx);
		}
	}

	/**
	 * Marks all patches as run, without actually running them.
	 * Is useful when you
	 * {@link com.exedio.cope.Model#createSchema() created}
	 * an empty schema.
	 */
	public void preempt()
	{
		logger.info("preempt");
		try(TransactionTry tx = PatchRun.TYPE.getModel().startTransactionTry("preempt"))
		{
			for(final Map.Entry<Integer,Stage> entry : stages.entrySet())
				entry.getValue().preempt();
			tx.commit();
		}
	}


	public static final TypeSet types = new TypeSet(PatchRun.TYPE, PatchMutex.TYPE, SchemaPatchRun.TYPE);

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
		public int getStage()
		{
			return Integer.MIN_VALUE;
		}

		@Override
		public void check()
		{
			// empty
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

	public List<String> getIDs()
	{
		return getIDs(false);
	}

	public List<String> getNonStaleIDs()
	{
		return getIDs(true);
	}

	private List<String> getIDs(final boolean staleOnly)
	{
		final ArrayList<String> result = new ArrayList<>();
		for(final Stage stage : stages.values())
			for(final Map.Entry<String, Patch> entry : stage.getPatches().entrySet())
				if(!staleOnly || !(entry.getValue() instanceof StalePatch))
					result.add(entry.getKey());

		Collections.reverse(result);
		return Collections.unmodifiableList(result);
	}
}
