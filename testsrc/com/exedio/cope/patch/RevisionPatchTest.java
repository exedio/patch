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

import static com.exedio.cope.SchemaInfo.quoteName;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.RevisionInfoRevise;
import com.exedio.cope.RevisionInfoRevise.Body;
import com.exedio.cope.SchemaInfo;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobContexts;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RevisionPatchTest extends CopeModel4Test
{
	private static final Model MODEL = PatchTest.MODEL;

	public RevisionPatchTest()
	{
		super(MODEL);
	}

	@Test void test() throws SQLException
	{
		assertEquals(emptyList(), runs());

		final HashMap<String, String> env = new HashMap<>();
		save(new RevisionInfoRevise(
				100, null, new Date(881000000), env, "comment",
				new Body("sql100-0", 2210001, 3310001)));
		env.put("hostname", "testHostname");
		save(new RevisionInfoRevise(
				101, "testSavepoint", new Date(881010000), env, "comment",
				new Body("sql101-0", 2210101, 3310101),
				new Body("sql101-1", 2210102, 3310102)));

		final Patch patch = new RevisionPatch(22);
		assertEquals("com.exedio.cope.patch.RevisionPatch", patch.getID());
		assertEquals(22, patch.getStage());

		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(patch);
		final Patches patches = builder.build();
		run(patches, JobContexts.EMPTY);
		{
			final Iterator<PatchRun> runs = runs().iterator();
			{
				final PatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoRevise#100", p.getPatch());
				assertEquals(Integer.MIN_VALUE, p.getStage());
				assertEquals(false, p.getIsTransactionally());
				assertEquals(null, p.getHost());
				assertEquals(null, p.getSavepoint());
				assertEquals(new Date(881000000), p.getFinished());
				assertEquals(3310001, p.getElapsed());
			}
			{
				final PatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoRevise#101", p.getPatch());
				assertEquals(Integer.MIN_VALUE, p.getStage());
				assertEquals(false, p.getIsTransactionally());
				assertEquals("testHostname", p.getHost());
				assertEquals("testSavepoint", p.getSavepoint());
				assertEquals(new Date(881010000), p.getFinished());
				assertEquals(6620203, p.getElapsed());
			}
			{
				final PatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoCreate#200", p.getPatch());
				assertEquals(Integer.MIN_VALUE, p.getStage());
				assertEquals(false, p.getIsTransactionally());
				assertEquals(null, p.getSavepoint());
				assertEquals(0, p.getElapsed());
			}
			assertEquals("com.exedio.cope.patch.RevisionPatch", runs.next().getPatch());
			assertFalse(runs.hasNext());
		}
		{
			final Iterator<SchemaPatchRun> runs = schemaRuns().iterator();
			{
				final SchemaPatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoRevise#100", p.getPatch());
				assertEquals(0, p.getPosition());
				assertEquals("sql100-0", p.getSql());
				assertEquals(new Date(881000000), p.getFinished());
				assertEquals(2210001, p.getRows());
				assertEquals(3310001, p.getElapsed());
			}
			{
				final SchemaPatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoRevise#101", p.getPatch());
				assertEquals(0, p.getPosition());
				assertEquals("sql101-0", p.getSql());
				assertEquals(new Date(881010000), p.getFinished());
				assertEquals(2210101, p.getRows());
				assertEquals(3310101, p.getElapsed());
			}
			{
				final SchemaPatchRun p = runs.next();
				assertEquals("com.exedio.cope.RevisionInfoRevise#101", p.getPatch());
				assertEquals(1, p.getPosition());
				assertEquals("sql101-1", p.getSql());
				assertEquals(new Date(881010000), p.getFinished());
				assertEquals(2210102, p.getRows());
				assertEquals(3310102, p.getElapsed());
			}
			assertFalse(runs.hasNext());
		}
	}

	private static void save(final RevisionInfoRevise revision) throws SQLException
	{
		final Model model = PatchRun.TYPE.getModel();
		model.commit();
		try(
			Connection con = SchemaInfo.newConnection(model);
			PreparedStatement stat = con.prepareStatement(
					"insert into " + quoteName(model, "while") +
					" (" + quoteName(model, "v") + "," + quoteName(model, "i") + ") values (?,?)"))
		{
			stat.setInt(1, revision.getNumber());
			stat.setBytes(2, revision.toBytes());
			assertEquals(1, stat.executeUpdate());
		}
		model.startTransaction(RevisionPatchTest.class.getName());
	}


	private static void run(
			final Patches patches,
			final JobContext ctx)
	{
		MODEL.commit();
		try
		{
			patches.run(ctx, new PatchInitiator("RevisionPatchTestInitiator"));
		}
		finally
		{
			MODEL.startTransaction(RevisionPatchTest.class.getName());
		}
		assertEquals(0, PatchMutex.TYPE.newQuery().total());
	}

	private static List<PatchRun> runs()
	{
		final Query<PatchRun> q = PatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	private static List<SchemaPatchRun> schemaRuns()
	{
		final Query<SchemaPatchRun> q = SchemaPatchRun.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}
}
