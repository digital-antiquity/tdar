package org.tdar.core.bean.resource;

import javax.persistence.ManyToOne;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.JSONTransient;

public class ResourceRelationship extends Persistable.Sequence<ResourceRelationship> implements HasResource<Resource> {

    public enum ResourceRelationshipType implements HasLabel {
        ISPARTOF("Is Part Of"),
        ISRELATEDTO("Is Related To");
        
        private String label;

        private ResourceRelationshipType(String label) {
            this.setLabel(label);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    private static final long serialVersionUID = 3569888913035673434L;

    
    @ManyToOne(optional = false)
    private Resource resource;
    
    private String heading;
    
    private String description;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @ManyToOne(optional = false)
    private Resource targetResource;

    
    @Override
    @JSONTransient
    public boolean isValidForController() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    @JSONTransient
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    public Resource getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(Resource targetResource) {
        this.targetResource = targetResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }


}
