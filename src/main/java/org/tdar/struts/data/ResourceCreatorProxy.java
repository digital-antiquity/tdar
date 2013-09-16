package org.tdar.struts.data;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // In the rare situation where javascript does not catch/correct an invalid creator proxy in client form, we need to
    // raise exceptions w/ human readable feedback so user has at least some idea what they need to fix.
    private static final String ERR_DETERMINE_CREATOR_INSUFFICIENT_INFO = "This resource CreatorProxy was initialized improperly";
    private static final String ERR_FMT2_DETERMINE_CREATOR_TOO_MUCH_INFO = "" +
            "There was a problem with one of your author/creator/contributor entries. " +
            "a single creator record may contain either person name, but the system encountered both. Please revise this record with " +
            "either \"%s\" or \"%s\" (internal type was %s).";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
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

    private CreatorType type = CreatorType.PERSON;

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
                if (determineActualCreatorType() == CreatorType.PERSON) {
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

    /**
     * figure out whether this proxy object represents a person or an institution.
     * 
     * @throws TdarRecoverableRuntimeException
     *             if system cannot reliably determine creatorType. Use {@link #getActualCreatorType() getActualCreatorType} if you do not want to deal w/
     *             exceptions (e.g. calling from freemarker).
     * 
     * @return creatorType, if system can figure out based on available info. otherwise null.
     */
    private CreatorType determineActualCreatorType() {
        if (institution == null && person == null) {
            throw new TdarRecoverableRuntimeException(ERR_DETERMINE_CREATOR_INSUFFICIENT_INFO);
        }
        if (institution.hasNoPersistableValues() && person.hasNoPersistableValues()) {
            return null;
        }
        if (!institution.hasNoPersistableValues() && !person.hasNoPersistableValues()) {
            String err = String.format(ERR_FMT2_DETERMINE_CREATOR_TOO_MUCH_INFO, getPerson(), getInstitution(), getType());
            logger.warn(err);
            return type;
        }

        if (!person.hasNoPersistableValues()) {
            return CreatorType.PERSON;
        } else {
            return CreatorType.INSTITUTION;
        }
    }

    @Transient
    public CreatorType getActualCreatorType() {
        CreatorType creatorType = null;
        try {
            creatorType = determineActualCreatorType();
        } catch (TdarRecoverableRuntimeException trex) {
            logger.warn("Cannot derive actual creator: the incoming proxy is either incomplete or ambiguous: {}", this);
        }
        return creatorType;
    }

    /**
     * Can this proxy return a valid ResourceCreator instance in its current
     * state.
     * 
     * @return
     */
    public boolean isValid() {
        if (person != null && !person.hasNoPersistableValues()) {
            return true;
        }
        if (institution != null && !institution.hasNoPersistableValues()) {
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

    public CreatorType getType() {
        return type;
    }

    public void setType(CreatorType type) {
        this.type = type;
    }

}
