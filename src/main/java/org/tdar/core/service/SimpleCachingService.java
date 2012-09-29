/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.utils.HomepageCache;

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

    /**
     * @return the homepagecache
     */
    public synchronized HomepageCache getHomepageCache() {
        if (homepageCache == null) {
            homepageCache = new HomepageCache();
            homepageCache.setCountryCount(resourceService.getISOGeographicCounts());
            homepageCache.setResourceCount(resourceService.getResourceCounts());
        }
        return homepageCache;
    }

    public void taintAll() {
        homepageCache = null;
    }

    public void taintAllAndRebuild() {
        taintAll();
        getHomepageCache();
    }
}
