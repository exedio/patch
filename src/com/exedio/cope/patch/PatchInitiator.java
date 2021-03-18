package com.exedio.cope.patch;

public final class PatchInitiator
{
	private final String initiator;

	PatchInitiator(final String initiator)
	{
		this.initiator = initiator;
	}

	String getInitiator()
	{
		return initiator;
	}

	/**
	 * Creates a new PatchInitiator based on execution stack.
	 * Takes the simple class name from first stack element which belongs not to this framework.
	 *
	 * If you are acting with any Servlet or servlet.Filter its recommended to use ServletPatchInitiatorUtil
	 */
	public static PatchInitiator createFromStackTrace()
	{
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		String result = "unknown";
		// start at 1 as element[0] is always the Thread.currentThread().getStackTrace() call
		for (int i = 1; i < stackTrace.length; i++)
		{

			final String className = stackTrace[i].getClassName();
			final int pos = className.lastIndexOf('.');
			final String packageName = pos > 0 ? className.substring(0, pos) : "";
			if (! PatchInitiator.class.getPackage().getName().equals(packageName))
			{
				result = className.substring(pos+1);
				break;
			}
		}
		return new PatchInitiator(result);
	}

	@Override
	public String toString()
	{
		return initiator;
	}
}
