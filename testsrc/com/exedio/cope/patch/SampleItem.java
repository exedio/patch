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

import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.StringField;

/**
 * @cope.constructor private
 */
public final class SampleItem extends Item
{
	static final StringField patch = new StringField().toFinal().lengthRange(0, 1000);
	static final IntegerField number = new IntegerField().toFinal().unique().defaultToNext(1000);
	static final StringField transactionName = new StringField().toFinal().optional();
	static final StringField mutexHost      = new StringField().toFinal().optional().lengthMax(10000);
	static final StringField mutexSavepoint = new StringField().toFinal().optional().lengthMax(10000);
	static final IntegerField mutexNumberOfPatches = new IntegerField().toFinal().min(1);


	SampleItem(
			final String patch,
			final String transactionName,
			final PatchMutex mutex)
	{
		this(
				patch,
				transactionName,
				mutex.getHost(),
				mutex.getSavepoint(),
				mutex.getNumberOfPatches());
	}


	/**
	 * Creates a new SampleItem with all the fields initially needed.
	 * @param patch the initial value for field {@link #patch}.
	 * @param transactionName the initial value for field {@link #transactionName}.
	 * @param mutexHost the initial value for field {@link #mutexHost}.
	 * @param mutexSavepoint the initial value for field {@link #mutexSavepoint}.
	 * @param mutexNumberOfPatches the initial value for field {@link #mutexNumberOfPatches}.
	 * @throws com.exedio.cope.IntegerRangeViolationException if mutexNumberOfPatches violates its range constraint.
	 * @throws com.exedio.cope.MandatoryViolationException if patch is null.
	 * @throws com.exedio.cope.StringLengthViolationException if patch, transactionName, mutexHost, mutexSavepoint violates its length constraint.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(constructor=...) and @WrapperInitial
	private SampleItem(
				final java.lang.String patch,
				final java.lang.String transactionName,
				final java.lang.String mutexHost,
				final java.lang.String mutexSavepoint,
				final int mutexNumberOfPatches)
			throws
				com.exedio.cope.IntegerRangeViolationException,
				com.exedio.cope.MandatoryViolationException,
				com.exedio.cope.StringLengthViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			SampleItem.patch.map(patch),
			SampleItem.transactionName.map(transactionName),
			SampleItem.mutexHost.map(mutexHost),
			SampleItem.mutexSavepoint.map(mutexSavepoint),
			SampleItem.mutexNumberOfPatches.map(mutexNumberOfPatches),
		});
	}

	/**
	 * Creates a new SampleItem and sets the given fields initially.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(genericConstructor=...)
	private SampleItem(final com.exedio.cope.SetValue<?>... setValues)
	{
		super(setValues);
	}

	/**
	 * Returns the value of {@link #patch}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getPatch()
	{
		return SampleItem.patch.get(this);
	}

	/**
	 * Returns the value of {@link #number}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final int getNumber()
	{
		return SampleItem.number.getMandatory(this);
	}

	/**
	 * Finds a sampleItem by it's {@link #number}.
	 * @param number shall be equal to field {@link #number}.
	 * @return null if there is no matching item.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="for")
	static final SampleItem forNumber(final int number)
	{
		return SampleItem.number.searchUnique(SampleItem.class,number);
	}

	/**
	 * Returns the value of {@link #transactionName}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getTransactionName()
	{
		return SampleItem.transactionName.get(this);
	}

	/**
	 * Returns the value of {@link #mutexHost}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getMutexHost()
	{
		return SampleItem.mutexHost.get(this);
	}

	/**
	 * Returns the value of {@link #mutexSavepoint}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getMutexSavepoint()
	{
		return SampleItem.mutexSavepoint.get(this);
	}

	/**
	 * Returns the value of {@link #mutexNumberOfPatches}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final int getMutexNumberOfPatches()
	{
		return SampleItem.mutexNumberOfPatches.getMandatory(this);
	}

	@javax.annotation.Generated("com.exedio.cope.instrument")
	private static final long serialVersionUID = 1l;

	/**
	 * The persistent type information for sampleItem.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(type=...)
	public static final com.exedio.cope.Type<SampleItem> TYPE = com.exedio.cope.TypesBound.newType(SampleItem.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument")
	private SampleItem(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
