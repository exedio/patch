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

import com.exedio.cope.CopeID;
import com.exedio.cope.DateField;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.LongField;
import com.exedio.cope.StringField;
import com.exedio.cope.misc.Computed;

@CopeID("CopePatchSchemaRun")
@Computed()
final class SchemaPatchRun extends Item
{
	static final StringField patch = new StringField().toFinal();
	static final IntegerField position  = new IntegerField().toFinal().min(0);
	static final DateField finished = new DateField().toFinal().defaultToNow();
	static final IntegerField rows  = new IntegerField().toFinal().min(0);
	static final LongField elapsed = new LongField().toFinal();

	/**

	 **
	 * Creates a new SchemaPatchRun with all the fields initially needed.
	 * @param patch the initial value for field {@link #patch}.
	 * @param position the initial value for field {@link #position}.
	 * @param rows the initial value for field {@link #rows}.
	 * @param elapsed the initial value for field {@link #elapsed}.
	 * @throws com.exedio.cope.IntegerRangeViolationException if position, rows violates its range constraint.
	 * @throws com.exedio.cope.MandatoryViolationException if patch is null.
	 * @throws com.exedio.cope.StringLengthViolationException if patch violates its length constraint.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tags <tt>@cope.constructor public|package|protected|private|none</tt> in the class comment and <tt>@cope.initial</tt> in the comment of fields.
	 */
	SchemaPatchRun(
				final java.lang.String patch,
				final int position,
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
			SchemaPatchRun.rows.map(rows),
			SchemaPatchRun.elapsed.map(elapsed),
		});
	}/**

	 **
	 * Creates a new SchemaPatchRun and sets the given fields initially.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.generic.constructor public|package|protected|private|none</tt> in the class comment.
	 */
	private SchemaPatchRun(final com.exedio.cope.SetValue<?>... setValues)
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
		return SchemaPatchRun.patch.get(this);
	}/**

	 **
	 * Returns the value of {@link #position}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final int getPosition()
	{
		return SchemaPatchRun.position.getMandatory(this);
	}/**

	 **
	 * Returns the value of {@link #finished}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final java.util.Date getFinished()
	{
		return SchemaPatchRun.finished.get(this);
	}/**

	 **
	 * Returns the value of {@link #rows}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final int getRows()
	{
		return SchemaPatchRun.rows.getMandatory(this);
	}/**

	 **
	 * Returns the value of {@link #elapsed}.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.get public|package|protected|private|none|non-final</tt> in the comment of the field.
	 */
	final long getElapsed()
	{
		return SchemaPatchRun.elapsed.getMandatory(this);
	}/**

	 **
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 */
	private static final long serialVersionUID = 1l;/**

	 **
	 * The persistent type information for schemaPatchRun.
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 *       It can be customized with the tag <tt>@cope.type public|package|protected|private|none</tt> in the class comment.
	 */
	static final com.exedio.cope.Type<SchemaPatchRun> TYPE = com.exedio.cope.TypesBound.newType(SchemaPatchRun.class);/**

	 **
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 * @cope.generated This feature has been generated by the cope instrumentor and will be overwritten by the build process.
	 */
	@SuppressWarnings("unused") private SchemaPatchRun(final com.exedio.cope.ActivationParameters ap){super(ap);
}}
