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

import com.exedio.cope.TypeSet;
import com.exedio.cope.util.JobContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Patches
{
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

	@SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
	public void run(final JobContext ctx)
	{
		if(ctx==null)
			throw new NullPointerException("ctx");

		Envelope envelope = null;

		for(final Map.Entry<Integer,Stage> entry : stages.entrySet())
		{
			if(envelope==null)
				envelope = entry.getValue().run(envelope, ctx);
		}

		if(envelope!=null)
			envelope.close();
	}


	public static final TypeSet types = new TypeSet(PatchRun.TYPE, PatchMutex.TYPE);

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

	public List<String> getNonStaleIDs()
	{
		final ArrayList<String> result = new ArrayList<>();
		for(final Stage stage : stages.values())
			for(final Map.Entry<String, Patch> entry : stage.getPatches().entrySet())
				if(!(entry.getValue() instanceof StalePatch))
					result.add(entry.getKey());

		Collections.reverse(result);
		return Collections.unmodifiableList(result);
	}
}
