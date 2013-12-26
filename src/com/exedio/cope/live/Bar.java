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
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.exedio.cope.Feature;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.Model;
import com.exedio.cope.NoSuchIDException;
import com.exedio.cope.StringField;
import com.exedio.cope.pattern.History;
import com.exedio.cope.pattern.Media;
import com.exedio.cops.Cop;

final class Bar
{
	private final Model model;
	private final LiveServlet servlet;

	Bar(final Model model, final LiveServlet servlet)
	{
		this.model = model;
		this.servlet = servlet;
	}

	private void startTransaction(final String name)
	{
		servlet.startTransaction(name);
	}

	static final String AVOID_COLLISION = "contentEditorBar823658617";

	static final String CSS_EDIT        = "copeLive-edit";
	static final String CSS_EDIT_ACTIVE = "copeLive-editActive";
	static final String CSS_SWAP        = "copeLive-swap";

	static final String EDIT_METHOD_LINE = AVOID_COLLISION + "line";
	static final String EDIT_METHOD_FILE = AVOID_COLLISION + "file";
	static final String EDIT_METHOD_AREA = AVOID_COLLISION + "area";

	static final String REFERER = "referer";
	static final String BORDERS_ON  = "borders.on";
	static final String BORDERS_OFF = "borders.off";
	static final String CLOSE = "close";
	static final String SWITCH_TARGET = "target.switch";
	static final String SAVE_TARGET   = "target.save";

	static final String FEATURE = "feature";
	static final String ITEM    = "item";
	static final String TEXT    = "text";
	static final String FILE    = "file";
	static final String ITEM_FROM = "itemPrevious";
	static final String PUBLISH_NOW = "publishNow";

	private static final String CLOSE_IMAGE       = CLOSE       + ".x";
	private static final String BORDERS_ON_IMAGE  = BORDERS_ON  + ".x";
	private static final String BORDERS_OFF_IMAGE = BORDERS_OFF + ".x";

	@SuppressWarnings("deprecation")
	private static boolean isMultipartContent(final HttpServletRequest request)
	{
		return ServletFileUpload.isMultipartContent(request);
	}

	void doRequest(
			final HttpServletRequest request,
			final HttpSession httpSession,
			final HttpServletResponse response,
			final Anchor anchor)
	throws IOException
	{
		if(!Cop.isPost(request))
		{
			try
			{
				startTransaction("redirectHome");
				anchor.redirectHome(request, response);
				model.commit();
			}
			finally
			{
				model.rollbackIfNotCommitted();
			}
			return;
		}

		final String referer;

		if(isMultipartContent(request))
		{
			final HashMap<String, String> fields = new HashMap<String, String>();
			final HashMap<String, FileItem> files = new HashMap<String, FileItem>();
			final FileItemFactory factory = new DiskFileItemFactory();
			final ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding(UTF8);
			try
			{
				for(final Iterator<?> i = upload.parseRequest(request).iterator(); i.hasNext(); )
				{
					final FileItem item = (FileItem)i.next();
					if(item.isFormField())
						fields.put(item.getFieldName(), item.getString(UTF8));
					else
						files.put(item.getFieldName(), item);
				}
			}
			catch(final FileUploadException e)
			{
				throw new RuntimeException(e);
			}

			final String featureID = fields.get(FEATURE);
			if(featureID==null)
				throw new NullPointerException();

			final Media feature = (Media)model.getFeature(featureID);
			if(feature==null)
				throw new NullPointerException(featureID);

			final String itemID = fields.get(ITEM);
			if(itemID==null)
				throw new NullPointerException();

			final FileItem file = files.get(FILE);

			try
			{
				startTransaction("publishFile(" + featureID + ',' + itemID + ')');

				final Item item = model.getItem(itemID);

				if(fields.get(PUBLISH_NOW)!=null)
				{
					for(final History history : History.getHistories(item.getCopeType()))
					{
						final History.Event event = history.createEvent(item, anchor.getHistoryAuthor(), false);
						event.createFeature(
								feature, feature.getName(),
								feature.isNull(item) ? null : ("file type=" + feature.getContentType(item) + " size=" + feature.getLength(item)),
								"file name=" + file.getName() + " type=" + file.getContentType() + " size=" + file.getSize());
					}

					// TODO use more efficient setter with File or byte[]
					feature.set(item, file.getInputStream(), file.getContentType());
				}
				else
				{
					anchor.modify(file, feature, item);
				}

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

			referer = fields.get(REFERER);
		}
		else // isMultipartContent
		{
			if(request.getParameter(BORDERS_ON)!=null || request.getParameter(BORDERS_ON_IMAGE)!=null)
			{
				anchor.borders = true;
			}
			else if(request.getParameter(BORDERS_OFF)!=null || request.getParameter(BORDERS_OFF_IMAGE)!=null)
			{
				anchor.borders = false;
			}
			else if(request.getParameter(CLOSE)!=null || request.getParameter(CLOSE_IMAGE)!=null)
			{
				httpSession.removeAttribute(LoginServlet.ANCHOR);
			}
			else if(request.getParameter(SWITCH_TARGET)!=null)
			{
				anchor.setTarget(servlet.getTarget(request.getParameter(SWITCH_TARGET)));
			}
			else if(request.getParameter(SAVE_TARGET)!=null)
			{
				try
				{
					startTransaction("saveTarget");
					anchor.getTarget().save(anchor);
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
				anchor.notifyPublishedAll();
			}
			else
			{
				final String featureID = request.getParameter(FEATURE);
				if(featureID==null)
					throw new NullPointerException();

				final Feature featureO = model.getFeature(featureID);
				if(featureO==null)
					throw new NullPointerException(featureID);

				final String itemID = request.getParameter(ITEM);
				if(itemID==null)
					throw new NullPointerException();

				if(featureO instanceof StringField)
				{
					final StringField feature = (StringField)featureO;
					final String value = request.getParameter(TEXT);

					try
					{
						startTransaction("barText(" + featureID + ',' + itemID + ')');

						final Item item = model.getItem(itemID);

						if(request.getParameter(PUBLISH_NOW)!=null)
						{
							String v = value;
							if("".equals(v))
								v = null;
							for(final History history : History.getHistories(item.getCopeType()))
							{
								final History.Event event = history.createEvent(item, anchor.getHistoryAuthor(), false);
								event.createFeature(feature, feature.getName(), feature.get(item), v);
							}
							feature.set(item, v);
							anchor.notifyPublished(feature, item);
						}
						else
						{
							anchor.modify(value, feature, item);
						}

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
				}
				else
				{
					final IntegerField feature = (IntegerField)featureO;
					final String itemIDFrom = request.getParameter(ITEM_FROM);
					if(itemIDFrom==null)
						throw new NullPointerException();

					try
					{
						startTransaction("swapPosition(" + featureID + ',' + itemIDFrom + ',' + itemID + ')');

						final Item itemFrom = model.getItem(itemIDFrom);
						final Item itemTo   = model.getItem(itemID);

						final Integer positionFrom = feature.get(itemFrom);
						final Integer positionTo   = feature.get(itemTo);
						feature.set(itemFrom, feature.getMinimum());
						feature.set(itemTo,   positionFrom);
						feature.set(itemFrom, positionTo);

						for(final History history : History.getHistories(itemFrom.getCopeType()))
						{
							final History.Event event = history.createEvent(itemFrom, anchor.getHistoryAuthor(), false);
							event.createFeature(feature, feature.getName(), positionFrom, positionTo);
						}
						for(final History history : History.getHistories(itemTo.getCopeType()))
						{
							final History.Event event = history.createEvent(itemTo, anchor.getHistoryAuthor(), false);
							event.createFeature(feature, feature.getName(), positionTo, positionFrom);
						}

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
				}
			}

			referer = request.getParameter(REFERER);
		}

		if(referer!=null)
			response.sendRedirect(response.encodeRedirectURL(referer));
	}
}
