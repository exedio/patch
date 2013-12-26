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
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.exedio.cope.Item;
import com.exedio.cope.Model;
import com.exedio.cope.NoSuchIDException;
import com.exedio.cope.pattern.Media;

final class MediaServlet
{
	private final Model model;
	private final LiveServlet servlet;

	MediaServlet(final Model model, final LiveServlet servlet)
	{
		this.model = model;
		this.servlet = servlet;
	}

	private void startTransaction(final String name)
	{
		servlet.startTransaction(name);
	}

	static final String PATH_INFO = "media";
	private static final String FEATURE = "f";
	private static final String ITEM    = "i";

	static String makeURL(final Media feature, final Item item)
	{
		return
			PATH_INFO +
			'?' + MediaServlet.FEATURE + '=' + feature.getID() +
			'&' + MediaServlet.ITEM + '=' + item.getCopeID();
	}

	void doRequest(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Anchor anchor)
	{
		final String featureID = request.getParameter(FEATURE);
		if(featureID==null)
			throw new NullPointerException();
		final Media feature = (Media)model.getFeature(featureID);
		if(feature==null)
			throw new NullPointerException(featureID);

		final String itemID = request.getParameter(ITEM);
		if(itemID==null)
			throw new NullPointerException();

		final Item item;
		try
		{
			startTransaction("media(" + featureID + ',' + itemID + ')');
			item = model.getItem(itemID);
			model.commit();
		}
		catch(final NoSuchIDException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			model.rollbackIfNotCommitted();
		}

		final FileItem fi = anchor.getModification(feature, item);
		if(fi==null)
			throw new NullPointerException(featureID + '-' + itemID);
		response.setContentType(fi.getContentType());
		response.setContentLength((int)fi.getSize());

		InputStream in = null;
		ServletOutputStream out = null;
		try
		{
			in  = fi.getInputStream();
			out = response.getOutputStream();

			final byte[] b = new byte[20*1024];
			for(int len = in.read(b); len>=0; len = in.read(b))
				out.write(b, 0, len);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(in!=null)
			{
				try
				{
					in.close();
				}
				catch(final IOException e)
				{
					e.printStackTrace();
				}
			}
			if(out!=null)
			{
				try
				{
					out.close();
				}
				catch(final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
