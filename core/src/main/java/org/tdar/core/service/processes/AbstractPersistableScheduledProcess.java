package org.tdar.core.service.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.Persistable;

public abstract class AbstractPersistableScheduledProcess<Q extends Persistable> extends AbstractScheduledProcess {

    private static final long serialVersionUID = 915599886023675766L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Long lastId;

    private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

    public abstract Class<Q> getPersistentClass();

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

    @Override
    public boolean shouldRunAtStartup() {
        return false;
    }

}
