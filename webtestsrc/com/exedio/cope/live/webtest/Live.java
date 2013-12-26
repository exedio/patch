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

package com.exedio.cope.live.webtest;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.exedio.cope.live.AbstractSession;
import com.exedio.cope.live.LiveServlet;

public final class Live extends LiveServlet
{
	private static final long serialVersionUID = 1l;

	public Live()
	{
		super(EditedServlet.model);
	}

	private static class Session extends AbstractSession implements Serializable
	{
		private static final long serialVersionUID = 1l;

		private final String user;
		private final boolean nameIsNull;

		Session(final String user)
		{
			this.user = user;
			this.nameIsNull = "noname".equals(user);
		}

		public String getName()
		{
			assertTransaction();

			return nameIsNull ? null : "getName(" + user + ')';
		}

		@Override
		public String getHome(final HttpServletRequest request)
		{
			assertTransaction();

			return
				"abstractHome".equals(user)
				? super.getHome(request)
				: request.getContextPath() + "/edited";
		}

		@Override
		public String toString()
		{
			return "toString(" + user + ')';
		}
	}

	@Override
	protected Session login(final String user, final String password)
	{
		assertTransaction();

		if(password.equals(user+"1234"))
			return new Session(user);

		return null;
	}

	static final void assertTransaction()
	{
		if(!EditedServlet.model.hasCurrentTransaction())
			throw new IllegalStateException("transaction required");
	}
}
