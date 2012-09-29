package org.tdar.core.bean.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.Resource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 * This is the class to build the relationships between creators and resources. These relationships include a role, which may depend
 * on the resource type and creator type.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@XStreamAlias("resourceCreator")
@Table(name = "resource_creator")
public class ResourceCreator extends Persistable.Sequence<ResourceCreator> implements HasResource<Resource> {

    private static final long serialVersionUID = 7641781600023145104L;

    @ManyToOne(optional = false)
    private Resource resource;

    @ManyToOne(optional = false)
    @IndexedEmbedded
    @BulkImportField(implementedSubclasses = { Person.class, Institution.class }, label = "Resource Creator", order = 1)
    private Creator creator;

    @Enumerated(EnumType.STRING)
    @Field
    @BulkImportField(label = "Resource Creator Role", comment = BulkImportField.CREATOR_ROLE_DESCRIPTION, order = 200)
    private ResourceCreatorRole role;

    public ResourceCreator(Resource resource, Creator creator, ResourceCreatorRole role) {
        setResource(resource);
        setCreator(creator);
        setRole(role);
    }

    public ResourceCreator() {
    }

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @XmlElementRef
    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public ResourceCreatorRole getRole() {
        return role;
    }

    public void setRole(ResourceCreatorRole role) {
        this.role = role;
    }

    @Transient
    public CreatorType getCreatorType() {
        if (getCreator() != null) {
            return getCreator().getCreatorType();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", creator, role);
    }

    public boolean isValid() {
        if (role == null || creator == null || resource == null) {
            logger.trace(String.format("role:%s creator:%s resource:%s", role, creator, resource));
            return false;
        }
        try {
            boolean relevant = getRole().isRelevantFor(getCreatorType(), getResource().getResourceType());
            if (!relevant) {
                Object[] tmp = { getRole(), getResource(), getResource().getResourceType() };
                logger.debug(String.format("role {} is not relevant for resourceType {} for {}", tmp));
            }
            return relevant;
        } catch (Exception e) {
            logger.debug("an error occurred when trying to validate a ResourceCreator", e);
        }
        return false;
    }

    public boolean isValidForController() {
        return true;
    }
}
