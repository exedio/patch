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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.exedio.cope.Model;
import com.exedio.cope.Revisions;
import com.exedio.cope.TypeSet;
import com.exedio.cope.live.Draft;
import com.exedio.cope.live.Drafts;
import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.pattern.Media;

public final class EditedServlet extends HttpServlet
{
	private static final long serialVersionUID = 1l;

	static final String ENCODING = "utf-8";

	public static final Model model = new Model(
			(Revisions)null,
			new TypeSet[]{Drafts.types},
			EditedItem.TYPE
			//, Draft.TYPE, com.exedio.cope.live.DraftItem.TYPE
	);

	static
	{
		model.enableSerialization(EditedServlet.class, "model");
	}

	private ConnectToken connectToken = null;

	@Override
	public void init() throws ServletException
	{
		super.init();

		connectToken = ConnectToken.issue(model, getClass().getName());
		model.createSchema();
		try
		{
			model.startTransaction("create sample data");
			final EditedItem i1 = createItem(0, "osorno.png", "image/png");
			final EditedItem i2 = createItem(1, "tree.jpg", "image/jpeg");
			createItem(2, "tree.jpg", "image/jpeg");
			final Draft d1 = new Draft("jim", "Jim Smith", "Many New Values");
			d1.addItem(EditedItem.field, i1, "item0New");
			d1.addItem(EditedItem.field, i2, "item1New");
			d1.addItem(EditedItem.map, 1, i2, "item1map1New");
			d1.addItem(EditedItem.map, 2, i2, "item1map2New");
			final Draft d2 = new Draft("john", null, "A Single New Value");
			d2.addItem(EditedItem.field, i1, "item0NewSingle");
			model.commit();
		}
		finally
		{
			model.rollbackIfNotCommitted();
		}
	}

	private static final EditedItem createItem(
			final int number,
			final String image, final String imageContentType)
	{
		final EditedItem item1 = new EditedItem(
				number,
				"item" + number,
				"item" + number + "fieldBlock\nsecond line\r\nthird line",
				Media.toValue(EditedServlet.class.getResourceAsStream(image), imageContentType),
				number+100,
				"COMPUTED" + number,
				Media.toValue(EditedServlet.class.getResourceAsStream(image), imageContentType));
		item1.setMap(1, "item" + number + "map1");
		item1.setMap(2, "item" + number + "map2");
		item1.setMapBlock(1, "item" + number + "map1Block\nsecond line\r\nthird line");
		item1.setComputedMap(1, "COMPUTED" + number + "map");
		return item1;
	}

	@Override
	public void destroy()
	{
		model.dropSchema();
		connectToken.returnIt();
		connectToken = null;
		super.destroy();
	}

	@Override
	protected void doGet(
			final HttpServletRequest request,
			final HttpServletResponse response)
	throws IOException
	{
		request.setCharacterEncoding(ENCODING);
		response.setContentType("text/html; charset="+ENCODING);

		response.addHeader("Cache-Control", "no-cache");
		response.addHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");
		response.addHeader("Cache-Control", "must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", System.currentTimeMillis());

		final Out out = new OutRequest(request, response);
		try
		{
			model.startTransaction("EditedServlet");
			EditedServlet_Jspm.write(out,
					response,
					response.encodeURL(request.getContextPath() + "/edited/livePath"),
					response.encodeURL(request.getContextPath() + "/edited/livePath/"),
					response.encodeURL(request.getContextPath() + "/edited/duplicate.html"),
					EditedItem.TYPE.search(null, EditedItem.position, true));
			model.commit();
			response.setStatus(HttpServletResponse.SC_OK);
			out.sendBody();
		}
		finally
		{
			model.rollbackIfNotCommitted();
		}
	}

	@Override
	protected void doPost(
			final HttpServletRequest request,
			final HttpServletResponse response) throws IOException
	{
		doGet(request, response);
	}
}
