package com.exedio.cope.patch.example;

import com.exedio.cope.Revisions;

public class RevisionsFactory implements Revisions.Factory
{

	@Override
	public Revisions create(final Context ctx)
	{
		return new Revisions(0);
	}

}
