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

import static org.junit.Assert.assertFalse;

import com.exedio.cope.Model;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;

public class SamplePatch implements Patch
{
	private final Model model;
	private final String id;
	private final boolean isTransactionally;

	SamplePatch(final Model model, final String id, final boolean isTransactionally)
	{
		this.model = model;
		this.id = id;
		this.isTransactionally = isTransactionally;
	}

	@Override
	public String getID()
	{
		return id;
	}

	@Override
	public boolean isTransactionally()
	{
		return isTransactionally;
	}

	@Override
	public void run(final JobContext ctx)
	{
		if(isTransactionally)
		{
			new SampleItem(id, model.currentTransaction().getName());
		}
		else
		{
			assertFalse(model.hasCurrentTransaction());
			try(TransactionTry tx = model.startTransactionTry("SamplePatch " + id + " run"))
			{
				new SampleItem(id, null);
				tx.commit();
			}
		}
		if("fail".equals(id))
			throw new RuntimeException("failed");
	}
}
