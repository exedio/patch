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

package com.exedio.cope.patch;

import static com.exedio.cope.SchemaInfo.getColumnName;
import static com.exedio.cope.SchemaInfo.getPrimaryKeyColumnName;
import static com.exedio.cope.SchemaInfo.getTableName;
import static com.exedio.cope.instrument.Visibility.PACKAGE;

import com.exedio.cope.CopeExternal;
import com.exedio.cope.Item;
import com.exedio.cope.SchemaInfo;
import com.exedio.cope.StringField;
import com.exedio.cope.instrument.Wrapper;
import java.util.concurrent.atomic.AtomicInteger;

@CopeExternal
final class SchemaSampleItem extends Item
{
	static final AtomicInteger thisSource = new AtomicInteger();

	@Wrapper(wrap="get", visibility=PACKAGE)
	private static final StringField content = new StringField().toFinal();

	static String create(final String contentValue)
	{
		return
				"INSERT INTO " + q(getTableName(TYPE)) +
				" ( " + q(getPrimaryKeyColumnName(TYPE)) + ", " + q(getColumnName(content)) + " ) " +
				"VALUES" +
				" ( " + thisSource.getAndIncrement() + ", '" + contentValue + "' )";
	}

	private static String q(final String name)
	{
		return SchemaInfo.quoteName(TYPE.getModel(), name);
	}


	/**
	 * Creates a new SchemaSampleItem with all the fields initially needed.
	 * @param content the initial value for field {@link #content}.
	 * @throws com.exedio.cope.MandatoryViolationException if content is null.
	 * @throws com.exedio.cope.StringLengthViolationException if content violates its length constraint.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(constructor=...) and @WrapperInitial
	private SchemaSampleItem(
				final java.lang.String content)
			throws
				com.exedio.cope.MandatoryViolationException,
				com.exedio.cope.StringLengthViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			SchemaSampleItem.content.map(content),
		});
	}

	/**
	 * Creates a new SchemaSampleItem and sets the given fields initially.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(genericConstructor=...)
	private SchemaSampleItem(final com.exedio.cope.SetValue<?>... setValues)
	{
		super(setValues);
	}

	/**
	 * Returns the value of {@link #content}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getContent()
	{
		return SchemaSampleItem.content.get(this);
	}

	@javax.annotation.Generated("com.exedio.cope.instrument")
	private static final long serialVersionUID = 1l;

	/**
	 * The persistent type information for schemaSampleItem.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(type=...)
	static final com.exedio.cope.Type<SchemaSampleItem> TYPE = com.exedio.cope.TypesBound.newType(SchemaSampleItem.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument")
	private SchemaSampleItem(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
