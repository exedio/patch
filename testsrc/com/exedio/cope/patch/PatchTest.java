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
import static org.junit.Assert.assertEquals;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.EmptyJobContext;
import java.util.List;
import org.junit.Test;

public class PatchTest extends CopeModel4Test
{
	static final Model MODEL = new Model(SampleItem.TYPE);

	public PatchTest()
	{
		super(MODEL);
	}

	@Test public void one()
	{
		assertEquals(asList(), ids());
		Patches.run(asList(
				new SamplePatch("one")
			),
			new EmptyJobContext()
		);
		assertEquals(asList("one"), ids());
	}

	@Test public void two()
	{
		assertEquals(asList(), ids());
		Patches.run(asList(
				new SamplePatch("one"),
				new SamplePatch("two")
			),
			new EmptyJobContext()
		);
		assertEquals(asList("one", "two"), ids());
	}

	private List<String> ids()
	{
		final Query<String> q = new Query<String>(SampleItem.patch, SampleItem.TYPE, null);
		q.setOrderBy(SampleItem.number, true);
		return q.search();
	}
}
