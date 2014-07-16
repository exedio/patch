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

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobContexts;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class StageTest extends CopeModel4Test
{
	static final Model MODEL = PatchTest.MODEL;

	public StageTest()
	{
		super(MODEL);
	}

	@Test public void test()
	{
		assertEquals(EMPTY_LIST, runs());
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(patch("oneB", 1));
		builder.insertAtStart(patch("twoB", 2));
		builder.insertAtStart(patch("twoA", 2));
		builder.insertAtStart(patch("oneA", 1));
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		final PatchRun oneA;
		final PatchRun oneB;
		final PatchRun twoA;
		final PatchRun twoB;
		{
			final Iterator<PatchRun> runs = runs().iterator();
			oneA = assertIt("oneA", 1, runs.next());
			oneB = assertIt("oneB", 1, runs.next());
			twoA = assertIt("twoA", 2, runs.next());
			twoB = assertIt("twoB", 2, runs.next());
			assertFalse(runs.hasNext());
		}
		run(patches, JobContexts.EMPTY);
		assertEquals(asList(oneA, oneB, twoA, twoB), runs());
		final PatchesBuilder builder2 = new PatchesBuilder();
		builder2.insertAtStart(patch("twoC", 2));
		builder2.insertAtStart(patch("oneC", 1));
		builder2.insertAtStart(patch("threeC", 3));
		builder2.insertAtStart(patch("oneB", 1));
		builder2.insertAtStart(patch("twoB", 2));
		builder2.insertAtStart(patch("twoA", 2));
		builder2.insertAtStart(patch("oneA", 1));
		final Patches patches2 = builder2.build();
		run(patches2, JobContexts.EMPTY);
		{
			final Iterator<PatchRun> runs = runs().iterator();
			assertEquals(oneA, runs.next());
			assertEquals(oneB, runs.next());
			assertEquals(twoA, runs.next());
			assertEquals(twoB, runs.next());
			assertIt("oneC"  , 1, runs.next());
			assertIt("twoC"  , 2, runs.next());
			assertIt("threeC", 3, runs.next());
			assertFalse(runs.hasNext());
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
			MODEL.startTransaction(StageTest.class.getName());
		}
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
	}

	private static Patch patch(final String id, final int stage)
	{
		return new Patch()
		{
			@Override
			public String getID()
			{
				return id;
			}

			@Override
			public int getStage()
			{
				return stage;
			}

			@Override
			public void check()
			{
				// empty
			}

			@Override
			public boolean isTransactionally()
			{
				return true;
			}

			@Override
			public void run(final JobContext ctx)
			{
				// empty
			}
		};
	}

	private static List<PatchRun> runs()
	{
		final Query<PatchRun> q = PatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private static PatchRun assertIt(
			final String id,
			final int stage,
			final PatchRun actual)
	{
		assertEquals("id", id, actual.getPatch());
		assertEquals("stage", stage, actual.getStage());
		assertEquals("isTransactionally", true, actual.getIsTransactionally());
		assertEquals("savepoint", "FAILURE: not supported", actual.getSavepoint());
		return actual;
	}
}