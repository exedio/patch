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

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.exedio.cope.Model;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import org.opentest4j.AssertionFailedError;

public class SamplePatch implements Patch
{
	private final Model model;
	private final String id;
	private final String checkExceptionMessage;
	private final boolean isTransactionally;

	SamplePatch(
			final Model model,
			final String id,
			final String checkExceptionMessage,
			final boolean isTransactionally)
	{
		this.model = model;
		this.id = id;
		this.checkExceptionMessage = checkExceptionMessage;
		this.isTransactionally = isTransactionally;
	}

	@Override
	public String getID()
	{
		return id;
	}

	@Override
	public int getStage()
	{
		return 0;
	}

	@Override
	public void check()
	{
		if(checkExceptionMessage!=null)
			throw new RuntimeException(checkExceptionMessage);
	}


	enum IsSuppressedResult { SUPER, BLOCKED, SUPPRESSED }

	private IsSuppressedResult isSuppressedResult = IsSuppressedResult.SUPER;

	@Override
	public boolean isSuppressed()
	{
		switch(isSuppressedResult)
		{
			case SUPER: return Patch.super.isSuppressed();
			case BLOCKED: throw new AssertionFailedError("isSuppressed is blocked");
			case SUPPRESSED: return true;
			default:
				throw new AssertionFailedError(String.valueOf(isSuppressedResult));
		}
	}

	SamplePatch isSuppressedResult(final IsSuppressedResult isSuppressedResult)
	{
		this.isSuppressedResult = requireNonNull(isSuppressedResult);
		return this;
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
			final PatchMutex mutex = PatchMutex.TYPE.newQuery().searchSingletonStrict();
			new SampleItem(id,
					model.currentTransaction().getName(),
					mutex);
		}
		else
		{
			assertFalse(model.hasCurrentTransaction());
			try(TransactionTry tx = model.startTransactionTry("SamplePatch " + id + " run"))
			{
				final PatchMutex mutex = PatchMutex.TYPE.newQuery().searchSingletonStrict();
				new SampleItem(id,
						null,
						mutex);
				tx.commit();
			}
		}
		if("fail".equals(id))
			throw new RuntimeException("failed");
	}
}
