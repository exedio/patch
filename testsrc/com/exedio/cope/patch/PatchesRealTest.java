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

import static com.exedio.cope.patch.PatchTest.MODEL;

import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.EmptyJobContext;
import org.junit.Test;

public class PatchesRealTest extends CopeModel4Test
{
	private static final PatchesBuilder builder = new PatchesBuilder();

	static
	{
		builder.add(newSamplePatch("other1"));
		builder.add(newSamplePatch("other2"));
	}

	public static final Patches patches = builder.build();

	public PatchesRealTest()
	{
		super(MODEL);
	}

	@Override
	protected boolean doesManageTransactions()
	{
		return false;
	}

	@Test public void run()
	{
		patches.run(new EmptyJobContext());
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id, true);
	}
}
