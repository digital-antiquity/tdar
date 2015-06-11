package org.tdar.core.cache;

import java.io.Serializable;

import org.tdar.core.bean.resource.InformationResource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This caches a one or a set of Resources on the homepage.
 * 
 * @author abrin
 * 
 */
@JsonAutoDetect
public class HomepageFeaturedItemCache implements Comparable<HomepageFeaturedItemCache>, ResourceCache<InformationResource>, Serializable {

    private static final long serialVersionUID = 4401314235170180736L;

    private InformationResource resource;

    public HomepageFeaturedItemCache() {

    }

    public HomepageFeaturedItemCache(InformationResource resource) {
        this.resource = resource;
    }

    @Override
    public Double getLogCount() {
        return Math.log(getCount());
    }

    @Override
    public int compareTo(HomepageFeaturedItemCache o) {
        return 1;
    }

    @Override
    public String getCssId() {
        return this.getKey().getResourceType().name();
    }

    @Override
    public String getLabel() {
        return getKey().getTitle();
    }

    public InformationResource getResource() {
        return resource;
    }

    public void setResource(InformationResource resource) {
        this.resource = resource;
    }

    @Override
    public Long getCount() {
        return 1L;
    }

    @Override
    public InformationResource getKey() {
        return resource;
    }
}
