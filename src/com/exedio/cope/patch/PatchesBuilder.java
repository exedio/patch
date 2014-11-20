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

import com.exedio.cope.util.CharsetName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public final class PatchesBuilder
{
	private LinkedHashMap<String,Patch> patches = new LinkedHashMap<>();

	public void insertAtStart(final Patch patch)
	{
		if(patch==null)
			throw new NullPointerException("patch");

		// NOTE
		// this should be the only place in production code, where getID is called.
		final String id = patch.getID();
		PatchRun.patch.check(id);
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
		final Patches result = new Patches(patches);
		patches = null;
		return result;
	}

	private void assertNotExhausted()
	{
		if(patches==null)
			throw new IllegalStateException("builder is exhausted");
	}

	public void insertStaleFromResource(final Class<?> clazz) throws IOException
	{
		final String name = "stale-patch-ids.txt";
		try(final InputStream stream = clazz.getResourceAsStream(name))
		{
			if(stream==null)
				throw new IllegalArgumentException("does not exist: " + name);

			try(
				final InputStreamReader reader = new InputStreamReader(stream, CharsetName.UTF8);
				final BufferedReader r = new BufferedReader(reader))
			{
				for(String line = r.readLine(); line!=null; line = r.readLine())
				{
					insertAtStart(Patches.stale(line));
				}
			}
		}
	}
}
