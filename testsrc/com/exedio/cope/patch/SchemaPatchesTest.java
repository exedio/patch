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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.exedio.cope.ActivationParameters;
import com.exedio.cope.Item;
import com.exedio.cope.MandatoryViolationException;
import com.exedio.cope.Model;
import com.exedio.cope.Revisions;
import com.exedio.cope.StringField;
import com.exedio.cope.StringLengthViolationException;
import com.exedio.cope.Type;
import com.exedio.cope.TypesBound;
import org.junit.Test;

public class SchemaPatchesTest
{
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
		return new SchemaPatch(MODEL)
		{
			boolean gotBody = false;

			@Override
			public String getID()
			{
				return "id";
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

	static final class AnItem extends Item
	{
		static final StringField patch = new StringField().toFinal().lengthRange(0, 1000);
		private static final long serialVersionUID = 1l;
		static final Type<AnItem> TYPE = TypesBound.newType(AnItem.class);
		@SuppressWarnings("unused") private AnItem(final ActivationParameters ap){super(ap);}
	}

	static final Model MODEL = new Model((Revisions.Factory)null, AnItem.TYPE);
}
