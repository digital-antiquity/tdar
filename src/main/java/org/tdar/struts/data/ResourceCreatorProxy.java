package org.tdar.struts.data;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

/**
 * $Id$
 * 
 * Utility class for easily passing resource creators between controller and
 * view
 * 
 * 
 * @author <a href='mailto:james.t.devos@asu.edu'>Jim deVos</a>
 * @version $Rev$
 */
public class ResourceCreatorProxy implements Comparable<ResourceCreatorProxy> {
    private Logger logger = Logger.getLogger(ResourceCreatorProxy.class);
    private boolean initialized = false;

    // either person or institution will be updated by the view and then
    // conditionally placed in the resourceCreator
    private Person person = new Person();
    private Institution institution = new Institution();
    private ResourceCreator resourceCreator = new ResourceCreator();

    // Once we are able to resolve the resource creator type we will set that resourceCreator's role.
    // private ResourceCreatorRole personRole = ResourceCreatorRole.AUTHOR;
    // private ResourceCreatorRole institutionRole = ResourceCreatorRole.AUTHOR;
    private ResourceCreatorRole role = ResourceCreatorRole.AUTHOR;

    public ResourceCreatorProxy() {
        // TODO: set any defaults here?

    }

    public ResourceCreatorProxy(Creator creator, ResourceCreatorRole role) {
        if (creator instanceof Person) {
            this.person = (Person) creator;
        } else {
            this.institution = (Institution) creator;
        }
        this.role = role;
    }

    public ResourceCreatorProxy(ResourceCreator rc) {
        this.resourceCreator = rc;
        initialized = true;
        if (rc.getCreator() instanceof Person) {
            this.person = (Person) rc.getCreator();
        } else {
            this.institution = (Institution) rc.getCreator();
        }
        this.role = rc.getRole();
    }

    // properly set the state of the resourceCreator field by determining if the proxy represents a person or an institution
    private void resolveResourceCreator() {
        if (!initialized) {
            try {
                if (getActualCreatorType() == CreatorType.PERSON) {
                    resourceCreator.setCreator(person);
                    Institution institution = person.getInstitution();
                    // FIXME: what is the purpose of this check?
                    if (institution == null || StringUtils.isBlank(institution.getName())) {
                        person.setInstitution(null);
                    }
                    logger.trace("creator type implicitly set to person:" + person);
                } else {
                    resourceCreator.setCreator(institution);
                    logger.trace("creator type implicitly set to person:" + institution);
                }
                resourceCreator.setRole(role);
            } catch (NullPointerException npe) {
                logger.warn("no resource creator was initialized becase no creator was set");
            }
            initialized = true;
        }
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public ResourceCreator getResourceCreator() {
        if (!initialized)
            resolveResourceCreator();
        return resourceCreator;
    }

    @Transient
    public CreatorType getActualCreatorType() {
        // figure out from the form if this proxy is a person or an institution
        if (institution == null && person == null) {
            throw new TdarRecoverableRuntimeException("This resource CreatorProxy was initialized improperly");
        }
        if (institution.hasNoPersistableValues() && person.hasNoPersistableValues()) {
            return null;
        }
        if (!institution.hasNoPersistableValues() && !person.hasNoPersistableValues()) {
            throw new TdarRecoverableRuntimeException("Both Proxies were Populated");
        }

        if (!person.hasNoPersistableValues()) {
            return CreatorType.PERSON;
        } else {
            return CreatorType.INSTITUTION;
        }
    }

    /**
     * Can this proxy return a valid ResourceCreator instance in its current
     * state.
     * 
     * @return
     */
    public boolean isValid() {
        if (person != null && !StringUtils.isBlank(person.getLastName())) {
            return true;
        }
        if (institution != null && !StringUtils.isBlank(institution.getName())) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format("[ResourceCreatorProxy@%s  role:%s rc:%s  p:%s  i:%s]", this.hashCode(), role, resourceCreator, person, institution);
    }

    @Override
    public int compareTo(ResourceCreatorProxy that) {
        return this.getResourceCreator().compareTo(that.getResourceCreator());
    }

    public ResourceCreatorRole getRole() {
        return role;
    }

    public void setRole(ResourceCreatorRole role) {
        this.role = role;
    }

}
