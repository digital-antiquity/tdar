package org.tdar.search.service.processes.upgradeTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.search.service.index.SearchIndexService;

@Component
public class PartialReindexProjectTitleProcess extends AbstractScheduledBatchProcess<Resource> {

    private static final long serialVersionUID = -3612408699729429857L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SearchIndexService searchIndexService;

    private boolean completed = false;

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Reindex Project Title";
    }

    @Override
    public Class<Resource> getPersistentClass() {
        return Resource.class;
    }

    @Override
    public boolean isEnabled() {
        return true;// geoSearchService.isEnabled();
    }

    @Override
    public synchronized void execute() {
        searchIndexService.partialIndexProject();
        completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public void process(Resource resource) {
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public int getBatchSize() {
        return 30;
    }
}
