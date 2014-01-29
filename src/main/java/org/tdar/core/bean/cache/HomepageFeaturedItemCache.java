package org.tdar.core.bean.cache;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResource;

/**
 * This caches a one or a set of Resources on the homepage.
 * 
 * @author abrin
 *
 */
@Entity
@Table(name = "homepage_featured_item_cache")
public class HomepageFeaturedItemCache extends Persistable.Base implements Comparable<HomepageFeaturedItemCache>, ResourceCache<InformationResource> {

    private static final long serialVersionUID = 4401314235170180736L;

    @OneToOne
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
        return this.getKey().getResourceTypeLabel();
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
