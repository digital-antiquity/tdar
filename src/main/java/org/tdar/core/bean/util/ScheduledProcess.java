package org.tdar.core.bean.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * Class to manage scheduled processes, both upgrade tasks and scheduled tasks.  It allows for batch processing. 
 * @author abrin
 *
 * @param <P>
 */
public interface ScheduledProcess<P extends Persistable> extends Serializable {

    /**
     * Entry point into the logical work that this scheduled process should perform.
     */
    void execute();

    boolean isEnabled();

    boolean shouldRunAtStartup();

    String getDisplayName();

    Class<P> getPersistentClass();

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

    Long getLastId();

    void setLastId(Long lastId);

    static abstract class Base<Q extends Persistable> implements ScheduledProcess<Q> {

        private static final long serialVersionUID = -8630791495469441646L;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private Long lastId;

        private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

        /**
         * @return the lastId
         */
        @Override
        public Long getLastId() {
            return lastId;
        }

        /**
         * @param lastId
         *            the lastId to set
         */
        @Override
        public void setLastId(Long lastId) {
            this.lastId = lastId;
        }

        /**
         * @return the logger
         */
        public Logger getLogger() {
            return logger;
        }

        @Override
        public void cleanup() {

        }

        public TdarConfiguration getTdarConfiguration() {
            return tdarConfiguration;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @Override
        public boolean shouldRunAtStartup() {
            return false;
        }

    }

}