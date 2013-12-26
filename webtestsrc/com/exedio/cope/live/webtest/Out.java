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

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.StringField;
import com.exedio.cope.live.Session;
import com.exedio.cope.pattern.MapField;
import com.exedio.cope.pattern.Media;
import com.exedio.cope.pattern.MediaFilter;
import com.exedio.cops.BodySender;
import com.exedio.cops.CopsServlet;

abstract class Out
{
	protected final StringBuilder bf;
	protected final HttpServletRequest request;
	private final HttpServletResponse response;

	Out(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		this.bf = new StringBuilder();
		this.request = request;
		this.response = response;
	}

	final void write(final String s)
	{
		bf.append(s);
	}

	final void write(final Date d)
	{
		bf.append(d);
	}

	final void write(final Session s)
	{
		bf.append(s);
	}

	final void write(final boolean b)
	{
		bf.append(b);
	}

	final void write(final int i)
	{
		bf.append(i);
	}

	abstract boolean isEditorLoggedIn();
	abstract boolean isEditorBordersEnabled();
	abstract Session getEditorSession();
	abstract void writeEditorHead();
	abstract void writeEditorBar();
	abstract void write(final String s, final StringField feature, final Item item);
	abstract <K> void write(final String s, final MapField<K, String> feature, final Item item, final K key);
	abstract void edit(final Media feature, final Item item);
	abstract void edit(final MediaFilter feature, final Item item);
	abstract void swapIcon(final IntegerField feature, final Item item);
	abstract void swapText(final IntegerField feature, final Item item);

	final void sendBody() throws IOException
	{
		BodySender.send(response, bf, CopsServlet.UTF8);
	}
}
