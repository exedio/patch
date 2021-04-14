
package com.exedio.cope.patch.example;

import com.exedio.cope.Model;
import com.exedio.cope.patch.Patches;

public final class Main
{
	private Main()
	{
	}

	public static final Model model = initializeModel();

	static
	{
		model.enableSerialization(Main.class, "model");
	}

	private static Model initializeModel()
	{
		return Model.builder().
			add(RevisionsFactory.INSTANCE).
			add(Patches.types).
			build();
	}
}
