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
import com.exedio.cope.live.Session;
import com.exedio.cope.live.util.LiveFilter;
import com.exedio.cope.pattern.MapField;
import com.exedio.cope.pattern.Media;
import com.exedio.cope.pattern.MediaFilter;

final class OutFilter extends Out
{
	OutFilter(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		super(request, response);
	}

	@Override
	boolean isEditorLoggedIn()
	{
		return LiveFilter.isLoggedIn();
	}

	@Override
	boolean isEditorBordersEnabled()
	{
		return LiveFilter.isBordersEnabled();
	}

	@Override
	Session getEditorSession()
	{
		return LiveFilter.getSession();
	}

	@Override
	void writeEditorHead()
	{
		bf.append(LiveFilter.getHead());
	}

	@Override
	void writeEditorBar()
	{
		bf.append(LiveFilter.getBar());
	}

	@Override
	void write(final String s, final StringField feature, final Item item)
	{
		bf.append(LiveFilter.edit(s, feature, item));
	}

	@Override
	<K> void write(final String s, final MapField<K, String> feature, final Item item, final K key)
	{
		bf.append(LiveFilter.edit(s, feature, item, key));
	}

	@Override
	void edit(final Media feature, final Item item)
	{
		bf.append(LiveFilter.edit(feature, item));
	}

	@Override
	void edit(final MediaFilter feature, final Item item)
	{
		bf.append(LiveFilter.edit(feature, item));
	}

	@Override
	void swapIcon(final IntegerField feature, final Item item)
	{
		bf.append(LiveFilter.edit(feature, item, request.getContextPath() + "/previous.png"));
	}

	@Override
	void swapText(final IntegerField feature, final Item item)
	{
		bf.append(LiveFilter.edit(feature, item, null));
	}
}
