package org.tdar.core.bean.util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.apache.commons.lang.ObjectUtils;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.resource.ResourceType;

@Entity
@Table(name = "homepage_cache_resource_type")
public class HomepageResourceCountCache extends Base implements Comparable<HomepageResourceCountCache> {

    private static final long serialVersionUID = 4401314235170180736L;

    @Column(name = "resource_count")
    private Long count;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceType resourceType;

    public HomepageResourceCountCache() {

    }

    public HomepageResourceCountCache(ResourceType resourceType, Long count) {
        this.setResourceType(resourceType);
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

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

}
