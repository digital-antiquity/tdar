package org.tdar.core.bean.cache;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "weekly_popular_resource_cache")
public class WeeklyPopularResourceCache extends Persistable.Base implements Comparable<WeeklyPopularResourceCache>, ResourceCache<Resource> {

    private static final long serialVersionUID = 4401314235170180736L;

    @OneToOne
    private Resource resource;

    public WeeklyPopularResourceCache() {

    }

    public WeeklyPopularResourceCache(Resource resource) {
        this.resource = resource;
    }

    public Double getLogCount() {
        return Math.log(getCount());
    }

    @Override
    public int compareTo(WeeklyPopularResourceCache o) {
        return 1;
    }

    public String getCssId() {
        return this.getKey().getResourceTypeLabel();
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
