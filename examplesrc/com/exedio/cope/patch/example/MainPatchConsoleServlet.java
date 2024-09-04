package com.exedio.cope.patch.example;

import com.exedio.cope.patch.PatchConsoleServlet;
import com.exedio.cope.patch.Patches;
import java.io.Serial;

public class MainPatchConsoleServlet extends PatchConsoleServlet
{
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public Patches getPatches()
	{
		return MainPatches.getPatches();
	}
}
