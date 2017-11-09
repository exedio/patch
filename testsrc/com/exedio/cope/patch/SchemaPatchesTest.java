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
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.exedio.cope.MandatoryViolationException;
import com.exedio.cope.StringLengthViolationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

public class SchemaPatchesTest
{
	@Test void getBody()
	{
		final SchemaPatch patch = patch("one", "two");
		assertEquals(asList("one", "two"), asList(patch.getBody()));
		assertEquals(asList("one", "two"), asList(patch.getBody()));
		assertNotSame(patch.getBody(), patch.getBody());
	}

	@Test void getBodyNull()
	{
		final SchemaPatch patch = patch((String[])null);
		assertFails(() ->
			patch.getBody(),
			NullPointerException.class, "body");
	}

	@Test void nullBody()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch((String[])null);
		assertFails(() ->
			builder.insertAtStart(patch),
			NullPointerException.class, "body");
	}

	@Test void emptyBody()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch();
		assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException.class,
			"body must not be empty");
	}

	@SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
	@Test void nullBodyElement()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", null);
		final Exception e = assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException.class,
			"body[1]: mandatory violation",
			MandatoryViolationException.class);
		assertEquals(
				"mandatory violation for CopePatchSchemaRun.sql",
				e.getCause().getMessage());
	}

	@SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
	@Test void emptyBodyElement()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", "");
		final Exception e = assertFails(() ->
			builder.insertAtStart(patch),
			IllegalArgumentException.class,
			"body[1]: length violation, " +
			"'' is too short, must be at least 1 characters, but was 0.",
			StringLengthViolationException.class);
		assertEquals(
				"length violation, '' is too short for CopePatchSchemaRun.sql, " +
				"must be at least 1 characters, but was 0.",
				e.getCause().getMessage());
	}

	private static SchemaPatch patch(final String... body)
	{
		return new SchemaPatch()
		{
			boolean gotBody = false;

			@Override
			public String getID()
			{
				return "id";
			}

			@Override
			public int getStage()
			{
				return 0;
			}

			@Override
			protected String[] computeBody()
			{
				assertFalse(gotBody, "gotBody");
				gotBody = true;

				return body;
			}
		};
	}
}
