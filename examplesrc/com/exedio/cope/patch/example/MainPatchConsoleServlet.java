package com.exedio.cope.patch.example;

import com.exedio.cope.patch.PatchConsoleServlet;
import com.exedio.cope.patch.Patches;

public class MainPatchConsoleServlet extends PatchConsoleServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	public Patches getPatches()
	{
		return MainPatches.getPatches();
	}
}
