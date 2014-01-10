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

import com.exedio.cope.Model;
import com.exedio.cope.TypeSet;
import com.exedio.cope.util.JobContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class Patches
{
	// TODO remove patches safely
	// TODO stages
	public static void run(
			final List<? extends Patch> patchesDescending,
			final JobContext ctx)
	{
		final ArrayList<Patch> patches = new ArrayList<Patch>(patchesDescending);
		{
			final HashSet<String> ids = new HashSet<String>();
			int position = 0;
			for(final Patch patch : patches)
			{
				final String id = patch.getID();
				if(!ids.add(id))
					throw new IllegalArgumentException("duplicate id >" + id + "< at position " + position);
				position++;
			}
		}
		Collections.reverse(patches);
		final Model model = PatchRun.TYPE.getModel();
		for(final Patch patch : patches)
		{
			final String id = patch.getID();
			try
			{
				model.startTransaction("patch " + id);
				// TODO faster query
				if(PatchRun.forPatch(id)==null)
				{
					// TODO logging
					// TODO transactions
					// TODO ctx stop
					// TODO ctx message
					// TODO ctx progress
					// TODO date / elapsed
					patch.run(ctx);
					new PatchRun(id);
				}
				model.commit();
			}
			finally
			{
				model.rollbackIfNotCommitted();
			}
		}
	}

	public static final TypeSet types = new TypeSet(PatchRun.TYPE);

	private Patches()
	{
		// prevent instantiation
	}
}
