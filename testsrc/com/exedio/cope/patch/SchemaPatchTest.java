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
import org.junit.Before;
import org.junit.Test;

public class SchemaPatchTest extends CopeModel4Test
{
	static final Model MODEL = PatchTest.MODEL;

	public SchemaPatchTest()
	{
		super(MODEL);
	}

	@Test public void one()
	{
		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one");
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertIt("one", items.next());
		assertFalse(items.hasNext());
		final Iterator<SchemaPatchRun> runs = runs().iterator();
		assertIt(0, patch.getBody()[0], runs.next());
		assertFalse(runs.hasNext());
	}

	@Test public void more()
	{
		assertEquals(EMPTY_LIST, items());
		final PatchesBuilder builder = new PatchesBuilder();
		final SchemaPatch patch = patch("one", "two", "three");
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
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

	private static void run(
			final Patches patches,
			final JobContext ctx)
	{
		MODEL.commit();
		try
		{
			patches.run(ctx);
			MODEL.clearCache();
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

		return new SchemaPatch()
		{
			boolean gotBody = false;

			@Override
			public String getID()
			{
				return "patchId";
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

	private List<SchemaSampleItem> items()
	{
		final Query<SchemaSampleItem> q = SchemaSampleItem.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private List<SchemaPatchRun> runs()
	{
		final Query<SchemaPatchRun> q = SchemaPatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private SchemaSampleItem assertIt(
			final String content,
			final SchemaSampleItem actual)
	{
		assertEquals("content", content, actual.getContent());
		return actual;
	}

	private SchemaPatchRun assertIt(
			final int position,
			final String sql,
			final SchemaPatchRun actual)
	{
		assertEquals("id", "patchId", actual.getPatch());
		assertEquals("position", position, actual.getPosition());
		assertEquals("sql", sql, actual.getSql());
		assertEquals("rows", 1, actual.getRows());
		return actual;
	}

	/**
	 * This method is needed because creation of SchemaSampleItem
	 * bypasses (therefore corrupt) cope item cache.
	 */
	@Before
	public void deleteItems()
	{
		MODEL.commit();
		try
		{
			MODEL.clearCache();
		}
		finally
		{
			MODEL.startTransaction(SchemaPatchTest.class.getName());
		}
		for(final SchemaSampleItem item : items())
			item.deleteCopeItem();
	}
}
