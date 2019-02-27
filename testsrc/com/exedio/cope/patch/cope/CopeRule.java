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

package com.exedio.cope.patch.cope;

import static java.util.Objects.requireNonNull;

import com.exedio.cope.ConnectProperties;
import com.exedio.cope.Model;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class CopeRule implements
		BeforeAllCallback, AfterAllCallback,
		BeforeEachCallback, AfterEachCallback
{
	public interface Config
	{
		Model getModel();
		ConnectProperties getConnectProperties();
	}

	@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private Model model;

	@Override
	public void beforeAll(final ExtensionContext context)
	{
		final Optional<Class<?>> clazz = context.getTestClass();
		if(!clazz.isPresent())
			throw new IllegalArgumentException("does not have a test class");
		if(!(Config.class.isAssignableFrom(clazz.get())))
			throw new IllegalArgumentException("test class must implement " + Config.class + ", but was " + clazz.get());
	}

	@Override
	public void afterAll(final ExtensionContext context)
	{
		ModelConnector.dropAndDisconnect();
	}


	@Override
	public void beforeEach(final ExtensionContext context)
	{
		@SuppressWarnings("OptionalGetWithoutIsPresent")
		final Config config = (Config)context.getTestInstance().get();
		model = requireNonNull(config.getModel(), "model");
		//noinspection IfStatementWithNegatedCondition
		if(!model.isConnected())
			ModelConnector.connectAndCreate(model, config.getConnectProperties());
		else
			model.deleteSchemaForTest(); // typically faster than checkEmptySchema

		if(!context.getTags().contains(NO_TRANSACTION))
			model.startTransaction(context.getUniqueId());
	}

	@Test
	@Tag(NO_TRANSACTION)
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface NoTransaction
	{
	}

	private static final String NO_TRANSACTION = "notransaction";

	@Override
	public void afterEach(final ExtensionContext context)
	{
		// NOTE:
		// do rollback even if @NoTransaction is absent
		// because test could have started a transaction
		model.rollbackIfNotCommitted();

		if(model.isConnected())
		{
			model.getConnectProperties().mediaFingerprintOffset().reset();
			model.setDatabaseListener(null);
		}
		model.removeAllChangeListeners();
	}


	private CopeRule()
	{
		// just make private
	}
}
