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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.exedio.cope.Model;
import com.exedio.cope.NoSuchIDException;
import com.exedio.cops.BodySender;
import com.exedio.cops.Cop;

final class Management
{
	private final Model model;
	private final boolean draftsEnabled;
	private final LiveServlet servlet;

	Management(final Model model, final boolean draftsEnabled, final LiveServlet servlet)
	{
		this.model = model;
		this.draftsEnabled = draftsEnabled;
		this.servlet = servlet;
	}

	private void startTransaction(final String name)
	{
		servlet.startTransaction(name);
	}

	static final String PATH_INFO = "management";
	static final String MODIFICATION_PUBLISH = "modification.publish";
	static final String MODIFICATION_DISCARD = "modification.discard";
	static final String MODIFICATION_PERSIST = "modification.persist";
	static final String MODIFICATION_PERSIST_COMMENT = "modification.persistComment";
	static final String MODIFICATION_IDS = "id";
	static final String SAVE_TO_DRAFT = "draft.saveTo";
	static final String DRAFT_ID     = "draft.id";
	static final String DRAFT_LOAD   = "draft.load";
	static final String DRAFT_DELETE = "draft.delete";
	static final String DRAFT_NEW    = "draft.new";
	static final String DRAFT_COMMENT= "draft.comment";
	static final String TARGET_ID   = "target.id";
	static final String TARGET_OPEN = "target.load";

	void doRequest(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Anchor anchor)
	throws IOException
	{
		if(Cop.isPost(request))
		{
			final String[] idA = request.getParameterValues(MODIFICATION_IDS);
			final HashSet<String> ids = idA!=null ? new HashSet<String>(Arrays.asList(idA)) : null;
			if(request.getParameter(MODIFICATION_PUBLISH)!=null)
			{
				try
				{
					startTransaction("publishPreviews");
					for(final Iterator<Modification> i = anchor.modifications.iterator(); i.hasNext(); )
					{
						final Modification p = i.next();
						if(ids!=null && ids.contains(p.getID()))
						{
							p.publish();
							i.remove();
						}
					}
					// TODO maintain history
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
			}
			else if(request.getParameter(MODIFICATION_PERSIST)!=null)
			{
				try
				{
					startTransaction("persistProposals");
					final Draft parent = new Draft(anchor.username, anchor.sessionName, request.getParameter(MODIFICATION_PERSIST_COMMENT));
					for(final Iterator<Modification> i = anchor.modifications.iterator(); i.hasNext(); )
					{
						final Modification p = i.next();
						if(ids!=null && ids.contains(p.getID()))
						{
							p.saveTo(parent);
							i.remove();
						}
					}
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
			}
			else if(request.getParameter(SAVE_TO_DRAFT)!=null)
			{
				try
				{
					startTransaction("saveToDraft");
					final Draft parent = (Draft)model.getItem(request.getParameter(DRAFT_ID));
					for(final Iterator<Modification> i = anchor.modifications.iterator(); i.hasNext(); )
					{
						final Modification p = i.next();
						if(ids!=null && ids.contains(p.getID()))
						{
							p.saveTo(parent);
							i.remove();
						}
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
			else if(request.getParameter(MODIFICATION_DISCARD)!=null)
			{
				for(final Iterator<Modification> i = anchor.modifications.iterator(); i.hasNext(); )
				{
					final Modification p = i.next();
					if(ids!=null && ids.contains(p.getID()))
						i.remove();
				}
			}
			else if(request.getParameter(DRAFT_LOAD)!=null)
			{
				try
				{
					startTransaction("loadDraft");
					final Draft draft =
						(Draft)model.getItem(request.getParameter(DRAFT_ID));
					for(final DraftItem i : draft.getItems())
						anchor.modify(
								DraftItem.newValue.get(i),
								DraftItem.feature.get(i),
								model.getItem(DraftItem.item.get(i)));
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
			else if(request.getParameter(DRAFT_DELETE)!=null)
			{
				try
				{
					startTransaction("deleteDraft");
					((Draft)model.getItem(request.getParameter(DRAFT_ID))).deleteCopeItem();
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
			else if(request.getParameter(DRAFT_NEW)!=null)
			{
				try
				{
					startTransaction("newDraft");
					new Draft(anchor.username, anchor.sessionName, request.getParameter(DRAFT_COMMENT));
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
			}
			else if(request.getParameter(TARGET_OPEN)!=null)
			{
				anchor.setTarget(servlet.getTarget(request.getParameter(TARGET_ID)));
			}
		}

		final StringBuilder out = new StringBuilder();
		try
		{
			startTransaction("proposal");
			final List<Draft> drafts =
				draftsEnabled
				? Draft.TYPE.search(null, Draft.date, true)
				: null;
			final ArrayList<Target> targets = new ArrayList<Target>();
			targets.add(TargetLive.INSTANCE);
			if(draftsEnabled)
			{
				for(final Draft draft : drafts)
					targets.add(new TargetDraft(draft));
				targets.add(TargetNewDraft.INSTANCE);
			}
			Management_Jspm.write(
					out,
					request, response,
					response.encodeURL(request.getContextPath() + request.getServletPath() + '/' + PATH_INFO),
					anchor.getModifications(),
					anchor.getTarget(), targets,
					draftsEnabled, drafts);
			model.commit();
		}
		finally
		{
			model.rollbackIfNotCommitted();
		}

		response.setContentType("text/html; charset="+UTF8);
		BodySender.send(response, out, UTF8);
	}
}
