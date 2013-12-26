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

package com.exedio.cope.live.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.StringField;
import com.exedio.cope.live.LiveRequest;
import com.exedio.cope.live.Session;
import com.exedio.cope.pattern.MapField;
import com.exedio.cope.pattern.Media;
import com.exedio.cope.pattern.MediaFilter;

public final class LiveFilter implements Filter
{
	public final void init(final FilterConfig config)
	{
		// do nothing
	}

	public final void destroy()
	{
		// do nothing
	}

	public final void doFilter(
			final ServletRequest request,
			final ServletResponse response,
			final FilterChain chain)
	throws IOException, ServletException
	{
		if(!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
		{
			chain.doFilter(request, response);
			return;
		}

		final LiveRequest liveRequest = LiveRequest.get((HttpServletRequest)request, (HttpServletResponse)response);
		if(liveRequest!=null)
		{
			try
			{
				threadLocal.set(liveRequest);
				chain.doFilter(request, response);
			}
			finally
			{
				threadLocal.remove();
			}
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

	private static final ThreadLocal<LiveRequest> threadLocal = new ThreadLocal<LiveRequest>();

	public static final boolean isLoggedIn()
	{
		return threadLocal.get()!=null;
	}

	public static final boolean isBordersEnabled()
	{
		final LiveRequest tl = threadLocal.get();
		return tl!=null && tl.isBordersEnabled();
	}

	public static final Session getSession()
	{
		final LiveRequest tl = threadLocal.get();
		return tl!=null ? tl.getSession() : null;
	}

	public static final <K> String edit(final String content, final MapField<K, String> feature, final Item item, final K key)
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return content;

		return tl.edit(content, feature, item, key);
	}

	public static final String edit(final String content, final StringField feature, final Item item)
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return content;

		return tl.edit(content, feature, item);
	}

	public static final String edit(final Media feature, final Item item)
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return "";

		return tl.edit(feature, item);
	}

	public static final String edit(final MediaFilter feature, final Item item)
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return "";

		return tl.edit(feature, item);
	}

	public static final String edit(final IntegerField feature, final Item item, final String buttonURL)
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return "";

		return tl.swap(feature, item, buttonURL);
	}

	public static final String getHead()
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return "";

		return tl.getHead();
	}

	public static final String getBar()
	{
		final LiveRequest tl = threadLocal.get();
		if(tl==null)
			return "";

		return tl.getBar();
	}
}
