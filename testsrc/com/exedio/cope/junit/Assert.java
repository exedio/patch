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

package com.exedio.cope.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

public final class Assert
{
	@SuppressWarnings("UnusedReturnValue")
	public static <T extends Throwable> T assertFails(
			final Executable executable,
			final Class<T> expectedType,
			final String expectedMessage)
	{
		final T result = assertThrows(expectedType, executable);
		assertSame(expectedType, result.getClass());
		assertEquals(expectedMessage, result.getMessage());
		assertSame(null, result.getCause());
		return result;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static <T extends Throwable> T assertFails(
			final Executable executable,
			final Class<T> expectedType,
			final String expectedMessage,
			final Class<? extends Throwable> expectedCause)
	{
		final T result = assertThrows(expectedType, executable);
		assertSame(expectedType, result.getClass());
		assertEquals(expectedMessage, result.getMessage());

		final Throwable actualCause = result.getCause();
		assertSame(expectedCause, actualCause!=null ? actualCause.getClass() : null);

		return result;
	}


	private Assert()
	{
		// prevent instantiation
	}
}
