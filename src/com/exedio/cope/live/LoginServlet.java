/*
 * Copyright (C) 2004-2009  exedio GmbH (www.exedio.com)
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

package com.exedio.cope.live;

import static com.exedio.cops.CopsServlet.UTF8;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.exedio.cope.Model;
import com.exedio.cops.BodySender;
import com.exedio.cops.Cop;

final class LoginServlet
{
	private final Model model;
	private final boolean draftsEnabled;
	private final LiveServlet servlet;

	LoginServlet(final Model model, final boolean draftsEnabled, final LiveServlet servlet)
	{
		this.model = model;
		this.draftsEnabled = draftsEnabled;
		this.servlet = servlet;
	}

	private void startTransaction(final String name)
	{
		servlet.startTransaction(name);
	}

	static final String ANCHOR = Session.class.getName();

	static final String SUBMIT   = "login.submit";
	static final String USERNAME = "login.username";
	static final String PASSWORD = "login.password";

	void doRequest(
			final HttpServletRequest request,
			final HttpSession httpSession,
			final HttpServletResponse response)
	throws IOException
	{
		assert httpSession!=null;
		response.setContentType("text/html; charset="+UTF8);
		if(Cop.isPost(request) && request.getParameter(SUBMIT)!=null)
		{
			final String username = request.getParameter(USERNAME);
			final String password = request.getParameter(PASSWORD);
			try
			{
				startTransaction("login");
				final Session session = servlet.login(username, password);
				if(session!=null)
				{
					final Anchor anchor =
						new Anchor(draftsEnabled, request, username, session, session.getName());
					httpSession.setAttribute(ANCHOR, anchor);
					anchor.redirectHome(request, response);
				}
				else
				{
					write(request, response, username);
				}
				model.commit();
			}
			finally
			{
				model.rollbackIfNotCommitted();
			}
		}
		else
		{
			write(request, response, null);
		}
	}

	private void write(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final String username)
	throws IOException
	{
		final StringBuilder out = new StringBuilder();
		Login_Jspm.write(out, request, response.encodeURL(request.getContextPath() + request.getServletPath()), LiveServlet.class.getPackage(), username);
		BodySender.send(response, out, UTF8);
	}
}
