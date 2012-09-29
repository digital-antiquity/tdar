/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.HomepageCache;
import org.springframework.scheduling.annotation.Async;

/**
 * @author Adam Brin
 * 
 */

@Service
@Transactional
public class SimpleCachingService {

    @Autowired
    private transient ResourceService resourceService;

    private HomepageCache homepageCache = null;

    private Logger logger = Logger.getLogger(getClass());
    /**
     * @return the homepagecache
     */
    public synchronized HomepageCache getHomepageCache() {
        if (homepageCache == null) {
            homepageCache = new HomepageCache();
            setupHomepageCache();
        }
        return homepageCache;
    }

    @Async
    public void setupHomepageCache() {
        logger.info("initializing homepage caches");
        getHomepageCache().setCountryCount(resourceService.getISOGeographicCounts());
        getHomepageCache().setResourceCount(resourceService.getResourceCounts());
    }

    public void taintAll() {
        homepageCache = null;
    }

    public void taintAllAndRebuild() {
        taintAll();
        setupHomepageCache();
    }
}
