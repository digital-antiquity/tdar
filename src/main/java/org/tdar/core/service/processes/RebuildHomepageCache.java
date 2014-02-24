package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.BrowseDecadeCountCache;
import org.tdar.core.bean.cache.BrowseYearCountCache;
import org.tdar.core.bean.cache.HomepageFeaturedItemCache;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.cache.WeeklyPopularResourceCache;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.AggregateViewStatistic;

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
public class RebuildHomepageCache extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final long serialVersionUID = 7987123045870435222L;
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private InformationResourceService informationResourceService;

    @Override
    public String getDisplayName() {
        return "Reprocess homepage cache";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return HomepageGeographicKeywordCache.class;
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
        resourceService.deleteAll(HomepageGeographicKeywordCache.class);
        resourceService.save(resourceService.getISOGeographicCounts());
        resourceService.deleteAll(HomepageResourceCountCache.class);
        resourceService.save(resourceService.getResourceCounts());
        resourceService.deleteAll(BrowseDecadeCountCache.class);
        resourceService.save(informationResourceService.findResourcesByDecade(Status.ACTIVE));
        resourceService.deleteAll(HomepageFeaturedItemCache.class);
        resourceService.deleteAll(BrowseYearCountCache.class);
        resourceService.deleteAll(WeeklyPopularResourceCache.class);
        resourceService.save(informationResourceService.findResourcesByYear(Status.ACTIVE));
        


        List<HomepageFeaturedItemCache> hfic = new ArrayList<HomepageFeaturedItemCache>();
        Long featuredCollectionId = getTdarConfiguration().getFeaturedCollectionId();
        for (Object res : informationResourceService.findRandomFeaturedResourceInCollection(true, featuredCollectionId, 5)) {
            hfic.add(new HomepageFeaturedItemCache((InformationResource) res));
        }

        logger.debug("homepage featured item cache ({})",hfic.size());
        List<WeeklyPopularResourceCache> wrc = new ArrayList<WeeklyPopularResourceCache>();

        int max = 20;
        DateTime end = new DateTime();
        DateTime start = end.minusDays(7);
        List<AggregateViewStatistic> aggregateUsageStats = resourceService.getOverallUsageStats(start.toDate(), end.toDate(), 40L);
        if (CollectionUtils.isNotEmpty(aggregateUsageStats)) {
            Set<Long> seen = new HashSet<>();
            for (AggregateViewStatistic avs : aggregateUsageStats) {
                Long resourceId = avs.getResourceId();
                // handling unique resource ids across the timeperiod
                if (seen.contains(resourceId)) {
                    continue;
                }
                
                if (max == 0) {
                    break;
                }
                max--;
                
                seen.add(resourceId);
                Resource resource = resourceService.find(resourceId);
                if (resource != null && resource.isActive()) {
                    wrc.add(new WeeklyPopularResourceCache(resource));
                }
            }
        }
        logger.debug("weekly popular stats ({})",wrc.size());
        resourceService.save(wrc);
        resourceService.save(hfic);

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
