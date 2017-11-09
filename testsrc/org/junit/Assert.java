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

package org.junit;

import org.junit.jupiter.api.Assertions;

/**
 * This is a replacement of the respective class in JUnit 4.
 * Allows switching to JUnit 5 without extensive changes in the project.
 */
public final class Assert
{
	public static void assertTrue(final boolean condition)
	{
		Assertions.assertTrue(condition);
	}

	public static void assertTrue(final boolean condition, final String message)
	{
		Assertions.assertTrue(condition, message);
	}

	public static void assertFalse(final boolean condition)
	{
		Assertions.assertFalse(condition);
	}

	public static void assertFalse(final boolean condition, final String message)
	{
		Assertions.assertFalse(condition, message);
	}


	public static void assertNull(final Object actual)
	{
		Assertions.assertNull(actual);
	}

	public static void assertNotNull(final Object actual)
	{
		Assertions.assertNotNull(actual);
	}

	public static void assertNotNull(final Object actual, final String message)
	{
		Assertions.assertNotNull(actual, message);
	}


	public static void assertEquals(final short expected, final short actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final short expected, final short actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final byte expected, final byte actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final byte expected, final byte actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final int expected, final int actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final int expected, final int actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final long expected, final long actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final long expected, final long actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final char expected, final char actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final char expected, final char actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final float expected, final float actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final float expected, final float actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final double expected, final double actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final double expected, final double actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}

	public static void assertEquals(final Object expected, final Object actual)
	{
		Assertions.assertEquals(expected, actual);
	}

	public static void assertEquals(final Object expected, final Object actual, final String message)
	{
		Assertions.assertEquals(expected, actual, message);
	}


	public static void assertSame(final Object expected, final Object actual)
	{
		Assertions.assertSame(expected, actual);
	}

	public static void assertNotSame(final Object unexpected, final Object actual)
	{
		Assertions.assertNotSame(unexpected, actual);
	}


	public static void fail()
	{
		Assertions.fail((String)null);
	}

	public static void fail(final String message)
	{
		Assertions.fail(message);
	}


	private Assert()
	{
		// prevent instantiation
	}
}
