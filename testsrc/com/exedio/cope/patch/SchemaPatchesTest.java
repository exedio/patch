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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.exedio.cope.StringLengthViolationException;
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

	@Test void nullBody()
	{
		assertFails(() ->
			patch((String[])null),
			NullPointerException.class, "body");
	}

	@Test void emptyBody()
	{
		//noinspection Convert2MethodRef
		assertFails(() ->
			patch(),
			IllegalArgumentException.class,
			"body must not be empty");
	}

	@Test void nullBodyElement()
	{
		assertFails(() ->
			patch("one", null),
			NullPointerException.class,
			"body[1]");
	}

	@Test void emptyBodyElement()
	{
		assertFails(() ->
			patch("one", ""),
			IllegalArgumentException.class,
			"body[1] must not be empty");
	}

	@Test void longBodyElement()
	{
		final String secondBody = "X".repeat(10*1000*1000 + 1);
		final String valueTruncated =
				"'" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ... " +
				"XXXXXXXXXXXXXXXXXXXX'" +
				" (truncated, was 10000001 characters)";
		final Exception e = assertFails(() ->
			patch("one", secondBody),
			IllegalArgumentException.class,
			"body[1]: length violation, " +
			valueTruncated + " is too long, " +
			"must be at most 10000000 characters, " +
			"but was 10000001",
			StringLengthViolationException.class);
		assertEquals(
				"length violation, " +
				valueTruncated + " is too long for CopePatchSchemaRun.sql, " +
				"must be at most 10000000 characters, " +
				"but was 10000001",
				e.getCause().getMessage());
	}

	private static SchemaPatch patch(final String... body)
	{
		return new SchemaPatch(body)
		{
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
		};
	}
}
