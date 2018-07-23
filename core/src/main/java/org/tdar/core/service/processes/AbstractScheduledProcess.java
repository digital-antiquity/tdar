package org.tdar.core.service.processes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.tdar.configuration.TdarConfiguration;

@Scope("prototype")
public abstract class AbstractScheduledProcess implements ScheduledProcess {

    private static final long serialVersionUID = 38507177698911172L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected TdarConfiguration config = TdarConfiguration.getInstance();

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

    protected Map<String, Object> initDataModel() {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("siteAcronym", config.getSiteAcronym());
        dataModel.put("siteUrl", config.getBaseUrl());
        dataModel.put("date", new Date());
        return dataModel;
    }

}
