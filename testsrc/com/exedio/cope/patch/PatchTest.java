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
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.Revisions;
import com.exedio.cope.UniqueViolationException;
import com.exedio.cope.junit.LogRule;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.AssertionErrorJobContext;
import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobContexts;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LogRule.class)
public class PatchTest extends CopeModel4Test
{
	// 200 is greater than 100/101 in RevisionPatchTest
	static final int REVISION_START_ID = 200;

	static final ProxyRevisionsFactory REVISION_FACTORY = new ProxyRevisionsFactory();

	static final Model MODEL = Model.builder().
			add(REVISION_FACTORY).
			add(Patches.types).
			add(SampleItem.TYPE, SchemaSampleItem.TYPE).
			build();

	public PatchTest()
	{
		super(MODEL);
	}

	@Test void one(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));
		log.assertEvents();
		assertEquals(1, run(patches, JobContexts.EMPTY));
		log.assertEvents(
				"INFO run initiated by PatchTestInitiator",
				"ERROR savepoint",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 run 1/1 one",
				"INFO s0 mutex release",
				"INFO run finished after 1 patches");
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch stage 0 one", 1, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOne;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOne = assertIt("one", true, runs.next());
			assertFalse(runs.hasNext());
		}
		log.assertEvents();
		assertEquals(0, run(patches, JobContexts.EMPTY));
		log.assertEvents("INFO run initiated by PatchTestInitiator");
		assertEquals(asList(one), items());
		assertEquals(asList(runOne), runs());
		assertEquals(true, isDone(patches));
	}


	@Test void oneWithListener(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final TestPatchesDoneListener listener = new TestPatchesDoneListener();
		final PatchesBuilder builder = new PatchesBuilder().withDoneListener(listener);
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));
		log.assertEvents();
		assertEquals(false, listener.patchesDone);
		assertEquals(1, run(patches, JobContexts.EMPTY));
		log.assertEvents(
				"INFO run initiated by PatchTestInitiator",
				"ERROR savepoint",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 run 1/1 one",
				"INFO s0 mutex release",
				"INFO run finished after 1 patches",
				"INFO PatchesDoneListener was notified");
		assertEquals(true, listener.patchesDone);
		//would cause exception if listener would get notified again
		assertEquals(0, run(patches, JobContexts.EMPTY));
	}

	@Test void oneNonTx(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("one"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));

		log.assertEvents();
		assertEquals(1, run(patches, JobContexts.EMPTY));
		log.assertEvents(
				"INFO run initiated by PatchTestInitiator",
				"ERROR savepoint",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 run 1/1 one",
				"INFO s0 mutex release",
				"INFO run finished after 1 patches");
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
		assertEquals(true, isDone(patches));

		log.assertEvents();
		assertEquals(0, run(patches, JobContexts.EMPTY));
		log.assertEvents("INFO run initiated by PatchTestInitiator");
		assertEquals(asList(one), items());
		assertEquals(asList(runOne), runs());
		assertEquals(true, isDone(patches));
	}

	@Test void two(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final JC ctx = new JC();
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("two"));
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));

		log.assertEvents();
		assertEquals(2, run(patches, ctx));
		log.assertEvents(
				"INFO run initiated by PatchTestInitiator",
				"ERROR savepoint",
				"INFO s0 mutex seize for 2 patches",
				"INFO s0 run 1/2 one",
				"INFO s0 run 2/2 two",
				"INFO s0 mutex release",
				"INFO run finished after 2 patches");
		ctx.assertIt(
				"stop()" +
				"stop()" + "message(run s0 1/2 one)" + "progress()" +
				"stop()" + "message(run s0 2/2 two)" + "progress()" );
		final SampleItem one;
		final SampleItem two;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", "patch stage 0 one", 2, items.next());
			two = assertIt("two", "patch stage 0 two", 2, items.next());
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
		assertEquals(true, isDone(patches));

		log.assertEvents();
		assertEquals(0, run(patches, ctx));
		log.assertEvents("INFO run initiated by PatchTestInitiator");
		ctx.assertIt("");
		assertEquals(asList(one, two), items());
		assertEquals(asList(runOne, runTwo), runs());
		assertEquals(true, isDone(patches));

		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(newSamplePatch("three"));
		builder2.insertAtStart(newSamplePatch("two"));
		builder2.insertAtStart(newSamplePatch("one"));
		final Patches patches2 = builder2.build();
		assertEquals(false, isDone(patches2));

		log.assertEvents();
		assertEquals(1, run(patches2, ctx));
		log.assertEvents(
				"INFO run initiated by PatchTestInitiator",
				"ERROR savepoint",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 run 1/1 three",
				"INFO s0 mutex release",
				"INFO run finished after 1 patches");
		ctx.assertIt(
				"stop()" +
				"stop()" + "message(run s0 1/1 three)" + "progress()");
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertEquals(two, items.next());
			assertIt("three", "patch stage 0 three", 1, items.next());
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
		assertEquals(true, isDone(patches2));
	}

	@Test void empty()
	{
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		final Patches patches = builder.build();
		assertEquals(true, isDone(patches));

		assertEquals(0, run(patches, JobContexts.EMPTY));
		assertEquals(asList(), items());
		assertEquals(asList(), runs());
		assertEquals(true, isDone(patches));

		assertEquals(0, run(patches, JobContexts.EMPTY));
		assertEquals(asList(), items());
		assertEquals(asList(), runs());
		assertEquals(true, isDone(patches));
	}

	@Test void emptyWithListener()
	{
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final TestPatchesDoneListener listener = new TestPatchesDoneListener();
		final PatchesBuilder builder = new PatchesBuilder().withDoneListener(listener);
		final Patches patches = builder.build();
		assertEquals(false, listener.patchesDone);

		assertEquals(0, run(patches, JobContexts.EMPTY));
		assertEquals(true, listener.patchesDone);
	}

	@Test void preempt(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("nonTx"));
		builder.insertAtStart(newSamplePatch("two"));
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();

		assertEquals(false, isDone(patches));
		log.assertEvents();
		preempt(patches);
		log.assertEvents(
				"INFO preempt initiated by PatchTestInitiator",
				"INFO s0 mutex seize for 3 patches",
				"INFO s0 mutex release");
		final PatchRun runOne, runTwo, runNonTx;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOne   = assertPreempt("one"  , true , runs.next());
			runTwo   = assertPreempt("two"  , true , runs.next());
			runNonTx = assertPreempt("nonTx", false, runs.next());
			assertFalse(runs.hasNext());
		}
		assertEquals(true, isDone(patches));

		log.assertEvents();

		// test empty preempt
		preempt(patches);

		log.assertEvents(
				"INFO preempt initiated by PatchTestInitiator");
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals(runOne  , runs.next());
			assertEquals(runTwo  , runs.next());
			assertEquals(runNonTx, runs.next());
			assertFalse(runs.hasNext());
		}
		assertEquals(true, isDone(patches));

		// test preempt continue
		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(newSamplePatchNonTx("nonTx"));
		builder2.insertAtStart(newSamplePatch("two"));
		builder2.insertAtStart(newSamplePatch("one"));
		builder2.insertAtStart(newSamplePatch("three"));
		final Patches patches2 = builder2.build();
		assertEquals(false, isDone(patches2));
		log.assertEvents();
		preempt(patches2);
		log.assertEvents(
				"INFO preempt initiated by PatchTestInitiator",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 mutex release");
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals(runOne  , runs.next());
			assertEquals(runTwo  , runs.next());
			assertEquals(runNonTx, runs.next());
			assertPreempt("three", true, runs.next());
			assertFalse(runs.hasNext());
		}
		assertEquals(true, isDone(patches2));
	}

	@Test void preemptWithListener(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("nonTx"));
		builder.insertAtStart(newSamplePatch("two"));
		builder.insertAtStart(newSamplePatch("one"));
		final TestPatchesDoneListener listener = new TestPatchesDoneListener();
		builder.withDoneListener(listener);
		final Patches patches = builder.build();

		assertEquals(false, isDone(patches));
		assertEquals(false, listener.patchesDone);
		log.assertEvents();
		preempt(patches);
		log.assertEvents(
				"INFO preempt initiated by PatchTestInitiator",
				"INFO s0 mutex seize for 3 patches",
				"INFO s0 mutex release",
				"INFO PatchesDoneListener was notified");
		assertEquals(true, listener.patchesDone);
	}

	@Test void preemptSingle(final LogRule log)
	{
		log.listen(Patches.class);
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("nonTx"));
		builder.insertAtStart(newSamplePatch("theBadPatch"));
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();

		assertEquals(false, isDone(patches));
		assertTrue(preemptSingle(patches, "theBadPatch"));
		log.assertEvents(
				"INFO preempt theBadPatch initiated by PatchTestInitiator",
				"INFO s0 mutex seize for 1 patches",
				"INFO s0 mutex release");

		final Iterator<PatchRun> runs = runs().iterator();
		assertPreempt("theBadPatch", true, runs.next());
		assertFalse(runs.hasNext());

		assertEquals(false, isDone(patches));

		assertFalse(preemptSingle(patches, "theBadPatch"));
		log.assertEvents(
				"INFO preempt theBadPatch initiated by PatchTestInitiator");

		assertEquals(false, isDone(patches));

		assertFails(
				() -> preemptSingle(patches, "doesNotExist"),
				NoSuchElementException.class,
				"Patch with id doesNotExist does not exist");
	}

	@SuppressWarnings({"DuplicateExpressions", "RedundantSuppression"})
	@Test void failure()
	{
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("fail"));
		builder.insertAtStart(newSamplePatch("ok"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));

		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			RuntimeException.class,
			"failed");
		final SampleItem ok;
		{
			final Iterator<SampleItem> items = items().iterator();
			ok = assertIt("ok", "patch stage 0 ok", 2, items.next());
			assertFalse(items.hasNext());
		}
		final PatchRun runOk;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			runOk = assertIt("ok", true, runs.next());
			assertFalse(runs.hasNext());
		}
		assertEquals(false, isDone(patches));

		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			UniqueViolationException.class,
			"unique violation for CopePatchMutex.idImplicitUnique");
		assertEquals(asList(ok), items());
		assertEquals(asList(runOk), runs());
		assertEquals(false, isDone(patches));

		PatchMutex.release();
		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			RuntimeException.class,
			"failed");
		assertEquals(asList(ok), items());
		assertEquals(asList(runOk), runs());
		assertEquals(false, isDone(patches));
	}

	@SuppressWarnings({"DuplicateExpressions", "RedundantSuppression"})
	@Test void failureNonTx()
	{
		assertEquals(emptyList(), items());
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatchNonTx("fail"));
		builder.insertAtStart(newSamplePatchNonTx("ok"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));

		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			RuntimeException.class,
			"failed");
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
		assertEquals(false, isDone(patches));

		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			UniqueViolationException.class,
			"unique violation for CopePatchMutex.idImplicitUnique");
		assertEquals(asList(ok, fail1), items());
		assertEquals(asList(runOk), runs());
		assertEquals(false, isDone(patches));

		PatchMutex.release();
		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			RuntimeException.class,
			"failed");
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
		assertEquals(false, isDone(patches));
	}

	@Test void stale()
	{
		final String id = "staleID";

		assertEquals(emptyList(), items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch(id));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));
		assertEquals(1, run(patches, JobContexts.EMPTY));
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt(id, "patch stage 0 " + id, 1, items.next());
			assertFalse(items.hasNext());
		}
		assertEquals(true, isDone(patches));

		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(Patches.stale(id));
		final Patches patches2 = builder2.build();
		assertEquals(true, isDone(patches2));
		assertEquals(0, run(patches2, JobContexts.EMPTY));
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertFalse(items.hasNext());
		}
		assertEquals(true, isDone(patches2));
	}

	@Test void staleError()
	{
		assertEquals(emptyList(), items());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(Patches.stale("staleID"));
		final Patches patches = builder.build();
		assertEquals(false, isDone(patches));
		assertFails(() ->
			run(patches, JobContexts.EMPTY),
			RuntimeException.class,
			"stale patch >staleID< is supposed to been run already, " +
			"therefore cannot be run again.");
		assertEquals(emptyList(), items());
		assertEquals(false, isDone(patches));
	}

	@Test void nullCtx()
	{
		final Patches patches = new PatchesBuilder().build();
		assertFails(() ->
			run(patches, null),
			NullPointerException.class, "ctx");
	}

	@Test void insertStaleFromResource() throws IOException
	{
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertStaleFromResource(PatchTest.class);
		final Patches patches = builder.build();
		preempt(patches);
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals("staleFromResource3", runs.next().getPatch());
			assertEquals("staleFromResource2", runs.next().getPatch());
			assertEquals("staleFromResource1", runs.next().getPatch());
			assertFalse(runs.hasNext());
		}
	}

	@Test void withStaleFromResource()
	{
		assertEquals(emptyList(), runs());
		final PatchesBuilder builder = new PatchesBuilder().withStaleFromResource(PatchTest.class);
		final Patches patches = builder.build();
		preempt(patches);
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals("staleFromResource3", runs.next().getPatch());
			assertEquals("staleFromResource2", runs.next().getPatch());
			assertEquals("staleFromResource1", runs.next().getPatch());
			assertFalse(runs.hasNext());
		}
	}

	private static PatchInitiator createPatchInitiator()
	{
		return new PatchInitiator("PatchTestInitiator");
	}

	static int run(
			final Patches patches,
			final JobContext ctx)
	{
		MODEL.commit();
		int result;
		try
		{
			result = patches.run(ctx, createPatchInitiator());
		}
		finally
		{
			MODEL.startTransaction(PatchTest.class.getName());
		}
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
		return result;
	}

	static boolean isDone(
			final Patches patches)
	{
		MODEL.commit();
		boolean result;
		DoneResult done;
		try
		{
			result = patches.isDone();
			done = patches.getDone();
		}
		finally
		{
			MODEL.startTransaction(PatchTest.class.getName());
		}
		assertEquals(result, done.isDone(), done.name());
		return result;
	}

	static void preempt(final Patches patches)
	{
		MODEL.commit();
		try
		{
			patches.preempt(createPatchInitiator());
		}
		finally
		{
			MODEL.startTransaction(PatchTest.class.getName());
		}
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
	}

	static boolean preemptSingle(final Patches patches, final String id)
	{
		MODEL.commit();
		boolean result;
		try
		{
			result = patches.preempt(id, createPatchInitiator());
		}
		finally
		{
			MODEL.startTransaction(PatchTest.class.getName());
		}
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
		return result;
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
		assertEquals(id, actual.getPatch(), "id");
		assertEquals(transactionName, actual.getTransactionName(), "transactionName");
		assertEquals(getHost(), actual.getMutexHost(), "mutexHost");
		assertEquals("FAILURE: not supported", actual.getMutexSavepoint(), "mutexSavepoint");
		assertEquals(mutexNumberOfPatches, actual.getMutexNumberOfPatches(), "mutexNumberOfPatches");
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
		assertEquals(id, actual.getPatch(), "id");
		assertEquals(0, actual.getStage(), "stage");
		assertEquals(isTransactionally, actual.getIsTransactionally(), "isTransactionally");
		assertEquals(getHost(), actual.getHost(), "host");
		assertEquals("FAILURE: not supported", actual.getSavepoint(), "savepoint");
		return actual;
	}

	private static PatchRun assertPreempt(
			final String id,
			final boolean isTransactionally,
			final PatchRun actual)
	{
		assertEquals(id, actual.getPatch(), "id");
		assertEquals(0, actual.getStage(), "stage");
		assertEquals(isTransactionally, actual.getIsTransactionally(), "isTransactionally");
		assertEquals(getHost(), actual.getHost(), "host");
		assertEquals("preempted", actual.getSavepoint(), "savepoint");
		assertEquals(0, actual.getElapsed(), "elapsed");
		return actual;
	}

	static class JC extends AssertionErrorJobContext
	{
		private final StringBuilder actual = new StringBuilder();

		@Override
		public void stopIfRequested()
		{
			actual.append("stop()");
		}

		@Override
		public boolean supportsMessage()
		{
			return true;
		}

		@Override
		public void setMessage(final String message)
		{
			actual.append("message(" + message + ")");
		}

		@Override
		public void incrementProgress()
		{
			actual.append("progress()");
		}

		void assertIt(final String expected)
		{
			assertEquals(expected, actual.toString());
			actual.setLength(0);
		}
	}

	private static String getHost()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch(final UnknownHostException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final class TestPatchesDoneListener implements PatchesDoneListener
	{
		private boolean patchesDone = false;
		@Override
		public void notifyPatchesDone()
		{
			assertEquals(false, patchesDone);
			patchesDone = true;
		}
	}

	/**
	 * Revisions.Factory which initializes by default with a static revision number but also allows tests
	 * to override this with a delegate.
	 */
	static class ProxyRevisionsFactory implements Revisions.Factory
	{
		private Revisions.Factory delegate = null;

		@Override
		public Revisions create(final Context ctx)
		{
			if (delegate != null)
				return delegate.create(ctx);
			else
				return new Revisions(REVISION_START_ID);
		}

		public void setDelegate(final Revisions.Factory delegate)
		{
			this.delegate = delegate;
		}
	}
}
