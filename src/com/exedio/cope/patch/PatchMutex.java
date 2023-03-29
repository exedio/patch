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

import static com.exedio.cope.instrument.Visibility.NONE;
import static com.exedio.cope.instrument.Visibility.PRIVATE;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;

import com.exedio.cope.CopeName;
import com.exedio.cope.DateField;
import com.exedio.cope.IntegerField;
import com.exedio.cope.Item;
import com.exedio.cope.StringField;
import com.exedio.cope.UniqueViolationException;
import com.exedio.cope.instrument.WrapInterim;
import com.exedio.cope.instrument.Wrapper;
import com.exedio.cope.instrument.WrapperType;
import com.exedio.cope.misc.Computed;
import java.util.Locale;

@CopeName("CopePatchMutex")
@Computed
@WrapperType(constructor=PRIVATE)
final class PatchMutex extends Item
{
	private static final int THE_ONE = 42;
	@WrapInterim
	@SuppressWarnings("unused")
	private static final Integer THE_ONE_OBJECT = THE_ONE;

	@Wrapper(wrap="get", visibility=NONE)
	@Wrapper(wrap="forStrict", visibility=NONE)
	@SuppressWarnings("unused")
	private static final IntegerField id =
		new IntegerField().toFinal().unique().
				defaultTo(THE_ONE_OBJECT).range(THE_ONE, THE_ONE+1);

	static PatchMutex seize(
			final int stage,
			final String host,
			final String savepoint,
			final int numberOfPatches)
	{
		try
		{
			return new PatchMutex(stage, host, savepoint, numberOfPatches);
		}
		catch(final UniqueViolationException e)
		{
			final PatchMutex mutex = forId(THE_ONE);
			throw new IllegalStateException(
					"Patch Mutex is locked" +
					(mutex!=null ? (
							" since " + ofPattern("uuuu-MM-dd HH:mm:ss", Locale.US).
									format(ofInstant(mutex.getFinished().toInstant(), UTC)) + " (UTC)" +
							" by " + mutex.getHost())
					: "") + ". " +
					"Probably a previous attempt to run patches failed (refer to your logs), " +
					"or patching is currently run by another thread (may be on another server)." +
					(mutex!=null
							? " Mutex has been locked for running" +
							  " the " + mutex.getNumberOfPatches() + " patches" +
							  " of stage " + mutex.getStage() + '.'
							: ""),
					e);
		}
	}

	static void release()
	{
		final PatchMutex mutex = forId(THE_ONE);
		if(mutex!=null)
			mutex.deleteCopeItem();
	}


	static final IntegerField stage    = PatchRun.stage.copy();
	static final StringField host      = PatchRun.host.copy();
	static final StringField savepoint = PatchRun.savepoint.copy();
	static final IntegerField numberOfPatches = new IntegerField().toFinal().min(1);
	static final DateField finished    = PatchRun.finished.copy();


	/**
	 * Creates a new PatchMutex with all the fields initially needed.
	 * @param stage the initial value for field {@link #stage}.
	 * @param host the initial value for field {@link #host}.
	 * @param savepoint the initial value for field {@link #savepoint}.
	 * @param numberOfPatches the initial value for field {@link #numberOfPatches}.
	 * @throws com.exedio.cope.IntegerRangeViolationException if numberOfPatches violates its range constraint.
	 * @throws com.exedio.cope.StringLengthViolationException if host, savepoint violates its length constraint.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(constructor=...) and @WrapperInitial
	@java.lang.SuppressWarnings({"RedundantArrayCreation","RedundantSuppression"})
	private PatchMutex(
				final int stage,
				final java.lang.String host,
				final java.lang.String savepoint,
				final int numberOfPatches)
			throws
				com.exedio.cope.IntegerRangeViolationException,
				com.exedio.cope.StringLengthViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			com.exedio.cope.SetValue.map(PatchMutex.stage,stage),
			com.exedio.cope.SetValue.map(PatchMutex.host,host),
			com.exedio.cope.SetValue.map(PatchMutex.savepoint,savepoint),
			com.exedio.cope.SetValue.map(PatchMutex.numberOfPatches,numberOfPatches),
		});
	}

	/**
	 * Creates a new PatchMutex and sets the given fields initially.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(genericConstructor=...)
	private PatchMutex(final com.exedio.cope.SetValue<?>... setValues){super(setValues);}

	/**
	 * Finds a patchMutex by it's {@link #id}.
	 * @param id shall be equal to field {@link #id}.
	 * @return null if there is no matching item.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="for")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	private static PatchMutex forId(final int id)
	{
		return PatchMutex.id.searchUnique(PatchMutex.class,id);
	}

	/**
	 * Returns the value of {@link #stage}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final int getStage()
	{
		return PatchMutex.stage.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #host}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final java.lang.String getHost()
	{
		return PatchMutex.host.get(this);
	}

	/**
	 * Returns the value of {@link #savepoint}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final java.lang.String getSavepoint()
	{
		return PatchMutex.savepoint.get(this);
	}

	/**
	 * Returns the value of {@link #numberOfPatches}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final int getNumberOfPatches()
	{
		return PatchMutex.numberOfPatches.getMandatory(this);
	}

	/**
	 * Returns the value of {@link #finished}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final java.util.Date getFinished()
	{
		return PatchMutex.finished.get(this);
	}

	@com.exedio.cope.instrument.Generated
	private static final long serialVersionUID = 1l;

	/**
	 * The persistent type information for patchMutex.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(type=...)
	static final com.exedio.cope.Type<PatchMutex> TYPE = com.exedio.cope.TypesBound.newType(PatchMutex.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@com.exedio.cope.instrument.Generated
	private PatchMutex(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
