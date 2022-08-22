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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public final class PatchesBuilder
{
	private LinkedHashMap<String,Patch> patches = new LinkedHashMap<>();
	private PatchesDoneListener doneListener = null;

	public void insertAtStart(final Patch patch)
	{
		requireNonNull(patch, "patch");

		// NOTE
		// this should be the only place in production code, where getID is called.
		final String id = patch.getID();
		PatchRun.patch.check(id);
		if(!id.equals(id.trim()))
			throw new IllegalArgumentException("id >" + id + "< is not trimmed");
		patch.check();

		assertNotExhausted();

		if(patches.containsKey(id))
			throw new IllegalArgumentException(
					"duplicate id >" + id + "< " +
					"with class " + patch.getClass().getName());

		patches.put(id, patch);
	}

	public Patches build()
	{
		assertNotExhausted();
		final Patches result = new Patches(patches, doneListener);
		patches = null;
		return result;
	}

	private void assertNotExhausted()
	{
		if(patches==null)
			throw new IllegalStateException("builder is exhausted");
	}

	/**
	 *@see PatchesDoneListener#notifyPatchesDone()
	 */
	public PatchesBuilder withDoneListener(final PatchesDoneListener listener)
	{
		requireNonNull(listener);
		if (doneListener != null)
			throw new IllegalArgumentException("a doneListener >"+doneListener+"< is already set");

		this.doneListener = listener;
		return this;
	}

	/**
	 * Inserts stale patches with {@link Patch#getID() ids} taken from
	 * class resource stale-patch-ids.txt.
	 * This file is typically the result of
	 * <p>
	 * {@code select patch from CopePatchRun order by this desc}
	 * <p>
	 * or follow the link "Stale Ids" in Patch Console.
	 *
	 * @see #insertStaleFromResource(Class)
	 */
	public PatchesBuilder withStaleFromResource(final Class<?> clazz)
	{
		try
		{
			insertStaleFromResource(clazz);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Inserts stale patches with {@link Patch#getID() ids} taken from
	 * class resource stale-patch-ids.txt.
	 * This file is typically the result of
	 * <p>
	 * {@code select patch from CopePatchRun order by this desc}
	 * <p>
	 * or follow the link "Stale Ids" in Patch Console.
	 *
	 * @see #withStaleFromResource(Class)
	 */
	public void insertStaleFromResource(final Class<?> clazz) throws IOException
	{
		final String name = "stale-patch-ids.txt";
		try(InputStream stream = clazz.getResourceAsStream(name))
		{
			if(stream==null)
				throw new IllegalArgumentException("" + clazz + " does not find " + name);

			try(
				InputStreamReader reader = new InputStreamReader(stream, UTF_8);
				BufferedReader bufferedReader = new BufferedReader(reader))
			{
				String line;
				//noinspection NestedAssignment
				while( (line = bufferedReader.readLine()) != null )
				{
					insertAtStart(Patches.stale(line));
				}
			}
		}
	}
}
