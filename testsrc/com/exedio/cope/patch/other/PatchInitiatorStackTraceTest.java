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

package com.exedio.cope.patch.other;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.exedio.cope.patch.PatchInitiator;
import com.exedio.cope.patch.PatchInitiatorStackTraceTestHelper;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * This class must reside in a package other than com.exedio.cope.patch.
 */
public class PatchInitiatorStackTraceTest
{
	@Test
	void test()
	{
		assertEquals("PatchInitiatorStackTraceTest", PatchInitiator.createFromStackTrace().toString());
	}

	@Test
	void testLocalClass()
	{
		final class Local
		{
			private String getInitiator()
			{
				return PatchInitiator.createFromStackTrace().toString();
			}
		}
		assertEquals("PatchInitiatorStackTraceTest$1Local", new Local().getInitiator());
	}

	@Test
	void testInnerClass()
	{
		assertEquals("PatchInitiatorStackTraceTest$Inner", new Inner().getInitiator());
	}

	@Test
	void testStaticNestedClass()
	{
		assertEquals("PatchInitiatorStackTraceTest$StaticNested", new StaticNested().getInitiator());
	}


	@Test
	void testAnonymousClass()
	{
		@SuppressWarnings("Convert2Lambda")
		final Supplier<String> anonymous = new Supplier<String>()
		{
			@Override
			public String get()
			{
				return PatchInitiator.createFromStackTrace().toString();
			}
		};
		assertEquals("PatchInitiatorStackTraceTest$1", anonymous.get());
	}

	@Test
	void testLambda()
	{
		final Supplier<String> lamba = ()-> PatchInitiator.createFromStackTrace().toString();
		assertEquals("PatchInitiatorStackTraceTest", lamba.get());
	}

	@Test
	void testFrameworkPackageSkipped()
	{
		assertEquals("PatchInitiatorStackTraceTest", new PatchInitiatorStackTraceTestHelper().getInitiator());
	}

	private static final class StaticNested
	{
		@SuppressWarnings("MethodMayBeStatic")
		private String getInitiator()
		{
			return PatchInitiator.createFromStackTrace().toString();
		}
	}

	private final class Inner
	{
		private String getInitiator()
		{
			return PatchInitiator.createFromStackTrace().toString();
		}
	}
}
