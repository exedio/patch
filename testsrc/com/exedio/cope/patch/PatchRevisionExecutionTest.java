package com.exedio.cope.patch;

import static com.exedio.cope.SchemaInfo.quoteName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.exedio.cope.Model;
import com.exedio.cope.Query;
import com.exedio.cope.Revision;
import com.exedio.cope.Revisions;
import com.exedio.cope.SchemaInfo;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.util.JobContexts;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatchRevisionExecutionTest extends CopeModel4Test
{
	static final Model MODEL = PatchTest.MODEL;
	private static final Revisions.Factory DELEGATE_REVISIONS_FACTORY = ctx -> new Revisions(new Revision(PatchTest.REVISION_START_ID + 1, "Create SchemaSampleItem", SchemaSampleItem.create("foo")));

	public PatchRevisionExecutionTest()
	{
		super(MODEL);
	}

	@Test void revisionExecutionOnRun()
	{
		PatchTest.REVISION_FACTORY.setDelegate(DELEGATE_REVISIONS_FACTORY);
		final Patches emptyPatches = new PatchesBuilder().build();
		PatchTest.run(emptyPatches, JobContexts.EMPTY);
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertEquals("foo", items.next().getContent(), "content");
		assertFalse(items.hasNext());
	}

	@Test void revisionExecutionOnPreempt()
	{
		PatchTest.REVISION_FACTORY.setDelegate(DELEGATE_REVISIONS_FACTORY);
		final Patches emptyPatches = new PatchesBuilder().build();
		PatchTest.preempt(emptyPatches);
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertEquals("foo", items.next().getContent(), "content");
		assertFalse(items.hasNext());
	}

	@Test void revisionExecutionOnPreemptSingle()
	{
		PatchTest.REVISION_FACTORY.setDelegate(DELEGATE_REVISIONS_FACTORY);
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		PatchTest.preemptSingle(patches, "one");
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertEquals("foo", items.next().getContent(), "content");
		assertFalse(items.hasNext());
	}

	@Test void revisionExecutionOnIsDone()
	{
		PatchTest.REVISION_FACTORY.setDelegate(DELEGATE_REVISIONS_FACTORY);
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		PatchTest.isDone(patches);
		final Iterator<SchemaSampleItem> items = items().iterator();
		assertEquals("foo", items.next().getContent(), "content");
		assertFalse(items.hasNext());
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id, null, true);
	}

	private static List<SchemaSampleItem> items()
	{
		final Query<SchemaSampleItem> q = SchemaSampleItem.TYPE.newQuery();
		q.setOrderByThis(true);
		return q.search();
	}

	@BeforeEach void deleteItemsAndReconnect()
	{
		// This is needed because creation of SchemaSampleItem
		// bypasses (therefore corrupt) cope item cache.
		for(final SchemaSampleItem item : items())
			item.deleteCopeItem();

		// force model reconnect, this is needed to reset
		// the revisions-already-executed flag in model
		MODEL.commit();
		MODEL.disconnect();
		MODEL.connect(getConnectProperties());
		MODEL.startTransaction(PatchRevisionExecutionTest.class.getName());
	}

	@AfterEach void resetDelegateAndThisSourceAndCleanupWhile() throws SQLException
	{
		PatchTest.REVISION_FACTORY.setDelegate(null);

		// reset thisSource of the SchemaSampleItem to let new items start with PK 0 in other tests
		SchemaSampleItem.thisSource.set(0);

		// cleanup the while table
		final Model model = PatchRun.TYPE.getModel();
		model.commit();
		try(
				Connection con = SchemaInfo.newConnection(model);
				PreparedStatement stat = con.prepareStatement(
						"delete from " + quoteName(model, "while") +
						" where " + quoteName(model, "v") + ">?"))
		{
			stat.setInt(1, PatchTest.REVISION_START_ID);
			stat.execute();
		}
		model.startTransaction(PatchRevisionExecutionTest.class.getName());
	}
}
