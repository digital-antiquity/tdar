package org.tdar.core.service.processes.upgradeTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.resource.ResourceService;

@Component
public class RebuildGeographicKeywordProcess extends AbstractScheduledBatchProcess<Resource> {

    private static final long serialVersionUID = -1389096329990660324L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient GenericService genericService;

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Rebuild Geographic Keywords";
    }

    @Override
    public Class<Resource> getPersistentClass() {
        return Resource.class;
    }

    @Override
    public boolean isEnabled() {
        return false;// geoSearchService.isEnabled();
    }

    @Override
    public void process(Resource resource) {
        if ((resource != null) && (resource.getActiveLatitudeLongitudeBoxes() != null) && (resource.getActiveLatitudeLongitudeBoxes().size() > 0)) {
            logger.trace("GeoCleanup progress tdarId: " + resource.getId());
            resourceService.processManagedKeywords(resource, resource.getActiveLatitudeLongitudeBoxes());
            logger.debug("final keywords: " + resource.getManagedGeographicKeywords());
            genericService.save(resource.getManagedGeographicKeywords());
            genericService.saveOrUpdate(resource);
        }
    }

    @Override
    public int getBatchSize() {
        return 30;
    }
}
