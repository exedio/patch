/*
 * Copyright (C) 2004-2009  exedio GmbH (www.exedio.com)
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

package com.exedio.cope.live;

import java.io.Serializable;

import com.exedio.cope.Feature;
import com.exedio.cope.Item;

abstract class Modification implements Serializable // for session persistence
{
	private static final long serialVersionUID = 1l;

	private final Feature feature;
	final Item item;

	Modification(final Feature feature, final Item item)
	{
		this.feature = feature;
		this.item = item;

		assert item!=null;
	}

	Feature getFeature()
	{
		return feature;
	}

	final String getID()
	{
		return feature.getID() + '/' + item.getCopeID();
	}

	abstract void publish();
	abstract void saveTo(final Draft draft);

	@Override
	public final int hashCode()
	{
		return feature.hashCode() ^ item.hashCode();
	}

	@Override
	public final boolean equals(final Object other)
	{
		if(!(other instanceof Modification))
			return false;

		final Modification o = (Modification)other;
		return feature.equals(o.feature) && item.equals(o.item);
	}

	@Override
	public final String toString()
	{
		return feature.getID() + '-' + item.getCopeID();
	}
}
