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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CopeAssert
{
	public static <T> void assertUnmodifiable(final Collection<T> c)
	{
		final String name = c.getClass().getName();
		assertTrue(UNMODIFIABLE_COLLECTIONS.contains(name), name);
	}

	private static final Set<String> UNMODIFIABLE_COLLECTIONS = Set.of(
			"java.util.Collections$UnmodifiableCollection",
			"java.util.Collections$UnmodifiableRandomAccessList",
			"java.util.Collections$SingletonList",
			"java.util.Collections$EmptyList",
			"java.util.Collections$UnmodifiableSet",
			"java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet");

	public static void assertEqualsUnmodifiable(final List<?> expected, final Collection<?> actual)
	{
		assertUnmodifiable(actual);
		assertEquals(expected, actual);
	}

	@SuppressWarnings("unused") // OK: for later use
	public static void assertEqualsUnmodifiable(final Map<?,?> expected, final Map<?,?> actual)
	{
		final String name = actual.getClass().getName();
		assertTrue(UNMODIFIABLE_MAPS.contains(name), name);
		assertEquals(expected, actual);
	}

	private static final Set<String> UNMODIFIABLE_MAPS = Set.of(
			"java.util.Collections$UnmodifiableMap");


	private CopeAssert()
	{
		// prevent instantiation
	}
}
