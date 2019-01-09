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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import java.util.List;

public class AssertionFailedLogAppender implements Appender<ILoggingEvent>
{
	@Override
	public String getName()
	{
		throw new AssertionError();
	}

	@Override
	public void doAppend(final ILoggingEvent iLoggingEvent) throws LogbackException
	{
		throw new AssertionError();
	}

	@Override
	public void setName(final String s)
	{
		throw new AssertionError(s);
	}

	@Override
	public void setContext(final Context context)
	{
		throw new AssertionError();
	}

	@Override
	public Context getContext()
	{
		throw new AssertionError();
	}

	@Override
	public void addStatus(final Status status)
	{
		throw new AssertionError();
	}

	@Override
	public void addInfo(final String s)
	{
		throw new AssertionError();
	}

	@Override
	public void addInfo(final String s, final Throwable throwable)
	{
		throw new AssertionError();
	}

	@Override
	public void addWarn(final String s)
	{
		throw new AssertionError();
	}

	@Override
	public void addWarn(final String s, final Throwable throwable)
	{
		throw new AssertionError();
	}

	@Override
	public void addError(final String s)
	{
		throw new AssertionError();
	}

	@Override
	public void addError(final String s, final Throwable throwable)
	{
		throw new AssertionError();
	}

	@Override
	public void addFilter(final Filter<ILoggingEvent> filter)
	{
		throw new AssertionError();
	}

	@Override
	public void clearAllFilters()
	{
		throw new AssertionError();
	}

	@Override
	public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList()
	{
		throw new AssertionError();
	}

	@Override
	public FilterReply getFilterChainDecision(final ILoggingEvent iLoggingEvent)
	{
		throw new AssertionError();
	}

	@Override
	public void start()
	{
		throw new AssertionError();
	}

	@Override
	public void stop()
	{
		throw new AssertionError();
	}

	@Override
	public boolean isStarted()
	{
		throw new AssertionError();
	}
}
