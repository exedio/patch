package com.exedio.cope.patch;

import com.exedio.cope.patch.other.PatchInitiatorStackTraceTest;

/**
 * Helper class for {@link PatchInitiatorStackTraceTest} which must in difference to the test class
 * reside in package com.exedio.cope.patch.
 */
public class PatchInitiatorStackTraceTestHelper
{
	public String getInitiator()
	{
		return PatchInitiator.createFromStackTrace().toString();
	}
}
