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
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Envelope
{
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final Model model;
	final String savepoint;
	private final PatchMutex mutex;

	Envelope(
			final Model model,
			final int numberOfPatches,
			final JobContext ctx)
	{
		this.model = model;

		ctx.stopIfRequested();
		savepoint = getSavepoint(model);

		logger.info("mutex");
		try(TransactionTry tx = model.startTransactionTry("patch mutex seize"))
		{
			mutex = new PatchMutex(savepoint, numberOfPatches);
			tx.commit();
		}
	}

	private static String getSavepoint(final Model model)
	{
		final String result;
		try
		{
			result = model.getSchemaSavepoint();
		}
		catch(final SQLException e)
		{
			logger.error("savepoint", e);
			return "FAILURE: " + e.getMessage();
		}
		logger.info("savepoint {}", result);
		return result;
	}

	void close()
	{
		try(TransactionTry tx = model.startTransactionTry("patch mutex release"))
		{
			mutex.deleteCopeItem();
			tx.commit();
		}
	}
}
