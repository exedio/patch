/*
 * Copyright (C) 2004-2015  exedio GmbH (www.exedio.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.exedio.cope.patch.example;

import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.misc.ServletUtil;
import com.exedio.cope.patch.Patch;
import com.exedio.cope.patch.PatchInitiator;
import com.exedio.cope.patch.Patches;
import com.exedio.cope.patch.PatchesBuilder;
import com.exedio.cope.patch.SchemaPatch;
import com.exedio.cope.patch.ServletPatchInitiatorUtil;
import com.exedio.cope.util.EmptyJobContext;
import jakarta.servlet.Servlet;

public final class MainPatches
{
	private static final PatchesBuilder builder = new PatchesBuilder().
			withStaleFromResource(MainPatches.class);

	static
	{
		MainPatchList.addAll();
	}

	private static final Patches patches = builder.build();

	static void add(final Patch patch)
	{
		builder.insertAtStart(patch);
	}

	static void add(final String id, final String... body)
	{
		builder.insertAtStart(new SchemaPatch(body){

			@Override
			public String getID()
			{
				return id;
			}

			@Override
			public int getStage()
			{
				return 10;
			}
		});
	}

	public static void run(final PatchInitiator initiator)
	{
		patches.run(new EmptyJobContext(), initiator);
	}

	public static Patches getPatches()
	{
		return patches;
	}

	public static ConnectToken getConnectedModel(final Servlet servlet)
	{
		return ServletUtil.getConnectedModel(servlet).returnOnFailureOf(result ->
		{
			if(result.getModel()!=Main.model)
				throw new IllegalArgumentException(result.getModel() + " " + Main.model);

			Main.model.reviseIfSupportedAndAutoEnabled();
			run(ServletPatchInitiatorUtil.create(servlet.getServletConfig()));
		});
		// DO NOT WRITE ANYTHING HERE, BUT IN returnOnFailureOf ABOVE ONLY
		// OTHERWISE ConnectTokens MAY BE LOST
	}

	private MainPatches()
	{
		// prevent instantiation
	}
}
