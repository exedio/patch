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

import com.exedio.cope.BooleanField;
import com.exedio.cope.CopeName;
import com.exedio.cope.DateField;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.LongField;
import com.exedio.cope.StringField;
import com.exedio.cope.misc.Computed;
import com.exedio.cope.util.CharSet;

@CopeName("CopePatchRun")
@Computed
final class PatchRun extends Item
{
	static final StringField patch = new StringField().toFinal().unique().charSet(new CharSet(' ', '~'));
	static final IntegerField stage = new IntegerField().toFinal();
	static final BooleanField isTransactionally = new BooleanField().toFinal();
	static final StringField host      = new StringField().toFinal().optional().lengthMax(10000);
	static final StringField savepoint = new StringField().toFinal().optional().lengthMax(10000);
	static final DateField finished = new DateField().toFinal().defaultToNow();
	static final LongField elapsed = new LongField().toFinal();

	/**
	 * for {@link Patches#preempt()} only
	 */
	PatchRun(
			final String patch,
			final int stage,
			final boolean isTransactionally,
			final String host)
	{
		this(patch, stage, isTransactionally, host, "preempted", 0);
	}


	/**
	 * Creates a new PatchRun with all the fields initially needed.
	 * @param patch the initial value for field {@link #patch}.
	 * @param stage the initial value for field {@link #stage}.
	 * @param isTransactionally the initial value for field {@link #isTransactionally}.
	 * @param host the initial value for field {@link #host}.
	 * @param savepoint the initial value for field {@link #savepoint}.
	 * @param elapsed the initial value for field {@link #elapsed}.
	 * @throws com.exedio.cope.MandatoryViolationException if patch is null.
	 * @throws com.exedio.cope.StringCharSetViolationException if patch violates its character set constraint.
	 * @throws com.exedio.cope.StringLengthViolationException if patch, host, savepoint violates its length constraint.
	 * @throws com.exedio.cope.UniqueViolationException if patch is not unique.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(constructor=...) and @WrapperInitial
	PatchRun(
				final java.lang.String patch,
				final int stage,
				final boolean isTransactionally,
				final java.lang.String host,
				final java.lang.String savepoint,
				final long elapsed)
			throws
				com.exedio.cope.MandatoryViolationException,
				com.exedio.cope.StringCharSetViolationException,
				com.exedio.cope.StringLengthViolationException,
				com.exedio.cope.UniqueViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			PatchRun.patch.map(patch),
			PatchRun.stage.map(stage),
			PatchRun.isTransactionally.map(isTransactionally),
			PatchRun.host.map(host),
			PatchRun.savepoint.map(savepoint),
			PatchRun.elapsed.map(elapsed),
		});
	}

	/**
	 * Creates a new PatchRun and sets the given fields initially.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(genericConstructor=...)
	private PatchRun(final com.exedio.cope.SetValue<?>... setValues)
	{
		super(setValues);
	}

	/**
	 * Returns the value of {@link #patch}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getPatch()
	{
		return PatchRun.patch.get(this);
	}

	/**
	 * Finds a patchRun by it's {@link #patch}.
	 * @param patch shall be equal to field {@link #patch}.
	 * @return null if there is no matching item.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="for")
	static final PatchRun forPatch(final java.lang.String patch)
	{
		return PatchRun.patch.searchUnique(PatchRun.class,patch);
	}

	/**
	 * Returns the value of {@link #stage}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final int getStage()
	{
		return PatchRun.stage.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #isTransactionally}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final boolean getIsTransactionally()
	{
		return PatchRun.isTransactionally.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #host}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getHost()
	{
		return PatchRun.host.get(this);
	}

	/**
	 * Returns the value of {@link #savepoint}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.lang.String getSavepoint()
	{
		return PatchRun.savepoint.get(this);
	}

	/**
	 * Returns the value of {@link #finished}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final java.util.Date getFinished()
	{
		return PatchRun.finished.get(this);
	}

	/**
	 * Returns the value of {@link #elapsed}.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @Wrapper(wrap="get")
	final long getElapsed()
	{
		return PatchRun.elapsed.getMandatory(this);
	}

	@javax.annotation.Generated("com.exedio.cope.instrument")
	private static final long serialVersionUID = 1l;

	/**
	 * The persistent type information for patchRun.
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument") // customize with @WrapperType(type=...)
	static final com.exedio.cope.Type<PatchRun> TYPE = com.exedio.cope.TypesBound.newType(PatchRun.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@javax.annotation.Generated("com.exedio.cope.instrument")
	private PatchRun(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
