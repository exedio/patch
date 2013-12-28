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
import com.exedio.cope.Revisions;
import com.exedio.cope.TypeSet;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.EmptyJobContext;
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
		Patches.run(asList(
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		final SampleItem one;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", items.next());
			assertFalse(items.hasNext());
		}
		Patches.run(asList(
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		assertEquals(asList(one), items());
	}

	@Test public void two()
	{
		assertEquals(EMPTY_LIST, items());
		Patches.run(asList(
				new SamplePatch("two"),
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		final SampleItem one;
		final SampleItem two;
		{
			final Iterator<SampleItem> items = items().iterator();
			one = assertIt("one", items.next());
			two = assertIt("two", items.next());
			assertFalse(items.hasNext());
		}
		Patches.run(asList(
				new SamplePatch("two"),
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		assertEquals(asList(one, two), items());
		Patches.run(asList(
				new SamplePatch("three"),
				new SamplePatch("two"),
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		{
			final Iterator<SampleItem> items = items().iterator();
			assertEquals(one, items.next());
			assertEquals(two, items.next());
			assertIt("three", items.next());
			assertFalse(items.hasNext());
		}
	}

	private List<SampleItem> items()
	{
		final Query<SampleItem> q = SampleItem.TYPE.newQuery();
		q.setOrderBy(SampleItem.number, true);
		return q.search();
	}

	private SampleItem assertIt(final String id, final SampleItem actual)
	{
		assertEquals("id", id, actual.getPatch());
		return actual;
	}
}
