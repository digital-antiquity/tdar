package org.tdar.core.bean.entity;

import java.util.Arrays;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

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
@Table(name = "resource_creator")
public class ResourceCreator extends Persistable.Sequence<ResourceCreator> implements HasResource<Resource> {

    private static final long serialVersionUID = 7641781600023145104L;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @IndexedEmbedded
    @JoinColumn(nullable = false, name = "creator_id")
    @NotNull
    @BulkImportField(implementedSubclasses = { Person.class, Institution.class }, label = "Resource Creator", order = 1)
    private Creator creator;

    @Enumerated(EnumType.STRING)
    @Field
    @BulkImportField(label = "Resource Creator Role", comment = BulkImportField.CREATOR_ROLE_DESCRIPTION, order = 200)
    private ResourceCreatorRole role;

    public ResourceCreator(Creator creator, ResourceCreatorRole role) {
        setCreator(creator);
        setRole(role);
    }
    
    @Override
    public java.util.List<?> getEqualityFields() {
        return Arrays.asList(creator, role);
    };


    public ResourceCreator() {
    }

    @XmlElementRef
    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @XmlAttribute
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Validatable#isValid()
     */
    public boolean isValid() {
        if (role == null || creator == null) {
            logger.trace(String.format("role:%s creator:%s ", role, creator));
            return false;
        }
        return true;
    }

    public boolean isValidForResource(Resource resource) {
        try {
            boolean relevant = getRole().isRelevantFor(getCreatorType(), resource.getResourceType());
            if (!relevant) {
                Object[] tmp = { getRole(), resource, resource.getResourceType() };
                logger.debug("role {} is not relevant for resourceType {} for {}", tmp);
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

    @Transient
    public final String getCreatorRoleIdentifier() {
        return getCreatorRoleIdentifier(this.getCreator(), this.getRole());
    }

    @Transient
    public static final String getCreatorRoleIdentifier(Creator creatorToFormat, ResourceCreatorRole creatorRole) {
        String toReturn = "";
        if (creatorToFormat != null && creatorToFormat.getCreatorType() != null) {
            String code = creatorToFormat.getCreatorType().getCode();
            String role = "";
            if (creatorRole != null) {
                role = creatorRole.name();
            }
            if (isNullOrTransient(creatorToFormat)) {
                throw new TdarRecoverableRuntimeException("creator id should never be -1 in search query");
            }
            toReturn = String.format("%s_%s_%s", code, creatorToFormat.getId(), role).toLowerCase();
        }
        return toReturn;
    }
}
