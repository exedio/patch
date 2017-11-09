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

import static com.exedio.cope.junit.Assert.assertFails;
import static com.exedio.cope.junit.CopeAssert.assertEqualsUnmodifiable;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.exedio.cope.MandatoryViolationException;
import com.exedio.cope.StringCharSetViolationException;
import com.exedio.cope.StringLengthViolationException;
import com.exedio.cope.util.AssertionErrorJobContext;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PatchesTest
{
	@Test void patchNull()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		assertFails(() ->
			builder.insertAtStart(null),
			NullPointerException.class, "patch");
	}

	@Test void idNull()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch(null);
		assertFails(() ->
			builder.insertAtStart(patch),
			MandatoryViolationException.class,
			"mandatory violation for CopePatchRun.patch");
	}

	@Test void idEmpty()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch("");
		assertFails(() ->
			builder.insertAtStart(patch),
			StringLengthViolationException.class,
			"length violation, '' is too short for CopePatchRun.patch, " +
			"must be at least 1 characters, but was 0.");
	}

	@Test void idCharset()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch("01234\t6789");
		assertFails(() ->
			builder.insertAtStart(patch),
			StringCharSetViolationException.class,
			"character set violation, '01234\t6789' for CopePatchRun.patch, " +
			"contains forbidden character '\t' on position 5.");
	}

	@Test void idTrimmedStart()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch(" 123456789");
		assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException .class,
			"id > 123456789< is not trimmed");
	}

	@Test void idTrimmedEnd()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatch("123456789 ");
		assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException.class,
			"id >123456789 < is not trimmed");
	}

	@Test void check()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final Patch patch = newSamplePatchCheck("id");
		assertFails(() ->
			builder.insertAtStart(patch),
			RuntimeException.class,
			"check exception message");
	}

	@Test void idDuplicate()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("duplicate"));
		final Patch patch = newSamplePatch("duplicate");
		assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException.class,
			"duplicate id >duplicate< " +
			"with class com.exedio.cope.patch.SamplePatch");
	}

	@Test void exhausted()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.build();
		assertFails(() ->
			builder.insertAtStart(newSamplePatch("id")),
			IllegalStateException.class,
			"builder is exhausted");
		assertFails(() ->
			builder.build(),
			IllegalStateException.class,
			"builder is exhausted");
	}

	@Test void stale()
	{
		final Patch patch = Patches.stale("staleID");
		assertEquals("staleID", patch.getID());
		assertEquals(false, patch.isTransactionally());
		assertFails(() ->
			patch.run(new AssertionErrorJobContext()),
			RuntimeException.class,
			"stale patch >staleID< is supposed to been run already, therefore cannot be run again.");
	}

	@Test void staleError()
	{
		assertFails(() ->
			Patches.stale(""),
			StringLengthViolationException.class,
			"length violation, '' is too short for CopePatchRun.patch, must be at least 1 characters, but was 0.");
	}

	@Test void getIDs()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("other1"));
		builder.insertAtStart(Patches.stale ("stale1"));
		builder.insertAtStart(newSamplePatch("other2"));
		builder.insertAtStart(Patches.stale ("stale2"));
		builder.insertAtStart(newSamplePatch("other3"));
		final Patches patches = builder.build();

		assertEqualsUnmodifiable(
				asList("other1", "other2", "other3", "stale1", "stale2"),
				patches.getIDs());
		assertEqualsUnmodifiable(
				asList("other1", "other2", "other3"),
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

	@Test void insertStaleFromResourceNotFound() throws IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		assertFails(() ->
			builder.insertStaleFromResource(Object.class),
			IllegalArgumentException.class,
			"class java.lang.Object does not find stale-patch-ids.txt");
	}

	@Test void insertStaleFromResourceNull() throws IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		assertFails(() ->
			builder.insertStaleFromResource(null),
			NullPointerException.class, null);
	}
}
