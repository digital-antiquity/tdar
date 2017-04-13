package org.tdar.core.bean.resource.ref;

import org.hibernate.annotations.Immutable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

import javax.persistence.*;

/**
 * Created by jimdevos on 4/13/17.
 */
@Entity
@Immutable
@Table(name="resource")
public final class ResourceRef
{

    @Id
    private Long id = null;

    @MapsId @OneToOne @JoinColumn(name="id")
    private Resource resource;

    @Column
    private String title;

    @Column
    private String description;

    @Column(name="resource_type")
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
