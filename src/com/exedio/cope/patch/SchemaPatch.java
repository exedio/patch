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
import static com.exedio.cope.util.Check.requireNonEmptyAndCopy;
import static java.lang.System.nanoTime;

import com.exedio.cope.ConstraintViolationException;
import com.exedio.cope.Model;
import com.exedio.cope.Revision;
import com.exedio.cope.SchemaInfo;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.misc.Arrays;
import com.exedio.cope.util.JobContext;
import com.exedio.dsmf.SQLRuntimeException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SchemaPatch implements Patch
{
	private static final Logger logger = LoggerFactory.getLogger(SchemaPatch.class);

	@Override
	public void check()
	{
		getBodyInternal();
	}

	@Override
	public final boolean isTransactionally()
	{
		return false;
	}

	/**
	 * The statements listed here
	 * are guaranteed to be executed subsequently
	 * in the order specified by the result
	 * by one single {@link java.sql.Connection connection}.
	 * So you may use connection states within a patch.
	 * <p>
	 * For each patch a new {@link java.sql.Connection connection} is created.
	 * That connection is not used for any other purpose afterwards,
	 * so you don't have to clean up connection state at the end of each patch.
	 * This is for minimizing effects between patches.
	 * <p>
	 * This behaviour is consistent to {@link Revision#getBody()}.
	 * <p>
	 * This method is guaranteed to be called once only
	 * for each instance of SchemaPatch.
	 * <p>
	 * <b>IMMUTABILITY:</b><br>
	 * The result should not change later during further development -
	 * not even implicitly by other changes in the code.
	 * <br>
	 * Consider a schema patch with the following statements:
	 * <ul>
	 * <li>{@code "ALTER TABLE Product ADD COLUMN stock integer"}</li>
	 * <li>{@code "UPDATE Product SET stock = someSophisticatedExpression"}</li>
	 * </ul>
	 * So far this is ok.
	 * Now someone might want to make a better world by generating the first statement
	 * via some api:
	 * <ul>
	 * <li>{@code SQLGenerator.addColumn(Product.TYPE, Product.stock)}</li>
	 * <li>{@code "UPDATE Product SET stock = someSophisticatedExpression"}</li>
	 * </ul>
	 * This looks nice at the first glance, but it introduces a severe problem:
	 * Two weeks later another developer renames {@code stock} into {@code supply}
	 * and writes another schema patch.
	 * <ul>
	 * <li>{@code "ALTER TABLE Product RENAME COLUMN stock TO supply"}</li>
	 * </ul>
	 * However, by renaming stock, the first patch changes to
	 * <ul>
	 * <li>{@code SQLGenerator.addColumn(Product.TYPE, Product.supply)}</li>
	 * <li>{@code "UPDATE Product SET stock = someSophisticatedExpression"}</li>
	 * </ul>
	 * The second statement will fail,
	 * because there is no column {@code stock} but only {@code supply}.
	 */
	protected abstract String[] computeBody();

	public final String[] getBody()
	{
		return Arrays.copyOf(getBodyInternal());
	}

	private String[] body = null;

	private String[] getBodyInternal()
	{
		if(body!=null)
			return body;

		final String[] body =
				requireNonEmptyAndCopy(computeBody(), "body");
		for(int i = 0; i<body.length; i++)
		{
			try
			{
				SchemaPatchRun.sql.check(body[i]);
			}
			catch(final ConstraintViolationException e)
			{
				throw new IllegalArgumentException("body[" + i + "]: " + e.getMessageWithoutFeature(), e);
			}
		}

		this.body = body;
		return body;
	}

	@Override
	public final void run(final JobContext ctx)
	{
		final String id = getID();
		final String[] body = getBodyInternal();
		final Model model = SchemaPatchRun.TYPE.getModel();

		if(logger.isInfoEnabled())
			logger.info("executing {} statements for {}", body.length, id);

		try(Connection connection = SchemaInfo.newConnection(model))
		{
			for(int position = 0; position<body.length; position++)
			{
				final String sql = body[position];
				if(logger.isInfoEnabled())
					logger.info("{}/{}: {}", position+1, body.length, sql);
				if(ctx.supportsMessage())
					ctx.setMessage("SchemaPatch " + (position+1) + '/' + body.length + ' ' + sql);
				final long start = nanoTime();
				final int rows = execute(connection, sql);
				final long elapsed = toMillies(nanoTime(), start);

				try(TransactionTry tx = model.startTransactionTry(SchemaPatch.class.getName() + ' ' + id + ' ' + (position+1) + '/' + body.length))
				{
					//noinspection ResultOfObjectAllocationIgnored persistent object
					new SchemaPatchRun(id, position, sql, rows, elapsed);
					tx.commit();
				}
				ctx.incrementProgress();
			}
		}
		catch(final SQLException e)
		{
			throw new RuntimeException(e);
		}

		model.clearCache();
	}

	private static int execute(
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
