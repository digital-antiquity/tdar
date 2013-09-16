package org.tdar.core.bean.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.TdarConfiguration;

public interface ScheduledProcess<P extends Persistable> extends Serializable {

    /**
     * Entry point into the logical work that this scheduled process should perform.
     */
    public void execute();

    public boolean isEnabled();

    public boolean shouldRunAtStartup();

    public String getDisplayName();

    public Class<P> getPersistentClass();

    /**
     * Returns true if this process has run to completion.
     * 
     * @return
     */
    public boolean isCompleted();

    /**
     * Performs any cleanup on this process, emailing any accumulated errors, etc.
     * Also resets this process to its initial state, typically clearing its completion status as well.
     * After invoking this method isCompleted() should return false again.
     */
    public void cleanup();

    /**
     * Returns true if this ScheduledProcess should only run once during the webapp VM lifecycle.
     * 
     * @return
     */
    public boolean isSingleRunProcess();

    public Long getLastId();

    public void setLastId(Long lastId);

    public static abstract class Base<Q extends Persistable> implements ScheduledProcess<Q> {

        private static final long serialVersionUID = -8630791495469441646L;

        protected final Logger logger = LoggerFactory.getLogger(getClass());

        private Long lastId;

        private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

        /**
         * @return the lastId
         */
        public Long getLastId() {
            return lastId;
        }

        /**
         * @param lastId
         *            the lastId to set
         */
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

        public boolean shouldRunAtStartup() {
            return false;
        }

    }

}