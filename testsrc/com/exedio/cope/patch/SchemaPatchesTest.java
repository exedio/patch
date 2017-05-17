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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import com.exedio.cope.MandatoryViolationException;
import com.exedio.cope.StringLengthViolationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

public class SchemaPatchesTest
{
	@Test public void getBody()
	{
		final SchemaPatch patch = patch("one", "two");
		assertEquals(asList("one", "two"), asList(patch.getBody()));
		assertEquals(asList("one", "two"), asList(patch.getBody()));
		assertNotSame(patch.getBody(), patch.getBody());
	}

	@Test public void getBodyNull()
	{
		final SchemaPatch patch = patch((String[])null);
		try
		{
			patch.getBody();
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("body", e.getMessage());
		}
	}

	@Test public void nullBody()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch((String[])null);
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("body", e.getMessage());
		}
	}

	@Test public void emptyBody()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch();
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals("body must not be empty", e.getMessage());
		}
	}

	@SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
	@Test public void nullBodyElement()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", null);
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"body[1]: mandatory violation",
					e.getMessage());
			final MandatoryViolationException cause = (MandatoryViolationException)e.getCause();
			assertEquals(
					"mandatory violation for CopePatchSchemaRun.sql",
					cause.getMessage());
		}
	}

	@SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
	@Test public void emptyBodyElement()
	{
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", "");
		try
		{
			builder.insertAtStart(patch);
			fail();
		}
		catch(final IllegalArgumentException e)
		{
			assertEquals(
					"body[1]: length violation, " +
					"'' is too short, must be at least 1 characters, but was 0.",
					e.getMessage());
			final StringLengthViolationException cause = (StringLengthViolationException)e.getCause();
			assertEquals(
					"length violation, '' is too short for CopePatchSchemaRun.sql, " +
					"must be at least 1 characters, but was 0.",
					cause.getMessage());
		}
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
				assertFalse("gotBody", gotBody);
				gotBody = true;

				return body;
			}
		};
	}
}
