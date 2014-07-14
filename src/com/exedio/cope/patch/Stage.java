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

import static com.exedio.cope.misc.TimeUtil.toMillies;
import static java.lang.System.nanoTime;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.util.JobContext;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Stage
{
	private static final Logger logger = LoggerFactory.getLogger(Patches.class);

	private final int stageNumber;
	private final LinkedHashMap<String,Patch> patches;

	Stage(final int stageNumber)
	{
		this.stageNumber = stageNumber;
		this.patches = new LinkedHashMap<>();
	}

	public void put(final String id, final Patch patch)
	{
		patches.put(id, patch);
	}

	Envelope run(final Envelope envelopeInitially, final JobContext ctx)
	{
		final Model model = PatchRun.TYPE.getModel();
		synchronized(runLock)
		{
			final LinkedHashMap<String,Patch> patches = new LinkedHashMap<>(this.patches);

			try(TransactionTry tx = model.startTransactionTry("patch query stage " + stageNumber))
			{
				final List<String> idsDone = new Query<>(PatchRun.patch).search();
				tx.commit();
				patches.keySet().removeAll(idsDone);
			}

			Envelope envelope = envelopeInitially;

			for(final Map.Entry<String, Patch> entry : patches.entrySet())
			{
				if(envelope==null)
					envelope = new Envelope(model, patches.size(), ctx);

				final String id = entry.getKey();

				ctx.stopIfRequested();
				logger.info("patch s{} {}", stageNumber, id);
				if(ctx.supportsMessage())
					ctx.setMessage("run s" + stageNumber + ' ' + id);

				final Patch patch = entry.getValue();
				final boolean isTransactionally = patch.isTransactionally();
				try
				{
					final long start = nanoTime();
					if(isTransactionally)
					{
						model.startTransaction("patch s" + stageNumber + ' ' + id);
						patch.run(ctx);
					}
					else
					{
						patch.run(ctx);
						model.startTransaction("patch s" + stageNumber + ' ' + id + " log");
					}
					new PatchRun(id, stageNumber, isTransactionally, envelope.savepoint, toMillies(nanoTime(), start));
					model.commit();
				}
				finally
				{
					model.rollbackIfNotCommitted();
				}
				ctx.incrementProgress();
			}

			return envelope;
		}
	}

	private final Object runLock = new Object();


	Map<String,Patch> getPatches()
	{
		return Collections.unmodifiableMap(patches);
	}
}
