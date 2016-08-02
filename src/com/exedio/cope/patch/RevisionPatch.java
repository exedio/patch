/*
 * Copyright (C) 2004-2013  exedio GmbH (www.exedio.com)
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

package com.exedio.cope.patch;

import com.exedio.cope.Model;
import com.exedio.cope.RevisionInfo;
import com.exedio.cope.RevisionInfoRevise;
import com.exedio.cope.RevisionInfoRevise.Body;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import java.util.Date;
import java.util.Map;

/**
 * This patch stores information from {@link RevisionInfo} into patch logs.
 * For preserving this information after dropping revisions.
 */
public final class RevisionPatch implements Patch
{
	private final int stage;

	public RevisionPatch(final int stage)
	{
		this.stage = stage;
	}

	@Override
	public String getID()
	{
		return getClass().getName();
	}

	@Override
	public int getStage()
	{
		return stage;
	}

	@Override
	public void check()
	{
		// empty
	}

	@Override
	public boolean isTransactionally()
	{
		return false;
	}

	@Override
	public void run(final JobContext ctx)
	{
		final Model model = PatchRun.TYPE.getModel();
		for(final Map.Entry<Integer, byte[]> entry : model.getRevisionLogs().entrySet())
		{
			ctx.stopIfRequested();

			final RevisionInfo revisionAbstract = RevisionInfo.read(entry.getValue());
			if(revisionAbstract==null || !(revisionAbstract instanceof RevisionInfoRevise))
				continue;

			final RevisionInfoRevise revision = (RevisionInfoRevise)revisionAbstract;
			final int number = entry.getKey();
			final String id = RevisionInfoRevise.class.getName() + '#' + number;
			final Date date = revision.getDate();

			try(TransactionTry tx = model.startTransactionTry(id))
			{
				long patchElapsed = 0;
				int position = 0;
				for(final Body body : revision.getBody())
				{
					final long elapsed = body.getElapsed();
					SchemaPatchRun.TYPE.newItem(
							SchemaPatchRun.patch.map(id),
							SchemaPatchRun.position.map(position++),
							SchemaPatchRun.sql.map(body.getSQL()),
							SchemaPatchRun.finished.map(date),
							SchemaPatchRun.rows.map(body.getRows()),
							SchemaPatchRun.elapsed.map(elapsed));

					patchElapsed += elapsed;
				}

				PatchRun.TYPE.newItem(
						PatchRun.patch.map(id),
						PatchRun.stage.map(Integer.MIN_VALUE),
						PatchRun.isTransactionally.map(false),
						PatchRun.host.map(revision.getEnvironment().get("hostname")),
						PatchRun.savepoint.map(revision.getSavepoint()),
						PatchRun.finished.map(date),
						PatchRun.elapsed.map(patchElapsed));

				tx.commit();
			}
		}
	}
}
