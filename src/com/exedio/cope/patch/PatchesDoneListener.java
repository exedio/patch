package com.exedio.cope.patch;

import com.exedio.cope.util.JobContext;

@FunctionalInterface
public interface PatchesDoneListener
{
	/**
	 * This method is called once when all patches are marked as done,
	 * so either after the patches {@link Patches#run(JobContext) ran} or were {@link Patches#preempt() preempted}.
	 */
	void notifyPatchesDone();
}
