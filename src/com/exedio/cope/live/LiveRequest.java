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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.exedio.cope.Cope;
import com.exedio.cope.Feature;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.StringField;
import com.exedio.cope.misc.Computed;
import com.exedio.cope.pattern.MapField;
import com.exedio.cope.pattern.Media;
import com.exedio.cope.pattern.MediaFilter;
import com.exedio.cope.util.XMLEncoder;

public final class LiveRequest
{
	public static LiveRequest get(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		final HttpSession session = request.getSession(false);
		if(session==null)
			return null;

		final Object anchor = session.getAttribute(LoginServlet.ANCHOR);
		if(anchor==null)
			return null;

		return new LiveRequest(request, response, (Anchor)anchor);
	}

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Anchor anchor;
	private HashMap<IntegerField, Item> positionItems = null;

	private LiveRequest(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Anchor anchor)
	{
		this.request = request;
		this.response = response;
		this.anchor = anchor;

		assert request!=null;
		assert response!=null;
		assert anchor!=null;
	}

	Item registerPositionItem(final IntegerField feature, final Item item)
	{
		final Integer next = feature.get(item);
		if(next==null)
			return null;

		if(positionItems==null)
			positionItems = new HashMap<IntegerField, Item>();

		final Item result = positionItems.put(feature, item);
		if(result==null)
			return null;

		final Integer previous = feature.get(result);
		return (previous!=null && previous.intValue()<next.intValue()) ? result : null;
	}

	public boolean isBordersEnabled()
	{
		return anchor.borders;
	}

	public Session getSession()
	{
		return anchor.session;
	}

	private static void checkEdit(final Feature feature, final Item item)
	{
		if(feature==null)
			throw new NullPointerException("feature");
		if(item==null)
			throw new NullPointerException("item");
		if(!feature.getType().isAssignableFrom(item.getCopeType()))
			throw new IllegalArgumentException("item " + item.getCopeID() + " does not belong to type of feature " + feature.getID());
	}

	private static <K> Item getItem(final MapField<K, String> feature, final K key, final Item item)
	{
		return
				feature.getRelationType().searchSingletonStrict(
						feature.getKey().equal(key).and(
						Cope.equalAndCast(feature.getParent(), item)));
	}

	public <K> String edit(final String content, final MapField<K, String> feature, final Item item, final K key)
	{
		checkEdit(feature, item);
		if(feature.isAnnotationPresent(Computed.class))
			return content + "<img src=\"" + anchor.errorButtonURL + "\" title=\"ignoring @Computed MapField " + feature + "\">";

		return edit(
				content,
				(StringField)feature.getValue(),
				getItem(feature, key, item));
	}

	public String edit(final String content, final StringField feature, final Item item)
	{
		checkEdit(feature, item);
		if(feature.isFinal())
			throw new IllegalArgumentException("feature " + feature.getID() + " must not be final");
		if(feature.isAnnotationPresent(Computed.class))
			return content + "<img src=\"" + anchor.errorButtonURL + "\" title=\"ignoring @Computed StringField " + feature + "\">";

		if(!anchor.borders)
		{
			final String modification = anchor.getModification(feature, item);
			return (modification!=null) ? modification : content;
		}

		final boolean block = feature.getMaximumLength()>StringField.DEFAULT_MAXIMUM_LENGTH;
		final String savedContent = feature.get(item);
		final String pageContent;
		final String editorContent;
		final boolean modifiable;
		if(content!=null ? content.equals(savedContent) : (savedContent==null))
		{
			modifiable = true;
			final String modification = anchor.getModification(feature, item);
			if(modification!=null)
				pageContent = editorContent = modification;
			else
				pageContent = editorContent = savedContent; // equals content anyway
		}
		else
		{
			modifiable = false;
			pageContent = content;
			editorContent = savedContent;
		}

		final String tag = block ? "div" : "span";
		final String editorContentEncoded = XMLEncoder.encode(editorContent);
		final StringBuilder bf = new StringBuilder();
		bf.append('<').
			append(tag).
			append(
				" class=\"" + Bar.CSS_EDIT + "\"" +
				" onclick=\"" +
					"return " + (block ? Bar.EDIT_METHOD_AREA : Bar.EDIT_METHOD_LINE) + "(this,'").
						append(feature.getID()).
						append("','").
						append(item.getCopeID()).
						append("','").
						append(block ? editorContentEncoded.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r") : editorContentEncoded).
					append("'," + modifiable + ");\"").
			append('>').
			append(pageContent).
			append("</").
			append(tag).
			append('>');

		return bf.toString();
	}

	public String edit(final Media feature, final Item item)
	{
		return edit(feature, item, true);
	}

	private String edit(final Media feature, final Item item, final boolean modifiable)
	{
		checkEdit(feature, item);
		if(feature.isFinal())
			throw new IllegalArgumentException("feature " + feature.getID() + " must not be final");

		final String modificationURL = modifiable ? anchor.getModificationURL(feature, item, response) : null;
		final String onload =
			modificationURL!=null
				? (" onload=\"this.src='" + XMLEncoder.encode(response.encodeURL(modificationURL)) + "';\"")
				: "";

		if(!anchor.borders)
			return onload;

		final StringBuilder bf = new StringBuilder();
		bf.append(
				" class=\"" + Bar.CSS_EDIT + "\"" +
				onload +
				" onclick=\"" +
					"return " + Bar.EDIT_METHOD_FILE + "(this,'").
						append(feature.getID()).
						append("','").
						append(item.getCopeID()).
						append("','").
						append(XMLEncoder.encode(feature.getURL(item))).
					append("'," + modifiable + ");\"");

		return bf.toString();
	}

	public String edit(final MediaFilter feature, final Item item)
	{
		if(!anchor.borders)
			return "";

		checkEdit(feature, item);

		return edit(feature.getSource(), item, false);
	}

	public String swap(final IntegerField feature, final Item item, final String buttonURL)
	{
		if(!anchor.borders)
			return "";

		checkEdit(feature, item);
		if(feature.isFinal())
			throw new IllegalArgumentException("feature " + feature.getID() + " must not be final");
		if(feature.isAnnotationPresent(Computed.class))
			return "<img src=\"" + anchor.errorButtonURL + "\" title=\"ignoring @Computed IntegerField " + feature + "\">";

		final Item previousItem = registerPositionItem(feature, item);
		if(previousItem==null)
			return "";

		return
			"<form action=\"" + action() + "\" method=\"POST\" class=\"" + Bar.CSS_SWAP + "\">" +
				"<input type=\"hidden\" name=\"" + Bar.REFERER   + "\" value=\"" + referer()                + "\">" +
				"<input type=\"hidden\" name=\"" + Bar.FEATURE   + "\" value=\"" + feature.getID()          + "\">" +
				"<input type=\"hidden\" name=\"" + Bar.ITEM_FROM + "\" value=\"" + previousItem.getCopeID() + "\">" +
				"<input type=\"hidden\" name=\"" + Bar.ITEM      + "\" value=\"" + item.getCopeID()         + "\">" +
				(
					buttonURL!=null
					? ("<input type=\"image\" src=\"" + buttonURL + "\" alt=\"Swap with previous item\">")
					: ("<input type=\"submit\" value=\"Up\">")
				) +
			"</form>";
	}

	public String getHead()
	{
		final StringBuilder out = new StringBuilder();
		Bar_Jspm.writeHead(out, anchor.borders);
		return out.toString();
	}

	public String getBar()
	{
		final ArrayList<Target> targets = new ArrayList<Target>();
		targets.add(TargetLive.INSTANCE);
		if(anchor.draftsEnabled)
		{
			final List<Draft> drafts = Draft.TYPE.search(null, Draft.date, true);
			for(final Draft draft : drafts)
				targets.add(new TargetDraft(draft));
			targets.add(TargetNewDraft.INSTANCE);
		}
		final boolean borders = anchor.borders;
		final StringBuilder out = new StringBuilder();
		Bar_Jspm.write(out,
				anchor.getTarget(),
				targets,
				action(),
				referer(),
				response.encodeURL(anchor.servletPath + Management.PATH_INFO),
				borders,
				borders ? Bar.BORDERS_OFF : Bar.BORDERS_ON,
				borders ? anchor.borderDisableButtonURL : anchor.borderEnableButtonURL,
				anchor.hideButtonURL,
				anchor.closeButtonURL,
				anchor.getModificationsCount(),
				anchor.sessionName);
		return out.toString();
	}

	private String action()
	{
		return response.encodeURL(anchor.servletPath);
	}

	private String referer()
	{
		final StringBuilder bf = new StringBuilder();
		bf.append(request.getContextPath());
		bf.append(request.getServletPath());

		final String pathInfo = request.getPathInfo();
		if(pathInfo!=null)
			bf.append(pathInfo);

		final String queryString = request.getQueryString();
		if(queryString!=null)
			bf.append('?').append(queryString);

		return bf.toString();
	}
}
