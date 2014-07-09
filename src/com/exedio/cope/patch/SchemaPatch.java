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

import static com.exedio.cope.misc.TimeUtil.toMillies;
import static java.lang.System.nanoTime;
import static java.util.Objects.requireNonNull;

import com.exedio.cope.Model;
import com.exedio.cope.SchemaInfo;
import com.exedio.cope.util.JobContext;
import com.exedio.dsmf.SQLRuntimeException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SchemaPatch implements Patch
{
	private static final Logger logger = LoggerFactory.getLogger(SchemaPatch.class);

	private final Model model;

	SchemaPatch(final Model model)
	{
		this.model = requireNonNull(model, "model");
	}

	@Override
	public void check()
	{
		final String[] body = getBody();
		if(body==null)
			throw new NullPointerException("body");
		if(body.length==0)
			throw new IllegalArgumentException("body must not be empty");
		for(int i = 0; i<body.length; i++)
		{
			if(body[i]==null)
				throw new NullPointerException("body" + '[' + i + ']');
			if(body[i].isEmpty())
				throw new IllegalArgumentException("body[" + i + "] must not be empty");
		}
	}

	@Override
	public final boolean isTransactionally()
	{
		return false;
	}

	protected abstract String[] getBody();

	@Override
	public final void run(final JobContext ctx)
	{
		final String id = getID();

		final String[] body = getBody();

		try(Connection connection = SchemaInfo.newConnection(model))
		{
			for(int position = 0; position<body.length; position++)
			{
				final String sql = body[position];
				if(logger.isInfoEnabled())
					logger.info("{} {}/{}: {}", new Object[]{id, position, body.length, sql});
				final long start = nanoTime();
				final int rows = execute(connection, sql);
				final long elapsed = toMillies(nanoTime(), start);

				new SchemaPatchRun(id, position, rows, elapsed);
			}
		}
		catch(final SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE") // OK
	private static final int execute(
			final Connection connection,
			final String sql)
	{
		try(Statement statement = connection.createStatement())
		{
			return statement.executeUpdate(sql);
		}
		catch(final SQLException e)
		{
			throw new SQLRuntimeException(e, sql);
		}
	}
}
