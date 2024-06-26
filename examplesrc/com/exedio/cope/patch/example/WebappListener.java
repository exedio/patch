/*
 * Copyright (C) 2004-2009  exedio GmbH (www.exedio.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.exedio.cope.patch.example;

import static com.exedio.cope.misc.ConnectToken.setProperties;

import com.exedio.cope.ConnectProperties;
import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.util.servlet.ServletSource;
import com.exedio.dsmf.Node;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class WebappListener implements ServletContextListener
{
	@Override
	public void contextInitialized(final ServletContextEvent sce)
	{
		final ServletContext ctx = sce.getServletContext();
		setProperties(Main.model, ConnectProperties.create(ServletSource.create(ctx)));
		// All example patches act on tables which are not part of the model, so we can create the model here
		createSchemaIfInvalid();
	}

	/**
	 *  Note: we suppress the revisions during initialization
	 */
	@SuppressWarnings("try") // ConnectToken#issue
	private static void createSchemaIfInvalid()
	{
		RevisionsFactory.INSTANCE.setSuppress(true);
		try(ConnectToken ignored = ConnectToken.issue(Main.model, "renew schema"))
		{
			final boolean schemaHasIssues = Main.model.getVerifiedSchema().getCumulativeColor() != Node.Color.OK;
			if (schemaHasIssues)
			{
				Main.model.tearDownSchema();
				Main.model.createSchema();
			}
		}
		finally
		{
			RevisionsFactory.INSTANCE.setSuppress(false);
		}
	}
}
