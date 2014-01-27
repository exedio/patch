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
			(Revisions.Factory)null,
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
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
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

	@Test public void oneNonTx()
	{
		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("one"));
		final Patches patches = builder.build();
		run(patches, new EmptyJobContext());
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", null, items.next());
			assertFalse(items.hasNext());
		}
		run(patches, new EmptyJobContext());
		assertEquals(asList(one), items());
	}

	@Test public void two()
	{
		assertEquals(EMPTY_LIST, items());
		final JC ctx = new JC();
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("two"));
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		run(patches, ctx);
		ctx.assertIt("requestedToStop()"+"incrementProgress()"+"requestedToStop()"+"incrementProgress()");
		final SampleItem one;
		final SampleItem two;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch one", items.next());
			two = assertIt("two", "patch two", items.next());
			assertFalse(items.hasNext());
		}
		run(patches, ctx);
		ctx.assertIt("");
		assertEquals(asList(one, two), items());
		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(newSamplePatch("three"));
		builder2.insertAtStart(newSamplePatch("two"));
		builder2.insertAtStart(newSamplePatch("one"));
		final Patches patches2 = builder2.build();
		run(patches2, ctx);
		ctx.assertIt("requestedToStop()"+"incrementProgress()");
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
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("fail"));
		builder.insertAtStart(newSamplePatch("ok"));
		final Patches patches = builder.build();
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

	@Test public void failureNonTx()
	{
		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("fail"));
		builder.insertAtStart(newSamplePatchNonTx("ok"));
		final Patches patches = builder.build();
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
		final SampleItem fail1;
		{
			final Iterator<SampleItem> items = items().iterator();
			ok = assertIt("ok", null, items.next());
			fail1 = assertIt("fail", null, items.next());
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
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(ok, items.next());
			assertEquals(fail1, items.next());
			assertFalse(fail1.equals(assertIt("fail", null, items.next())));
			assertFalse(items.hasNext());
		}
	}

	@Test public void stale()
	{
		final String id = "staleID";

		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch(id));
		final Patches patches = builder.build();
		run(patches, new EmptyJobContext());
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt(id, "patch " + id, items.next());
			assertFalse(items.hasNext());
		}

		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(Patches.stale(id));
		final Patches patches2 = builder2.build();
		run(patches2, new EmptyJobContext());
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertFalse(items.hasNext());
		}
	}

	@Test public void staleError()
	{
		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(Patches.stale("staleID"));
		final Patches patches = builder.build();
		try
		{
			run(patches, new EmptyJobContext());
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals(
					"stale patch staleID is supposed to been run already, " +
					"therefore cannot be run again.",
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
		return new SamplePatch(MODEL, id, true);
	}

	private SamplePatch newSamplePatchNonTx(final String id)
	{
		return new SamplePatch(MODEL, id, false);
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
		public void stopIfRequested()
		{
			actual.append("requestedToStop()");
		}

		@Override
		public void incrementProgress()
		{
			actual.append("incrementProgress()");
		}

		void assertIt(final String expected)
		{
			assertEquals(expected, actual.toString());
			actual.setLength(0);
		}
	}
}
