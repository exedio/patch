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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CopeAssert
{
	@SuppressWarnings("SuspiciousMethodCalls")
	public static <T> void assertUnmodifiable(final Collection<T> c)
	{
		try
		{
			c.add(null);
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}
		try
		{
			c.addAll(Collections.singleton(null));
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}

		if(!c.isEmpty())
		{
			final Object o = c.iterator().next();
			try
			{
				c.clear();
				fail("should have thrown UnsupportedOperationException");
			}
			catch(final UnsupportedOperationException ignored) {/*OK*/}
			try
			{
				c.remove(o);
				fail("should have thrown UnsupportedOperationException");
			}
			catch(final UnsupportedOperationException ignored) {/*OK*/}
			try
			{
				c.removeAll(Collections.singleton(o));
				fail("should have thrown UnsupportedOperationException");
			}
			catch(final UnsupportedOperationException ignored) {/*OK*/}
			try
			{
				c.retainAll(Collections.emptyList());
				fail("should have thrown UnsupportedOperationException");
			}
			catch(final UnsupportedOperationException ignored) {/*OK*/}

			final Iterator<?> iterator = c.iterator();
			try
			{
				iterator.next();
				iterator.remove();
				fail("should have thrown UnsupportedOperationException");
			}
			catch(final UnsupportedOperationException ignored) {/*OK*/}
		}

		if(c instanceof List<?>)
		{
			final List<T> l = (List<T>)c;

			if(!l.isEmpty())
			{
				try
				{
					l.set(0, null);
					fail("should have thrown UnsupportedOperationException");
				}
				catch(final UnsupportedOperationException ignored) {/*OK*/}
			}
		}
	}

	public static void assertEqualsUnmodifiable(final List<?> expected, final Collection<?> actual)
	{
		assertUnmodifiable(actual);
		assertEquals(expected, actual);
	}

	public static void assertEqualsUnmodifiable(final Map<?,?> expected, final Map<?,?> actual)
	{
		try
		{
			actual.clear();
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}
		try
		{
			actual.put(null, null);
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}
		try
		{
			actual.putAll(Collections.emptyMap());
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}
		try
		{
			actual.remove(null);
			fail("should have thrown UnsupportedOperationException");
		}
		catch(final UnsupportedOperationException ignored) {/*OK*/}
		assertUnmodifiable(actual.keySet());
		assertUnmodifiable(actual.values());
		assertUnmodifiable(actual.entrySet());
		assertEquals(expected, actual);
	}


	private CopeAssert()
	{
		// prevent instantiation
	}
}
