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
			stages.computeIfAbsent(patch.getStage(), Stage::new).put(id, patch);
		}
	}

	public int run(final JobContext ctx)
	{
		if(ctx==null)
			throw new NullPointerException("ctx");

		int result = 0;
		for(final Stage stage : stages.values())
		{
			result += stage.run(ctx);
		}

		if(result>0)
			logger.info("run finished after {} patches", result);

		return result;
	}

	/**
	 * If this method returns true, it is definitely known,
	 * that there are no pending patches.
	 * This means, {@link #run(JobContext) run} has nothing to do.
	 * <p>
	 * If this method returns false, nothing is known.
	 * In particular one on the following conditions may be true:
	 * <ul>
	 * <li>There is at least one pending patch.</li>
	 * <li>Method {@link #run(JobContext) run} is currently executed by another thread.</li>
	 * <li>This method {@code isDone} is currently executed by another thread.</li>
	 * <li>The model containing the {@link Patches#types types} of the patch framework
	 *     is not yet {@link com.exedio.cope.Model#isConnected() connected}.</li>
	 * </ul>
	 * This implies, that the result is not monotonous:
	 * After this method returned true, subsequent calls may return false.
	 */
	public boolean isDone()
	{
		// TODO there is no test for being not connected
		if(!PatchRun.TYPE.getModel().isConnected())
			return false;

		for(final Stage stage : stages.values())
		{
			if(!stage.isDone())
				return false;
		}

		return true;
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
		for(final Stage stage : stages.values())
			stage.preempt();
	}


	public static final TypeSet types = new TypeSet(PatchRun.TYPE, PatchMutex.TYPE, SchemaPatchRun.TYPE);

	public static Patch stale(final String id)
	{
		PatchRun.patch.check(id);
		return new StalePatch(id);
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
