package org.tdar.core.cache;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.tdar.core.bean.resource.ResourceType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This caches the counts of resource types for the homepage.
 * 
 * @author abrin
 * 
 */
@JsonAutoDetect
public class HomepageResourceCountCache implements Comparable<HomepageResourceCountCache>, ResourceCache<ResourceType>, Serializable {

    private static final long serialVersionUID = 1808499288414868010L;

    private Long count;

    private ResourceType resourceType;

    public HomepageResourceCountCache() {

    }

    public HomepageResourceCountCache(ResourceType resourceType, Long count) {
        this.setResourceType(resourceType);
        this.count = count;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public Double getLogCount() {
        return Math.log(getCount());
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public int compareTo(HomepageResourceCountCache o) {
        return ObjectUtils.compare(getResourceType(), o.getResourceType());
    }

    @Override
    public String getLabel() {
        return getResourceType().getPlural();
    }

    @Override
    public ResourceType getKey() {
        return getResourceType();
    }

    @Override
    public String getCssId() {
        return this.getKey().name();
    }
}
