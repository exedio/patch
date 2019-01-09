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

package com.exedio.cope.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LogbackException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.LoggerFactory;

public final class LogRule implements AfterEachCallback, ParameterResolver
{
	private final ArrayList<String> actualEvents = new ArrayList<>();
	private final ArrayList<Runnable> afterEach = new ArrayList<>();

	public void listen(final Class<?> clazz)
	{
		listen(clazz, null);
	}

	public void listen(final Class<?> clazz, final String shortcut)
	{
		final Logger logger = (Logger)LoggerFactory.getLogger(clazz);

		final AssertionFailedLogAppender appender = new AssertionFailedLogAppender()
		{
			@Override
			public void doAppend(final ILoggingEvent iLoggingEvent) throws LogbackException
			{
				actualEvents.add(
						(shortcut!=null ? (shortcut + ": ") : "") +
						iLoggingEvent.getLevel() + " " +
						iLoggingEvent.getFormattedMessage());
			}
		};
		logger.addAppender(appender);

		afterEach.add(() -> logger.detachAppender(appender));
	}

	public void assertEvents(final String... expectedEvents)
	{
		assertEquals(Arrays.asList(expectedEvents), actualEvents);
		actualEvents.clear();
	}


	@Override
	public boolean supportsParameter(
			final ParameterContext parameterContext,
			final ExtensionContext extensionContext)
	{
		return LogRule.class==parameterContext.getParameter().getType();
	}

	@Override
	public Object resolveParameter(
			final ParameterContext parameterContext,
			final ExtensionContext extensionContext)
	{
		return this;
	}

	@Override
	public void afterEach(final ExtensionContext context)
	{
		actualEvents.clear();
		afterEach.forEach(Runnable::run);
		afterEach.clear();
	}


	private LogRule()
	{
		// just make private
	}
}
