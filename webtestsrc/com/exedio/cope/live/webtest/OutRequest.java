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

final class OutRequest extends Out
{
	private final LiveRequest live;

	OutRequest(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		super(request, response);
		this.live = LiveRequest.get(request, response);
	}

	@Override
	boolean isEditorLoggedIn()
	{
		return live!=null;
	}

	@Override
	boolean isEditorBordersEnabled()
	{
		return live!=null && live.isBordersEnabled();
	}

	@Override
	Session getEditorSession()
	{
		return live!=null ? live.getSession() : null;
	}

	@Override
	void writeEditorHead()
	{
		if(live!=null)
			bf.append(live.getHead());
	}

	@Override
	void writeEditorBar()
	{
		if(live!=null)
			bf.append(live.getBar());
	}

	@Override
	void write(final String s, final StringField feature, final Item item)
	{
		bf.append(live!=null ? live.edit(s, feature, item) : s);
	}

	@Override
	<K> void write(final String s, final MapField<K, String> feature, final Item item, final K key)
	{
		bf.append(live!=null ? live.edit(s, feature, item, key) : s);
	}

	@Override
	void edit(final Media feature, final Item item)
	{
		if(live!=null)
			bf.append(live.edit(feature, item));
	}

	@Override
	void edit(final MediaFilter feature, final Item item)
	{
		if(live!=null)
			bf.append(live.edit(feature, item));
	}

	@Override
	void swapIcon(final IntegerField feature, final Item item)
	{
		if(live!=null)
			bf.append(live.swap(feature, item, request.getContextPath() + "/previous.png"));
	}

	@Override
	void swapText(final IntegerField feature, final Item item)
	{
		if(live!=null)
			bf.append(live.swap(feature, item, null));
	}
}
