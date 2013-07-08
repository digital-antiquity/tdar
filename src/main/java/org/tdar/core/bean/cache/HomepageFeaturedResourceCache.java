package org.tdar.core.bean.cache;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.ObjectUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "homepage_featured_item_cache")
public class HomepageFeaturedResourceCache extends Persistable.Base implements Comparable<HomepageFeaturedResourceCache>, ResourceCache<Resource> {

    private static final long serialVersionUID = 4401314235170180736L;

    @OneToOne
    private Resource resource;

    public HomepageFeaturedResourceCache() {

    }

    public HomepageFeaturedResourceCache(Resource resource) {
        this.setResource(resource);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String getLabel() {
        return getResource().getTitle();
    }

    @Override
    public Long getCount() {
        return 1l;
    }

    @Override
    public Resource getKey() {
        return resource;
    }

    @Override
    public Double getLogCount() {
        return 1.0;
    }

    @Override
    public String getCssId() {
        return String.format("%s-%s", getKey().getResourceType().name(),getKey().getId());
    }

    @Override
    public int compareTo(HomepageFeaturedResourceCache o) {
        return ObjectUtils.compare(getKey(), o.getKey());
    }

}
