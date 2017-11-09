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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.patch.cope.CopeRule;
import com.exedio.cope.util.JobContexts;
import org.junit.jupiter.api.Test;

/**
 * This test simulates the usage of patches within the project.
 */
public class PatchesRealTest extends CopeModel4Test
{
	private static final PatchesBuilder builder = new PatchesBuilder();

	static
	{
		// insert new patches here !!!
		add(newSamplePatch("other1"));
		add(newSamplePatch("other2"));
	}

	public static final Patches patches = builder.build();

	private static void add(final Patch patch)
	{
		builder.insertAtStart(patch);
	}


	// here the test specific stuff starts

	public PatchesRealTest()
	{
		super(MODEL);
	}

	@CopeRule.NoTransaction
	@Test void run()
	{
		assertEquals(false, patches.isDone());
		assertEquals(2, patches.run(JobContexts.EMPTY));
		assertEquals(true, patches.isDone());
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id, null, true);
	}
}
