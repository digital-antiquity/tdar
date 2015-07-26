package org.tdar.core.service.processes;

import java.io.Serializable;

/**
 * Class to manage scheduled processes, both upgrade tasks and scheduled tasks. It allows for batch processing.
 * 
 * @author abrin
 * 
 * @param <P>
 */
public interface ScheduledProcess extends Serializable {

    /**
     * Entry point into the logical work that this scheduled process should perform.
     */
    void execute();

    boolean isEnabled();

    boolean shouldRunAtStartup();

    String getDisplayName();

    /**
     * Returns true if this process has run to completion.
     * 
     * @return
     */
    boolean isCompleted();

    /**
     * Performs any cleanup on this process, emailing any accumulated errors, etc.
     * Also resets this process to its initial state, typically clearing its completion status as well.
     * After invoking this method isCompleted() should return false again.
     */
    void cleanup();

    /**
     * Returns true if this ScheduledProcess should only run once during the webapp VM lifecycle.
     * 
     * @return
     */
    boolean isSingleRunProcess();

}