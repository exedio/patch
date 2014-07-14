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
import com.exedio.cope.UniqueViolationException;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.AssertionErrorJobContext;
import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobContexts;
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
		assertEquals(EMPTY_LIST, runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch s0 one", 1, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOne;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOne = assertIt("one", true, runs.next());
			assertFalse(runs.hasNext());
		}
		run(patches, JobContexts.EMPTY);
		assertEquals(asList(one), items());
		assertEquals(asList(runOne), runs());
	}

	@Test public void oneNonTx()
	{
		assertEquals(EMPTY_LIST, items());
		assertEquals(EMPTY_LIST, runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("one"));
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", null, 1, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOne;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOne = assertIt("one", false, runs.next());
			assertFalse(runs.hasNext());
		}
		run(patches, JobContexts.EMPTY);
		assertEquals(asList(one), items());
		assertEquals(asList(runOne), runs());
	}

	@Test public void two()
	{
		assertEquals(EMPTY_LIST, items());
		assertEquals(EMPTY_LIST, runs());
		final JC ctx = new JC();
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("two"));
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		run(patches, ctx);
		ctx.assertIt(
				"requestedToStop()" +
				"requestedToStop()" + "setMessage(run s0 one)" + "incrementProgress()" +
				"requestedToStop()" + "setMessage(run s0 two)" + "incrementProgress()" );
		final SampleItem one;
		final SampleItem two;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch s0 one", 2, items.next());
			two = assertIt("two", "patch s0 two", 2, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOne;
		final PatchRun runTwo;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOne = assertIt("one", true, runs.next());
			runTwo = assertIt("two", true, runs.next());
			assertFalse(runs.hasNext());
		}
		run(patches, ctx);
		ctx.assertIt("");
		assertEquals(asList(one, two), items());
		assertEquals(asList(runOne, runTwo), runs());
		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(newSamplePatch("three"));
		builder2.insertAtStart(newSamplePatch("two"));
		builder2.insertAtStart(newSamplePatch("one"));
		final Patches patches2 = builder2.build();
		run(patches2, ctx);
		ctx.assertIt(
				"requestedToStop()" +
				"requestedToStop()" + "setMessage(run s0 three)" + "incrementProgress()");
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertEquals(two, items.next());
			assertIt("three", "patch s0 three", 1, items.next());
			assertFalse(items.hasNext());
		}
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals(runOne, runs.next());
			assertEquals(runTwo, runs.next());
			assertIt("three", true, runs.next());
			assertFalse(runs.hasNext());
		}
		ctx.assertIt("");
	}

	@Test public void failure()
	{
		assertEquals(EMPTY_LIST, items());
		assertEquals(EMPTY_LIST, runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("fail"));
		builder.insertAtStart(newSamplePatch("ok"));
		final Patches patches = builder.build();
		try
		{
			run(patches, JobContexts.EMPTY);
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("failed", e.getMessage());
		}
		final SampleItem ok;
		{
			final Iterator<SampleItem> items = items().iterator();
			ok = assertIt("ok", "patch s0 ok", 2, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOk;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOk = assertIt("ok", true, runs.next());
			assertFalse(runs.hasNext());
		}

		try
		{
			run(patches, JobContexts.EMPTY);
			fail();
		}
		catch(final UniqueViolationException e)
		{
			assertEquals("unique violation for CopePatchMutex.idImplicitUnique", e.getMessage());
		}
		assertEquals(asList(ok), items());
		assertEquals(asList(runOk), runs());

		PatchMutex.release();
		try
		{
			run(patches, JobContexts.EMPTY);
			fail();
		}
		catch(final RuntimeException e)
		{
			assertEquals("failed", e.getMessage());
		}
		assertEquals(asList(ok), items());
		assertEquals(asList(runOk), runs());
	}

	@Test public void failureNonTx()
	{
		assertEquals(EMPTY_LIST, items());
		assertEquals(EMPTY_LIST, runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("fail"));
		builder.insertAtStart(newSamplePatchNonTx("ok"));
		final Patches patches = builder.build();
		try
		{
			run(patches, JobContexts.EMPTY);
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
			ok    = assertIt("ok"  , null, 2, items.next());
			fail1 = assertIt("fail", null, 2, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOk;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOk = assertIt("ok", false, runs.next());
			assertFalse(runs.hasNext());
		}

		try
		{
			run(patches, JobContexts.EMPTY);
			fail();
		}
		catch(final UniqueViolationException e)
		{
			assertEquals("unique violation for CopePatchMutex.idImplicitUnique", e.getMessage());
		}
		assertEquals(asList(ok, fail1), items());
		assertEquals(asList(runOk), runs());

		PatchMutex.release();
		try
		{
			run(patches, JobContexts.EMPTY);
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
			assertFalse(fail1.equals(assertIt("fail", null, 1, items.next())));
			assertFalse(items.hasNext());
		}
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals(runOk, runs.next());
			assertFalse(runs.hasNext());
		}
	}

	@Test public void stale()
	{
		final String id = "staleID";

		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch(id));
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt(id, "patch s0 " + id, 1, items.next());
			assertFalse(items.hasNext());
		}

		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(Patches.stale(id));
		final Patches patches2 = builder2.build();
		run(patches2, JobContexts.EMPTY);
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
			run(patches, JobContexts.EMPTY);
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

	@Test public void nullCtx()
	{
		final Patches patches = new PatchesBuilder().build();
		try
		{
			run(patches, null);
			fail();
		}
		catch(final NullPointerException e)
		{
			assertEquals("ctx", e.getMessage());
		}
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
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id, null, true);
	}

	private static SamplePatch newSamplePatchNonTx(final String id)
	{
		return new SamplePatch(MODEL, id, null, false);
	}

	private static List<SampleItem> items()
	{
		final Query<SampleItem> q = SampleItem.TYPE.newQuery();
		q.setOrderBy(SampleItem.number, true);
		return q.search();
	}

	private static SampleItem assertIt(
			final String id,
			final String transactionName,
			final int mutexNumberOfPatches,
			final SampleItem actual)
	{
		assertEquals("id", id, actual.getPatch());
		assertEquals("transactionName", transactionName, actual.getTransactionName());
		assertEquals("mutexSavepoint", "FAILURE: not supported", actual.getMutexSavepoint());
		assertEquals("mutexNumberOfPatches", mutexNumberOfPatches, actual.getMutexNumberOfPatches());
		return actual;
	}

	private static List<PatchRun> runs()
	{
		final Query<PatchRun> q = PatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private static PatchRun assertIt(
			final String id,
			final boolean isTransactionally,
			final PatchRun actual)
	{
		assertEquals("id", id, actual.getPatch());
		assertEquals("stage", 0, actual.getStage());
		assertEquals("isTransactionally", isTransactionally, actual.getIsTransactionally());
		assertEquals("savepoint", "FAILURE: not supported", actual.getSavepoint());
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
		public boolean supportsMessage()
		{
			return true;
		}

		@Override
		public void setMessage(final String message)
		{
			actual.append("setMessage(" + message + ")");
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
