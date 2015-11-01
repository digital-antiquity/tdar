package org.tdar.core.service.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.tdar.core.configuration.TdarConfiguration;

@Scope("prototype")
public abstract class AbstractScheduledProcess implements ScheduledProcess {

    private static final long serialVersionUID = 38507177698911172L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

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
