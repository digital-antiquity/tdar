package org.tdar.core.service.processes;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.geosearch.GeoSearchService;

@Component
public class RebuildGeographicKeywordProcess extends ScheduledProcess.Base<Resource> {

    private static final long serialVersionUID = -1389096329990660324L;

    @Autowired
    private GeoSearchService geoSearchService;

    @Autowired
    private ResourceService resourceService;

    private final Logger logger = Logger.getLogger(getClass());

    public boolean isShouldRunOnce() {
        return true;
    }

    public String getDisplayName() {
        return "Rebuild Geographic Keywords";
    }

    public Class<Resource> getPersistentClass() {
        return Resource.class;
    }

    @Override
    public boolean isConfigured() {
        return geoSearchService.isEnabled();
    }

    @Override
    public void process(Resource resource) {
        if (resource != null && resource.getActiveLatitudeLongitudeBoxes() != null && resource.getActiveLatitudeLongitudeBoxes().size() > 0) {
            logger.trace("GeoCleanup progress tdarId: " + resource.getId());
            resourceService.processManagedKeywords(resource, resource.getActiveLatitudeLongitudeBoxes());
            logger.debug("final keywords: " + resource.getManagedGeographicKeywords());
            resourceService.save(resource.getManagedGeographicKeywords());
            resourceService.saveOrUpdate(resource);
        }
    }

    @Override
    public int getBatchSize() {
        return 30;
    }
}
