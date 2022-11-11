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

import com.exedio.cope.util.JobContext;

public interface Patch
{
	String getID();
	int getStage();

	/**
	 * The default implementation is empty.
	 */
	default void check()
	{
		// empty
	}

	/**
	 * If this method returns {@code false} this patch is treated normally.
	 * The default implementation returns {@code false}.
	 * <p>
	 * If this methods returns {@code true} this patch is treated as if the
	 * patch did not exist at all by
	 * {@link Patches#run(JobContext, PatchInitiator) Patches#run},
	 * {@link Patches#preempt(PatchInitiator) Patches#preempt}, and
	 * {@link Patches#isDone() Patches#isDone()}.
	 * As an exception the methods named above may emit log events
	 * when skipping suppressed patches.
	 * <p>
	 * This method is guaranteed not to be called within
	 * {@link PatchesBuilder#build()},
	 * so the implementation may rely on information
	 * that becomes available only later.
	 */
	default boolean isSuppressed()
	{
		return false;
	}

	/**
	 * Specifies, whether the framework shall manage
	 * {@link com.exedio.cope.Transaction transactions}
	 * for the patch.
	 *
	 * If returns true, the framework
	 * {@link com.exedio.cope.Model#startTransaction(String) starts}
	 * a transaction for the patch, commits the transaction on normal termination of
	 * {@link #run(JobContext) run} or rolls back the transaction on failure.
	 *
	 * If returns false, no transaction is started or committed,
	 * but transaction is rolled back on failure.
	 */
	boolean isTransactionally();

	void run(JobContext ctx);
}
