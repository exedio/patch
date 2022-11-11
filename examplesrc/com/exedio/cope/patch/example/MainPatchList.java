/*
 * Copyright (C) 2004-2015  exedio GmbH (www.exedio.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.exedio.cope.patch.example;

import static com.exedio.cope.patch.example.MainPatches.add;

import com.exedio.cope.util.JobContext;
import com.exedio.cope.util.JobStop;
import java.time.Duration;

final class MainPatchList
{
	static void addAll()
	{
		// insert new patches here !!!
		// new patches on top
		add(new AbstractPatch()
		{

			@Override
			public void run(final JobContext context)
			{
				try
				{
					for (int cnt = 1; cnt <= 10; cnt++)
					{
						context.sleepAndStopIfRequested(Duration.ofSeconds(1));
						if (context.supportsProgress())
							context.incrementProgress();
					}
				}
				catch (final JobStop e)
				{
					if (context.supportsMessage())
						context.setMessage("Job Stopped: "+e.getMessage());
					throw e;
				}
			}

			@Override
			public String getID()
			{
				return "JavaPatch with progress";
			}
		} );
		add(new AbstractPatch()
		{

			@Override
			public void run(final JobContext context)
			{
				try
				{
					for (int cnt = 1; cnt <= 12; cnt++)
					{
						context.sleepAndStopIfRequested(Duration.ofSeconds(1));
						if (context.supportsCompleteness())
							context.setCompleteness(((double) cnt) / 12d);
					}
				}
				catch (final JobStop e)
				{
					if (context.supportsMessage())
						context.setMessage("Job Stopped: "+e.getMessage());
					throw e;
				}
			}

			@Override
			public String getID()
			{
				return "JavaPatch with completeness";
			}
		} );
		add(new AbstractPatch()
		{

			@Override
			public void run(final JobContext context)
			{
				if (context.supportsMessage())
					context.setMessage("Attempt to do nothing - 15 times");
				try
				{
					for (int cnt = 1; cnt <= 15; cnt++)
					{
						context.sleepAndStopIfRequested(Duration.ofSeconds(1));
						if (context.supportsMessage())
							context.setMessage("Did nothing in "+(cnt == 1 ? "1st" : (cnt == 2 ? "2nd" : (cnt == 3 ? "3rd" : (""+cnt+"th")))) +" iteration.");
					}
				}
				catch (final JobStop e)
				{
					if (context.supportsMessage())
						context.setMessage("Job Stopped: "+e.getMessage());
					throw e;
				}
			}

			@Override
			public String getID()
			{
				return "JavaPatch with messages";
			}
		} );
		add("LongRunningDDL", "INSERT INTO TestTable values (1, SLEEP(30000),'test','test')");
		add("TestTable",
				"CREATE TABLE TestTable(this int,catch int not null,name varchar(80) not null)",
				"INSERT INTO TestTable values (0, SLEEP(15000),'test')",
				"ALTER TABLE TestTable ADD COLUMN description varchar(80)");
		add("CreateHSQLFunction", "CREATE FUNCTION SLEEP(v BIGINT) RETURNS INT LANGUAGE JAVA DETERMINISTIC NO SQL EXTERNAL NAME 'CLASSPATH:com.exedio.cope.patch.example.HSQLSleep.sleep'");
		add(new AbstractPatch()
		{
			@Override
			public boolean isSuppressed()
			{
				return true;
			}

			@Override
			public void run(final JobContext context)
			{
				throw new RuntimeException("suppressed patch must not be run");
			}

			@Override
			public String getID()
			{
				return "Suppressed Patch";
			}
		} );
	}

	private MainPatchList()
	{
		// prevent instantiation
	}
}
