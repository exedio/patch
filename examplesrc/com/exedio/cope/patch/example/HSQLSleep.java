package com.exedio.cope.patch.example;

import java.sql.SQLException;

public final class HSQLSleep
{
	private HSQLSleep()
	{
	}

	@SuppressWarnings("unused") // OK: is referred to by hsqldb CREATE FUNCTION
	public static int sleep(final long millis) throws java.sql.SQLException
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (final InterruptedException e)
		{
			throw new SQLException(e);
		}
		return 0;
	}
}
