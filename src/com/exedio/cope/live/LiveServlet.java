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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.exedio.cope.Model;
import com.exedio.cope.NoSuchIDException;
import com.exedio.cope.misc.ConnectToken;
import com.exedio.cops.CopsServlet;
import com.exedio.cops.Resource;

public abstract class LiveServlet extends CopsServlet
{
	private static final long serialVersionUID = 1l;

	static final Resource logo = new Resource("logo.png");

	static final Resource borderDisable = new Resource("borderDisable.png");
	static final Resource borderEnable  = new Resource("borderEnable.png");
	static final Resource hide          = new Resource("hide.png");
	static final Resource close         = new Resource("close.png");
	static final Resource error         = new Resource("error.png");

	private final Model model;
	private final LoginServlet login;
	private final Bar bar;
	private final Management management;
	private final MediaServlet media;

	/**
	 * Subclasses must define a public no-args constructor
	 * providing the model.
	 */
	protected LiveServlet(final Model model)
	{
		if(model==null)
			throw new NullPointerException("model");
		final boolean draftsEnabled = model.contains(Drafts.types);

		this.model = model;
		this.login = new LoginServlet(model, draftsEnabled, this);
		this.bar = new Bar(model, this);
		this.management = new Management(model, draftsEnabled, this);
		this.media = new MediaServlet(model, this);
	}

	private ConnectToken connectToken = null;

	@Override
	public final void init(final ServletConfig config) throws ServletException
	{
		super.init(config);

		connectToken = ConnectToken.issue(model, getClass().getName());

		boolean mustReturn = true;
		try
		{
			initConnected();
			mustReturn = false;
		}
		finally
		{
			if(mustReturn)
			{
				connectToken.returnIt();
				connectToken = null;
			}
		}
		// DO NOT WRITE ANYTHING HERE, BUT IN initConnected ONLY
		// OTHERWISE ConnectTokens MAY BE LOST
	}

	private void initConnected()
	{
		model.reviseIfSupported();
	}

	final void startTransaction(final String name)
	{
		model.startTransaction(getClass().getName() + '#' + name);
	}

	@Override
	public final void destroy()
	{
		if(connectToken!=null)
		{
			connectToken.returnIt();
			connectToken = null;
		}

		super.destroy();
	}

	protected abstract Session login(String username, String password);

	@Override
	public final void doRequest(
			final HttpServletRequest request,
			final HttpServletResponse response)
	throws IOException
	{
		request.setCharacterEncoding(UTF8);
		final HttpSession httpSession = request.getSession(true);
		final Object anchor = httpSession.getAttribute(LoginServlet.ANCHOR);

		if(anchor==null)
			login.doRequest(request, httpSession, response);
		else
		{
			final String pathInfo = request.getPathInfo();
			if(('/' + Management.PATH_INFO).equals(pathInfo))
				management.doRequest(request, response, (Anchor)anchor);
			else if(('/' + MediaServlet.PATH_INFO).equals(pathInfo))
				media.doRequest(request, response, (Anchor)anchor);
			else
				bar.doRequest(request, httpSession, response, (Anchor)anchor);
		}
	}

	final Target getTarget(final String id)
	{
		if(TargetLive.ID.equals(id))
		{
			return TargetLive.INSTANCE;
		}
		else if(TargetNewDraft.ID.equals(id))
		{
			return TargetNewDraft.INSTANCE;
		}
		else
		{
			try
			{
				startTransaction("findDraft");
				return new TargetDraft((Draft)model.getItem(id));
			}
			catch(final NoSuchIDException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				model.rollbackIfNotCommitted();
			}
		}
	}
}
