package org.tdar.core.service.processes.daily;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;

/**
 * $Id$
 * 
 * ScheduledProcess to rebuild geographic keyword and resource count caches.
 * 
 * How often should this be run?
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
@Scope("prototype")
public class RebuildHomepageCache extends AbstractScheduledProcess {

    private static final long serialVersionUID = 7987123045870435222L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient InformationResourceService informationResourceService;

    @Override
    public String getDisplayName() {
        return "Reprocess homepage cache";
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }
    
    
    @Override
    public void execute() {
        logger.info("rebuilding homepage cache");
        resourceService.evictHomepageMapCache();
        resourceService.getISOGeographicCounts();
        
        resourceService.evictResourceCountCache();
        resourceService.getResourceCounts();

        resourceService.evictDecadeCountCache();
        informationResourceService.findResourcesByDecade();

        resourceService.evictBrowseYearCountCache();
        informationResourceService.findResourceCountsByYear();

        resourceService.getWeeklyPopularResources();
        resourceService.evictPopularResourceCache();

        informationResourceService.getFeaturedItems();
        resourceService.evictHomepageFeaturedItemCache();
        
        logger.info("done caching");
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
