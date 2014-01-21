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

public final class SampleItem extends Item
{
	static final StringField patch = new StringField().toFinal().unique().lengthRange(0, 1000);
	static final IntegerField number = new IntegerField().toFinal().unique().defaultToNext(1000);
	static final StringField transactionName = new StringField().toFinal();

	/**

	 **
	 * Creates a new SampleItem with all the fields initially needed.
	 * @param patch the initial value for field {@link #patch}.
	 * @param transactionName the initial value for field {@link #transactionName}.
	 * @throws com.exedio.cope.MandatoryViolationException if patch, transactionName is null.
	 * @throws com.exedio.cope.StringLengthViolationException if patch, transactionName violates its length constraint.
	 * @throws com.exedio.cope.UniqueViolationException if patch is not unique.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tags <tt>@cope.constructor public|package|protected|private|none</tt> in the class comment and <tt>@cope.initial</tt> in the comment of fields.
	 */
	SampleItem(
				final java.lang.String patch,
				final java.lang.String transactionName)
			throws
				com.exedio.cope.MandatoryViolationException,
				com.exedio.cope.StringLengthViolationException,
				com.exedio.cope.UniqueViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			SampleItem.patch.map(patch),
			SampleItem.transactionName.map(transactionName),
		});
	}/**

	 **
	 * Creates a new SampleItem and sets the given fields initially.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.generic.constructor public|package|protected|private|none</tt> in the class comment.
	 */
	private SampleItem(final com.exedio.cope.SetValue<?>... setValues)
	{
		super(setValues);
	}/**

	 **
	 * Returns the value of {@link #patch}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final java.lang.String getPatch()
	{
		return SampleItem.patch.get(this);
	}/**

	 **
	 * Finds a sampleItem by it's {@link #patch}.
	 * @param patch shall be equal to field {@link #patch}.
	 * @return null if there is no matching item.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.for public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	static final SampleItem forPatch(final java.lang.String patch)
	{
		return SampleItem.patch.searchUnique(SampleItem.class,patch);
	}/**

	 **
	 * Returns the value of {@link #number}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final int getNumber()
	{
		return SampleItem.number.getMandatory(this);
	}/**

	 **
	 * Finds a sampleItem by it's {@link #number}.
	 * @param number shall be equal to field {@link #number}.
	 * @return null if there is no matching item.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.for public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	static final SampleItem forNumber(final int number)
	{
		return SampleItem.number.searchUnique(SampleItem.class,number);
	}/**

	 **
	 * Returns the value of {@link #transactionName}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final java.lang.String getTransactionName()
	{
		return SampleItem.transactionName.get(this);
	}/**

	 **
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 */
	private static final long serialVersionUID = 1l;/**

	 **
	 * The persistent type information for sampleItem.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.type public|package|protected|private|none</tt> in the class comment.
	 */
	public static final com.exedio.cope.Type<SampleItem> TYPE = com.exedio.cope.TypesBound.newType(SampleItem.class);/**

	 **
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 */
	@SuppressWarnings("unused") private SampleItem(final com.exedio.cope.ActivationParameters ap){super(ap);
}}
