package com.exedio.cope.patch.example;

import com.exedio.cope.Item;
import com.exedio.cope.StringField;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass") // needed for TYPE
public class ExampleItem extends Item
{
	static final StringField name = new StringField().toFinal().optional();


	/**
	 * Creates a new ExampleItem with all the fields initially needed.
	 * @param name the initial value for field {@link #name}.
	 * @throws com.exedio.cope.StringLengthViolationException if name violates its length constraint.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(constructor=...) and @WrapperInitial
	@java.lang.SuppressWarnings("RedundantArrayCreation")
	ExampleItem(
				final java.lang.String name)
			throws
				com.exedio.cope.StringLengthViolationException
	{
		this(new com.exedio.cope.SetValue<?>[]{
			com.exedio.cope.SetValue.map(ExampleItem.name,name),
		});
	}

	/**
	 * Creates a new ExampleItem and sets the given fields initially.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(genericConstructor=...)
	protected ExampleItem(final com.exedio.cope.SetValue<?>... setValues){super(setValues);}

	/**
	 * Returns the value of {@link #name}.
	 */
	@com.exedio.cope.instrument.Generated // customize with @Wrapper(wrap="get")
	@java.lang.SuppressWarnings({"FinalMethodInFinalClass","RedundantSuppression","UnnecessarilyQualifiedStaticUsage"})
	final java.lang.String getName()
	{
		return ExampleItem.name.get(this);
	}

	@com.exedio.cope.instrument.Generated
	@java.io.Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The persistent type information for exampleItem.
	 */
	@com.exedio.cope.instrument.Generated // customize with @WrapperType(type=...)
	public static final com.exedio.cope.Type<ExampleItem> TYPE = com.exedio.cope.TypesBound.newType(ExampleItem.class);

	/**
	 * Activation constructor. Used for internal purposes only.
	 * @see com.exedio.cope.Item#Item(com.exedio.cope.ActivationParameters)
	 */
	@com.exedio.cope.instrument.Generated
	protected ExampleItem(final com.exedio.cope.ActivationParameters ap){super(ap);}
}
