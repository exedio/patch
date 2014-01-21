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
import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.Revisions;
import com.exedio.cope.TypeSet;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.AssertionErrorJobContext;
import com.exedio.cope.util.EmptyJobContext;
import com.exedio.cope.util.JobContext;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class PatchTest extends CopeModel4Test
{
	static final Model MODEL = new Model(
			(Revisions)null,
			new TypeSet[]{Patches.types},
			SampleItem.TYPE
	);

	public PatchTest()
	{
		super(MODEL);
	}

	@Test public void one()
	{
		assertEquals(EMPTY_LIST, items());
		final Patches patches = Patches.byDescending(asList(
				newSamplePatch("one")
		));
		run(patches, new EmptyJobContext());
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch one", items.next());
			assertFalse(items.hasNext());
		}
		run(patches, new EmptyJobContext());
		assertEquals(asList(one), items());
	}

	@Test public void two()
	{
		assertEquals(EMPTY_LIST, items());
		final JC ctx = new JC();
		final Patches patches = Patches.byDescending(asList(
				newSamplePatch("two"),
				newSamplePatch("one")
		));
		run(patches, ctx);
		ctx.assertIt("requestedToStop()"+"requestedToStop()");
		final SampleItem one;
		final SampleItem two;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch one", items.next());
			two = assertIt("two", "patch two", items.next());
			assertFalse(items.hasNext());
		}
		run(patches, ctx);
		ctx.assertIt("requestedToStop()"+"requestedToStop()");
		assertEquals(asList(one, two), items());
		final Patches patches2 = Patches.byDescending(asList(
				newSamplePatch("three"),
				newSamplePatch("two"),
				newSamplePatch("one")
		));
		run(patches2, ctx);
		ctx.assertIt("requestedToStop()"+"requestedToStop()"+"requestedToStop()");
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertEquals(two, items.next());
			assertIt("three", "patch three", items.next());
			assertFalse(items.hasNext());
		}
		ctx.assertIt("");
	}

	@Test public void failure()
	{
		assertEquals(EMPTY_LIST, items());
		final Patches patches = Patches.byDescending(asList(
			newSamplePatch("fail"),
			newSamplePatch("ok")));
		try
		{
			run(patches, new EmptyJobContext());
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("failed", e.getMessage());
		}
		final SampleItem ok;
		{
			final Iterator<SampleItem> items = items().iterator();
			ok = assertIt("ok", "patch ok", items.next());
			assertFalse(items.hasNext());
		}
		try
		{
			run(patches, new EmptyJobContext());
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("failed", e.getMessage());
		}
		assertEquals(asList(ok), items());
	}

	@Test public void nullPatch()
	{
		assertEquals(EMPTY_LIST, items());
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
		assertEquals(EMPTY_LIST, items());
	}

	@Test public void nullID()
	{
		assertEquals(EMPTY_LIST, items());
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
		assertEquals(EMPTY_LIST, items());
	}

	@Test public void emptyID()
	{
		assertEquals(EMPTY_LIST, items());
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
		assertEquals(EMPTY_LIST, items());
	}

	@Test public void duplicateID()
	{
		assertEquals(EMPTY_LIST, items());
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
		assertEquals(EMPTY_LIST, items());
	}

	@Test public void stale()
	{
		final String id = "staleID";

		assertEquals(EMPTY_LIST, items());
		run(Patches.byDescending(asList(newSamplePatch(id))), new EmptyJobContext());
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt(id, "patch " + id, items.next());
			assertFalse(items.hasNext());
		}

		run(Patches.byDescending(asList(Patches.stale(id))), new EmptyJobContext());
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertFalse(items.hasNext());
		}
	}

	@Test public void staleError()
	{
		assertEquals(EMPTY_LIST, items());
		final Patches patches = Patches.byDescending(asList(Patches.stale("staleID")));
		try
		{
			run(patches, new EmptyJobContext());
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals(
					"stale patch staleID is supposed to been run already, " +
					"therefore cannot be run again. ",
					e.getMessage());
		}
		assertEquals(EMPTY_LIST, items());
	}

	private static void run(
			final Patches patches,
			final JobContext ctx)
	{
		MODEL.commit();
		try
		{
			patches.run(ctx);
		}
		finally
		{
			MODEL.startTransaction(PatchTest.class.getName());
		}
	}

	private SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id);
	}

	private List<SampleItem> items()
	{
		final Query<SampleItem> q = SampleItem.TYPE.newQuery();
		q.setOrderBy(SampleItem.number, true);
		return q.search();
	}

	private SampleItem assertIt(
			final String id,
			final String transactionName,
			final SampleItem actual)
	{
		assertEquals("id", id, actual.getPatch());
		assertEquals("transactionName", transactionName, actual.getTransactionName());
		return actual;
	}

	static class JC extends AssertionErrorJobContext
	{
		private final StringBuilder actual = new StringBuilder();

		@Override
		public boolean requestedToStop()
		{
			actual.append("requestedToStop()");
			return false;
		}

		void assertIt(final String expected)
		{
			assertEquals(expected, actual.toString());
			actual.setLength(0);
		}
	}
}
