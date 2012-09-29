package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.util.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.HomepageResourceCountCache;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.service.resource.ResourceService;

/**
 * $Id$
 * 
 * ScheduledProcess to reprocess all datasets.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
public class RebuildHomepageCache extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final long serialVersionUID = 7987123045870435222L;
    @Autowired
    private ResourceService resourceService;

    @Override
    public String getDisplayName() {
        return "Reprocess homepage cache";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return HomepageGeographicKeywordCache.class;
    }

    @Override
    public boolean isShouldRunOnce() {
        return false;
    }

    @Override
    public void process(HomepageGeographicKeywordCache cache) {
        logger.info("rebuilding homepage cache");
        resourceService.delete(resourceService.findAll(HomepageGeographicKeywordCache.class));
        resourceService.save(resourceService.getISOGeographicCounts());
        resourceService.delete(resourceService.findAll(HomepageResourceCountCache.class));
        resourceService.save(resourceService.getResourceCounts());
        logger.info("done cache");
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public List<Long> getPersistableIdQueue() {
        return new ArrayList<Long>(Arrays.asList(-1L));
    }

    @Override
    public void processBatch(List<Long> batch) throws Exception {
        process(null);
    }

}
