package org.tdar.core.bean.collection;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.AbstractPersistable;

@Table(name = "homepage_featured_collection")
@Entity
public class HomepageFeaturedCollections extends AbstractPersistable {

    private static final long serialVersionUID = -800964224885668226L;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "collection_id")
    @NotNull
    private ResourceCollection featured;

    public ResourceCollection getFeatured() {
        return featured;
    }

    public void setFeatured(ResourceCollection featured) {
        this.featured = featured;
    }

}
