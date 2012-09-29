package org.tdar.struts.data;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;

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
    private ResourceCreatorRole personRole = ResourceCreatorRole.AUTHOR;
    private ResourceCreatorRole institutionRole = ResourceCreatorRole.AUTHOR;

    public ResourceCreatorProxy() {
        // TODO: set any defaults here?
    }

    public ResourceCreatorProxy(Creator creator,ResourceCreatorRole role) {
        if (creator instanceof Person) {
            this.person = (Person) creator;
            this.personRole = role;
        } else {
            this.institution = (Institution) creator;
            this.institutionRole = role;
        }
        resolveResourceCreator();
    }

    public ResourceCreatorProxy(ResourceCreator rc) {
        this.resourceCreator = rc;
        if (rc.getCreator() instanceof Person) {
            this.person = (Person) rc.getCreator();
            this.personRole = rc.getRole();
        } else {
            this.institution = (Institution) rc.getCreator();
            this.institutionRole = rc.getRole();
        }
        // FIXME: should continue to refactor to avoid duplicate checks on person/institution in resolveResourceCreator
        // in this case
        resolveResourceCreator();
    }

    public ResourceCreator resolveResourceCreator() {
        if (!initialized) {
            if (getActualCreatorType() == CreatorType.PERSON) {
                resourceCreator.setCreator(person);
                resourceCreator.setRole(personRole);
                Institution institution = person.getInstitution();
                // FIXME: what is the purpose of this check?
                if (institution == null || StringUtils.isBlank(institution.getName())) {
                    person.setInstitution(null);
                }
                logger.trace("creator type implicitly set to person:" + person);
            } else {
                resourceCreator.setCreator(institution);
                resourceCreator.setRole(institutionRole);
                logger.trace("creator type implicitly set to person:" + institution);
            }
            initialized = true;
        }
        return resourceCreator;
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
        return resourceCreator;
    }

    public void setResourceCreator(ResourceCreator resourceCreator) {
        this.resourceCreator = resourceCreator;
    }

    public CreatorType getActualCreatorType() {
        // figure out from the form if this proxy is a person or an
        // institution
        if (StringUtils.isBlank(institution.getName())) {
            return CreatorType.PERSON;
        } else {
            return CreatorType.INSTITUTION;
        }

    }

    public ResourceCreatorRole getPersonRole() {
        return personRole;
    }

    public void setPersonRole(ResourceCreatorRole personRole) {
        this.personRole = personRole;
    }

    public ResourceCreatorRole getInstitutionRole() {
        return institutionRole;
    }

    public void setInstitutionRole(ResourceCreatorRole institutionRole) {
        this.institutionRole = institutionRole;
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
        return String.format("[ResourceCreatorProxy@%s  type:%s rc:%s  p:%s  i:%s]", this.hashCode(), getActualCreatorType(), resourceCreator, person,
                institution);
    }

    @Override
    public int compareTo(ResourceCreatorProxy that) {
        return this.getResourceCreator().compareTo(that.getResourceCreator());
    }

}
