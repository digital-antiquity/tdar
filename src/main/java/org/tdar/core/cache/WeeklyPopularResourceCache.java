package org.tdar.core.cache;

import java.io.Serializable;

import org.tdar.core.bean.resource.Resource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class WeeklyPopularResourceCache implements Comparable<WeeklyPopularResourceCache>, ResourceCache<Resource>, Serializable {

    private static final long serialVersionUID = 589291882710378333L;

    private Resource resource;

    public WeeklyPopularResourceCache() {

    }

    public WeeklyPopularResourceCache(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Double getLogCount() {
        return Math.log(getCount());
    }

    @Override
    public int compareTo(WeeklyPopularResourceCache o) {
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

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Long getCount() {
        return 1L;
    }

    @Override
    public Resource getKey() {
        return resource;
    }
}
