package com.exedio.cope.patch.example;

import com.exedio.cope.Revision;
import com.exedio.cope.Revisions;

public final class RevisionsFactory implements Revisions.Factory
{
	public static final RevisionsFactory INSTANCE = new RevisionsFactory();

	private boolean suppress = false;

	private RevisionsFactory()
	{
	}

	@Override
	public Revisions create(final Context ctx)
	{
		if (suppress)
			return new Revisions(0);
		else
			return new Revisions(new Revision(1, "Test Revision to ensure Revisions are executed","CREATE TABLE TestRevisionTable(this int,catch int not null,name varchar(80) not null)"));
	}

	void setSuppress(final boolean suppress)
	{
		this.suppress = suppress;
	}
}
