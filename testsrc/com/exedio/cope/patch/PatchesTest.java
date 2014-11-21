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

import static com.exedio.cope.junit.CopeAssert.assertEqualsUnmodifiable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.exedio.cope.MandatoryViolationException;
import com.exedio.cope.StringLengthViolationException;
import com.exedio.cope.util.AssertionErrorJobContext;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public class PatchesTest
{
	@Test public void nullPatch()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		try
		{
			builder.insertAtStart(null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("patch", e.getMessage());
		}
	}

	@Test public void nullID()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch(null);
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final MandatoryViolationException e)
		{
			assertEquals(
					"mandatory violation for CopePatchRun.patch",
					e.getMessage());
		}
	}

	@Test public void emptyID()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch("");
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final StringLengthViolationException e)
		{
			assertEquals(
					"length violation, '' is too short for CopePatchRun.patch, " +
					"must be at least 1 characters, but was 0.",
					e.getMessage());
		}
	}

	@Test public void check()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatchCheck("id");
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("check exception message", e.getMessage());
		}
	}

	@Test public void duplicateID()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("duplicate"));
		final Patch patch = newSamplePatch("duplicate");
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"duplicate id >duplicate< " +
					"with class com.exedio.cope.patch.SamplePatch",
					e.getMessage());
		}
	}

	@Test public void exhausted()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.build();
		try
		{
			builder.insertAtStart(newSamplePatch("id"));
			fail();
		}
		catch(final IllegalStateException e)
		{
			assertEquals("builder is exhausted", e.getMessage());
		}
		try
		{
			builder.build();
			fail();
		}
		catch(final IllegalStateException e)
		{
			assertEquals("builder is exhausted", e.getMessage());
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

	@Test public void staleError()
	{
		try
		{
			Patches.stale("");
			fail();
		}
		catch(final StringLengthViolationException e)
		{
			assertEquals(
					"length violation, '' is too short for CopePatchRun.patch, must be at least 1 characters, but was 0.",
					e.getMessage());
		}
	}

	@Test public void getNonStaleIDs()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("other1"));
		builder.insertAtStart(Patches.stale ("stale1"));
		builder.insertAtStart(newSamplePatch("other2"));
		builder.insertAtStart(Patches.stale ("stale2"));
		builder.insertAtStart(newSamplePatch("other3"));
		final Patches patches = builder.build();

		assertEqualsUnmodifiable(
				Arrays.asList("other1", "other2", "other3"),
				patches.getNonStaleIDs());
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(null, id, null, true);
	}

	private static SamplePatch newSamplePatchCheck(final String id)
	{
		return new SamplePatch(null, id, "check exception message", true);
	}

	@Test public void insertStaleFromResourceNotFound() throws IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		try
		{
			builder.insertStaleFromResource(Object.class);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("does not exist: stale-patch-ids.txt", e.getMessage());
		}
	}
}
