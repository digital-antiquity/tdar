/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.util.Map;

import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.ResourceType;

/**
 * @author Adam Brin
 * 
 */
public class HomepageCache {

    private Map<GeographicKeyword, Long> countryCount;
    private Map<ResourceType, Pair<Long,Double>> resourceCount;

    /**
     * @param countryCount
     *            the countryCount to set
     */
    public void setCountryCount(Map<GeographicKeyword, Long> countryCount) {
        this.countryCount = countryCount;
    }

    /**
     * @return the countryCount
     */
    public Map<GeographicKeyword, Long> getCountryCount() {
        return countryCount;
    }

    /**
     * @param resourceCount
     *            the resourceCount to set
     */
    public void setResourceCount(Map<ResourceType, Pair<Long,Double>> resourceCount) {
        this.resourceCount = resourceCount;
    }

    /**
     * @return the resourceCount
     */
    public Map<ResourceType, Pair<Long,Double>> getResourceCount() {
        return resourceCount;
    }

}
