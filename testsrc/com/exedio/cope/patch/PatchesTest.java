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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.exedio.cope.util.AssertionErrorJobContext;
import java.util.List;
import org.junit.Test;

public class PatchesTest
{
	@Test public void nullPatch()
	{
		final List<SamplePatch> patches = asList(
			newSamplePatch("other1"),
			null,
			newSamplePatch("other2"),
			newSamplePatch("other3"));
		try
		{
			Patches.byDescending(patches);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"null at position 1",
					e.getMessage());
		}
	}

	@Test public void nullID()
	{
		final List<SamplePatch> patches = asList(
			newSamplePatch("other1"),
			newSamplePatch(null),
			newSamplePatch("other2"),
			newSamplePatch("other3"));
		try
		{
			Patches.byDescending(patches);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"illegal id at position 1 " +
					"with class com.exedio.cope.patch.SamplePatch",
					e.getMessage());
			assertEquals(
					"mandatory violation for CopePatchRun.patch",
					e.getCause().getMessage());
		}
	}

	@Test public void emptyID()
	{
		final List<SamplePatch> patches = asList(
			newSamplePatch("other1"),
			newSamplePatch(""),
			newSamplePatch("other2"),
			newSamplePatch("other3"));
		try
		{
			Patches.byDescending(patches);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"illegal id at position 1 " +
					"with class com.exedio.cope.patch.SamplePatch",
					e.getMessage());
			assertEquals(
					"length violation, '' is too short for CopePatchRun.patch, " +
					"must be at least 1 characters, but was 0.",
					e.getCause().getMessage());
		}
	}

	@Test public void duplicateID()
	{
		final List<SamplePatch> patches = asList(
			newSamplePatch("other1"),
			newSamplePatch("duplicate"),
			newSamplePatch("duplicate"),
			newSamplePatch("other2"),
			newSamplePatch("other3"));
		try
		{
			Patches.byDescending(patches);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"duplicate id >duplicate< " +
					"at position 2 " +
					"with class com.exedio.cope.patch.SamplePatch",
					e.getMessage());
		}
	}

	@Test public void stale()
	{
		final Patch patch = Patches.stale("staleID");
		assertEquals("staleID", patch.getID());
		assertEquals(false, patch.isTransactionally());
		try
		{
			patch.run(new AssertionErrorJobContext());
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals(
					"stale patch staleID is supposed to been run already, therefore cannot be run again.",
					e.getMessage());
		}
	}

	private SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(null, id, true);
	}
}
