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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.junit.LogRule;
import com.exedio.cope.patch.PatchTest.JC;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobContexts;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LogRule.class)
public class SchemaPatchTest extends CopeModel4Test
{
	static final Model MODEL = PatchTest.MODEL;

	public SchemaPatchTest()
	{
		super(MODEL);
	}

	@Test void one(final LogRule log)
	{
		log.listen(Patches.class, "pt");
		log.listen(SchemaPatch.class, "spt");
		assertEquals(List.of(), items());
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one");
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		log.assertEvents();
		assertEquals(1, run(patches, JobContexts.EMPTY));
		log.assertEvents(
				"pt: INFO run initiated by SchemaPatchTestInitiator",
				"pt: WARN savepoint not supported by com.exedio.cope.HsqldbDialect",
				"pt: INFO s0 mutex seize for 1 patches",
				"pt: INFO s0 run 1/1 patchId",
				"spt: INFO executing 1 statements for patchId",
				"spt: INFO 1/1: INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 0, 'one' )",
				"pt: INFO s0 mutex release",
				"pt: INFO run finished after 1 patches");
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertIt("one", items.next());
		assertFalse(items.hasNext());
		final Iterator<SchemaPatchRun> runs = runs().iterator();
		assertIt(0, patch.getBody()[0], runs.next());
		assertFalse(runs.hasNext());
	}

	@Test void more(final LogRule log)
	{
		log.listen(Patches.class, "pt");
		log.listen(SchemaPatch.class, "spt");
		assertEquals(List.of(), items());
		final JC ctx = new JC();
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", "two", "three");
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		log.assertEvents();
		assertEquals(1, run(patches, ctx));
		log.assertEvents(
				"pt: INFO run initiated by SchemaPatchTestInitiator",
				"pt: WARN savepoint not supported by com.exedio.cope.HsqldbDialect",
				"pt: INFO s0 mutex seize for 1 patches",
				"pt: INFO s0 run 1/1 patchId",
				"spt: INFO executing 3 statements for patchId",
				"spt: INFO 1/3: INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 0, 'one' )",
				"spt: INFO 2/3: INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 1, 'two' )",
				"spt: INFO 3/3: INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 2, 'three' )",
				"pt: INFO s0 mutex release",
				"pt: INFO run finished after 1 patches");
		ctx.assertIt(
				"stop()" +
				"stop()" + "message(run s0 1/1 patchId)" +
				"message(SchemaPatch 1/3 INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 0, 'one' ))"   + "progress()" +
				"message(SchemaPatch 2/3 INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 1, 'two' ))"   + "progress()" +
				"message(SchemaPatch 3/3 INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 2, 'three' ))" + "progress()" +
				"progress()");
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertIt("one", items.next());
		assertIt("two", items.next());
		assertIt("three", items.next());
		assertFalse(items.hasNext());
		final Iterator<SchemaPatchRun> runs = runs().iterator();
		assertIt(0, patch.getBody()[0], runs.next());
		assertIt(1, patch.getBody()[1], runs.next());
		assertIt(2, patch.getBody()[2], runs.next());
		assertFalse(runs.hasNext());
	}

	@Test void checkOk(final LogRule log)
	{
		log.listen(Patches.class, "pt");
		log.listen(SchemaPatch.class, "spt");
		assertEquals(List.of(), items());
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patchCheck(
				new String[]{
						"VALUES(0) -- first",
						"VALUES(0) -- second"
				},
				SchemaSampleItem.create("body"));
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		log.assertEvents();
		assertEquals(1, run(patches, JobContexts.EMPTY));
		log.assertEvents(
				"pt: INFO run initiated by SchemaPatchTestInitiator",
				"pt: WARN savepoint not supported by com.exedio.cope.HsqldbDialect",
				"pt: INFO s0 mutex seize for 1 patches",
				"pt: INFO s0 run 1/1 patchId",
				"spt: INFO check 1/2: VALUES(0) -- first",
				"spt: INFO check 2/2: VALUES(0) -- second",
				"spt: INFO executing 1 statements for patchId",
				"spt: INFO 1/1: INSERT INTO \"SchemaSampleItem\" ( \"this\", \"content\" ) VALUES ( 0, 'body' )",
				"pt: INFO s0 mutex release",
				"pt: INFO run finished after 1 patches");
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertIt("body", items.next());
		assertFalse(items.hasNext());
		final Iterator<SchemaPatchRun> runs = runs().iterator();
		assertIt(0, patch.getBody()[0], runs.next());
		assertFalse(runs.hasNext());
	}

	@Test void checkEmpty(final LogRule log)
	{
		assertCheckFails(log,
				SchemaSampleItem.select(),
				"Check failed, returned empty result set: ");
	}

	@Test void checkNull(final LogRule log)
	{
		assertCheckFails(log,
				"VALUES(NULL)",
				"Check failed, returned null: ");
	}

	@Test void checkOne(final LogRule log)
	{
		assertCheckFails(log,
				"VALUES(1)",
				"Check failed, returned 1: ");
	}

	@Test void checkMoreThanOne(final LogRule log)
	{
		assertCheckFails(log,
				"VALUES(55)",
				"Check failed, returned 55: ");
	}

	@Test void checkTwoRows(final LogRule log)
	{
		assertCheckFails(log,
				"VALUES(0),(0)",
				"Check failed, returned result set with more than one line: ");
	}

	private static void assertCheckFails(
			final LogRule log,
			final String check,
			final String failure)
	{
		log.listen(Patches.class, "pt");
		log.listen(SchemaPatch.class, "spt");
		assertEquals(List.of(), items());
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patchCheck(
				new String[]{ check },
				SchemaSampleItem.create("body"));
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		log.assertEvents();
		assertFails(
				() -> run(patches, JobContexts.EMPTY),
				IllegalStateException.class,
				failure + check);
		log.assertEvents(
				"pt: INFO run initiated by SchemaPatchTestInitiator",
				"pt: WARN savepoint not supported by com.exedio.cope.HsqldbDialect",
				"pt: INFO s0 mutex seize for 1 patches",
				"pt: INFO s0 run 1/1 patchId",
				"spt: INFO check 1/1: " + check);
		assertEquals(List.of(), items());
		assertEquals(List.of(), runs());
	}

	private static int run(
			final Patches patches,
			final JobContext ctx)
	{
		MODEL.commit();
		try
		{
			return patches.run(ctx, new PatchInitiator("SchemaPatchTestInitiator"));
		}
		finally
		{
			MODEL.startTransaction(SchemaPatchTest.class.getName());
		}
	}

	private static SchemaPatch patch(final String... contents)
	{
		final String[] body = new String[contents.length];
		for(int i = 0; i<contents.length; i++)
			body[i] = SchemaSampleItem.create(contents[i]);

		return new SchemaPatch(body)
		{
			@Override
			public String getID()
			{
				return "patchId";
			}

			@Override
			public int getStage()
			{
				return 0;
			}
		};
	}

	private static SchemaPatch patchCheck(
			final String[] checks,
			final String body)
	{
		return new SchemaPatch(checks, new String[]{ body })
		{
			@Override
			public String getID()
			{
				return "patchId";
			}

			@Override
			public int getStage()
			{
				return 0;
			}
		};
	}

	private static List<SchemaSampleItem> items()
	{
		final Query<SchemaSampleItem> q = SchemaSampleItem.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private static List<SchemaPatchRun> runs()
	{
		final Query<SchemaPatchRun> q = SchemaPatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private static void assertIt(
			final String content,
			final SchemaSampleItem actual)
	{
		assertEquals(content, actual.getContent(), "content");
	}

	private static void assertIt(
			final int position,
			final String sql,
			final SchemaPatchRun actual)
	{
		assertEquals("patchId", actual.getPatch(), "id");
		assertEquals(position, actual.getPosition(), "position");
		assertEquals(sql, actual.getSql(), "sql");
		assertEquals(1, actual.getRows(), "rows");
	}

	/**
	 * This method is needed because creation of SchemaSampleItem
	 * bypasses (therefore corrupt) cope item cache.
	 */
	@BeforeEach void deleteItems()
	{
		for(final SchemaSampleItem item : items())
			item.deleteCopeItem();
	}

	@AfterEach void resetThisSource()
	{
		SchemaSampleItem.thisSource.set(0);
	}
}
