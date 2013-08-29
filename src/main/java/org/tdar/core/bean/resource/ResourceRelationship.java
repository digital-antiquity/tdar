package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;

/**
 * $Id$
 * <p>
 * A persistable pointer to a resource, stored by a registered user of tDAR.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "resource_relationship")
public class ResourceRelationship extends Persistable.Base {


    private static final long serialVersionUID = 2240540556284744345L;

    @ManyToOne(optional = false)
    private Resource sourceResource;

    @ManyToOne(optional = false)
    private Resource targetResource;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", length = 255)
    @Field(norms = Norms.NO, store = Store.YES)
    private ResourceRelationshipType type;

    private enum ResourceRelationshipType implements HasLabel {
        REFERENCES("References"),
        REPLACES("Replaces"),
        VERSION_OF("Version Of");

        private String label;

        private ResourceRelationshipType(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

    }

    public ResourceRelationshipType getType() {
        return type;
    }

    public void setType(ResourceRelationshipType type) {
        this.type = type;
    }

    public Resource getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(Resource targetResource) {
        this.targetResource = targetResource;
    }

    public Resource getSourceResource() {
        return sourceResource;
    }

    public void setSourceResource(Resource sourceResource) {
        this.sourceResource = sourceResource;
    }
}
