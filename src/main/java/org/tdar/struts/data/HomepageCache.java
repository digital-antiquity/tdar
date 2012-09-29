/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.data;

import java.util.Map;

import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.utils.Pair;

/**
 * ProxyObject for the cache data used by the homepage
 * 
 * @author Adam Brin
 * 
 */
public class HomepageCache {

    private Map<GeographicKeyword, Pair<Long, Double>> countryCount;
    private Map<ResourceType, Pair<Long,Double>> resourceCount;

    /**
     * @param countryCount
     *            the countryCount to set
     */
    public void setCountryCount(Map<GeographicKeyword, Pair<Long, Double>> countryCount) {
        this.countryCount = countryCount;
    }

    /**
     * @return the countryCount
     */
    public Map<GeographicKeyword, Pair<Long, Double>> getCountryCount() {
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
