package org.tdar.core.bean.resource.ref;

import org.hibernate.annotations.Immutable;
import org.tdar.core.bean.collection.ResourceCollection;

import javax.persistence.*;

/**
 * Created by jimdevos on 4/13/17.
 */
@Entity
@Immutable
@Table(name="collection")
public final class CollectionRef  {

    @Id
    private Long id;

    @MapsId @OneToOne @JoinColumn(name="id")
    private ResourceCollection resourceCollection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }
}
