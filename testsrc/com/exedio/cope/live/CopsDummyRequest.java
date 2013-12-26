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

import java.io.BufferedReader;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

final class CopsDummyRequest implements HttpServletRequest
{
	public String getContextPath()
	{
		return "/contextPath";
	}

	public String getServletPath()
	{
		return "/servletPath";
	}

	public String getAuthType()
	{
		throw new RuntimeException();
	}

	public Cookie[] getCookies()
	{
		throw new RuntimeException();
	}

	public long getDateHeader(final String arg0)
	{
		throw new RuntimeException();
	}

	public String getHeader(final String arg0)
	{
		throw new RuntimeException();
	}

	public Enumeration getHeaderNames()
	{
		throw new RuntimeException();
	}

	public Enumeration getHeaders(final String arg0)
	{
		throw new RuntimeException();
	}

	public int getIntHeader(final String arg0)
	{
		throw new RuntimeException();
	}

	public String getMethod()
	{
		throw new RuntimeException();
	}

	public String getPathInfo()
	{
		throw new RuntimeException();
	}

	public String getPathTranslated()
	{
		throw new RuntimeException();
	}

	public String getQueryString()
	{
		throw new RuntimeException();
	}

	public String getRemoteUser()
	{
		throw new RuntimeException();
	}

	public String getRequestURI()
	{
		throw new RuntimeException();
	}

	public StringBuffer getRequestURL()
	{
		throw new RuntimeException();
	}

	public String getRequestedSessionId()
	{
		throw new RuntimeException();
	}

	public HttpSession getSession()
	{
		throw new RuntimeException();
	}

	public HttpSession getSession(final boolean arg0)
	{
		throw new RuntimeException();
	}

	public Principal getUserPrincipal()
	{
		throw new RuntimeException();
	}

	public boolean isRequestedSessionIdFromCookie()
	{
		throw new RuntimeException();
	}

	public boolean isRequestedSessionIdFromURL()
	{
		throw new RuntimeException();
	}

	@Deprecated
	public boolean isRequestedSessionIdFromUrl()
	{
		throw new RuntimeException();
	}

	public boolean isRequestedSessionIdValid()
	{
		throw new RuntimeException();
	}

	public boolean isUserInRole(final String arg0)
	{
		throw new RuntimeException();
	}

	public Object getAttribute(final String arg0)
	{
		throw new RuntimeException();
	}

	public Enumeration getAttributeNames()
	{
		throw new RuntimeException();
	}

	public String getCharacterEncoding()
	{
		throw new RuntimeException();
	}

	public int getContentLength()
	{
		throw new RuntimeException();
	}

	public String getContentType()
	{
		throw new RuntimeException();
	}

	public ServletInputStream getInputStream()
	{
		throw new RuntimeException();
	}

	public String getLocalAddr()
	{
		throw new RuntimeException();
	}

	public String getLocalName()
	{
		throw new RuntimeException();
	}

	public int getLocalPort()
	{
		throw new RuntimeException();
	}

	public Locale getLocale()
	{
		throw new RuntimeException();
	}

	public Enumeration getLocales()
	{
		throw new RuntimeException();
	}

	public String getParameter(final String arg0)
	{
		throw new RuntimeException();
	}

	public Map getParameterMap()
	{
		throw new RuntimeException();
	}

	public Enumeration getParameterNames()
	{
		throw new RuntimeException();
	}

	public String[] getParameterValues(final String arg0)
	{
		throw new RuntimeException();
	}

	public String getProtocol()
	{
		throw new RuntimeException();
	}

	public BufferedReader getReader()
	{
		throw new RuntimeException();
	}

	@Deprecated
	public String getRealPath(final String arg0)
	{
		throw new RuntimeException();
	}

	public String getRemoteAddr()
	{
		throw new RuntimeException();
	}

	public String getRemoteHost()
	{
		throw new RuntimeException();
	}

	public int getRemotePort()
	{
		throw new RuntimeException();
	}

	public RequestDispatcher getRequestDispatcher(final String arg0)
	{
		throw new RuntimeException();
	}

	public String getScheme()
	{
		throw new RuntimeException();
	}

	public String getServerName()
	{
		throw new RuntimeException();
	}

	public int getServerPort()
	{
		throw new RuntimeException();
	}

	public boolean isSecure()
	{
		throw new RuntimeException();
	}

	public void removeAttribute(final String arg0)
	{
		throw new RuntimeException();
	}

	public void setAttribute(final String arg0, final Object arg1)
	{
		throw new RuntimeException();
	}

	public void setCharacterEncoding(final String arg0)
	{
		throw new RuntimeException();
	}
}
