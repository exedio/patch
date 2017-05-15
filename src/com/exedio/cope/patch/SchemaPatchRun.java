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

import com.exedio.cope.CopeName;
import com.exedio.cope.DateField;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.LongField;
import com.exedio.cope.StringField;
import com.exedio.cope.misc.Computed;

@CopeName("CopePatchSchemaRun")
@Computed()
final class SchemaPatchRun extends Item
{
	static final StringField patch = new StringField().toFinal();
	static final IntegerField position  = new IntegerField().toFinal().min(0);
	static final StringField sql = new StringField().toFinal().lengthMax(10*1000*1000);
	static final DateField finished = new DateField().toFinal().defaultToNow();
	static final IntegerField rows  = new IntegerField().toFinal().min(0);
	static final LongField elapsed = new LongField().toFinal();


	/**
	 * Creates a new SchemaPatchRun with all the fields initially needed.
	 * @param patch the initial value for field {@link #patch}.
	 * @param position the initial value for field {@link #position}.
	 * @param sql the initial value for field {@link #sql}.
	 * @param rows the initial value for field {@link #rows}.
	 * @param elapsed the initial value for field {@link #elapsed}.
	 * @throws com.exedio.cope.IntegerRangeViolationException if position, rows violates its range constraint.
	 * @throws com.exedio.cope.MandatoryViolationException if patch, sql is null.
	 * @throws com.exedio.cope.StringLengthViolationException if patch, sql violates its length constraint.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(constructor=...) and @WrapperInitial
	SchemaPatchRun(
				final java.lang.String patch,
				final int position,
				final java.lang.String sql,
				final int rows,
				final long elapsed)
			throws
				com.exedio.cope.IntegerRangeViolationException,
				com.exedio.cope.MandatoryViolationException,
				com.exedio.cope.StringLengthViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			SchemaPatchRun.patch.map(patch),
			SchemaPatchRun.position.map(position),
			SchemaPatchRun.sql.map(sql),
			SchemaPatchRun.rows.map(rows),
			SchemaPatchRun.elapsed.map(elapsed),
		});
	}

	/**
	 * Creates a new SchemaPatchRun and sets the given fields initially.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(genericConstructor=...)
	private SchemaPatchRun(final com.exedio.cope.SetValue<?>... setValues)
	{
		super(setValues);
	}

	/**
	 * Returns the value of {@link #patch}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getPatch()
	{
		return SchemaPatchRun.patch.get(this);
	}

	/**
	 * Returns the value of {@link #position}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final int getPosition()
	{
		return SchemaPatchRun.position.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #sql}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getSql()
	{
		return SchemaPatchRun.sql.get(this);
	}

	/**
	 * Returns the value of {@link #finished}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.util.Date getFinished()
	{
		return SchemaPatchRun.finished.get(this);
	}

	/**
	 * Returns the value of {@link #rows}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final int getRows()
	{
		return SchemaPatchRun.rows.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #elapsed}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final long getElapsed()
	{
		return SchemaPatchRun.elapsed.getMandatory(this);
	}

	@javax.annotation.Generated("com.exedio.cope.instrument")
	private static final long serialVersionUID = 1l;

	/**
	 * The persistent type information for schemaPatchRun.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(type=...)
	static final com.exedio.cope.Type<SchemaPatchRun> TYPE = com.exedio.cope.TypesBound.newType(SchemaPatchRun.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument")
	private SchemaPatchRun(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
