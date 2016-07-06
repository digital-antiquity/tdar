package org.tdar.core.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private boolean initialized = false;
    private Set<String> seenImportFieldNames = new HashSet<>();

    // either person or institution will be updated by the view and then
    // conditionally placed in the resourceCreator
    private Person person = new Person();
    private Institution institution = new Institution();
    private ResourceCreator resourceCreator = new ResourceCreator();
    @SuppressWarnings("rawtypes")
    private List<Creator> resolved;
    // Once we are able to resolve the resource creator type we will set that resourceCreator's role.
    // private ResourceCreatorRole personRole = ResourceCreatorRole.AUTHOR;
    // private ResourceCreatorRole institutionRole = ResourceCreatorRole.AUTHOR;
    private ResourceCreatorRole role = ResourceCreatorRole.AUTHOR;

    public ResourceCreatorProxy() {
        // TODO: set any defaults here?
    }

    private CreatorType type = CreatorType.PERSON;
    private Long id;

    public ResourceCreatorProxy(Creator<?> creator, ResourceCreatorRole role) {
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
        this.setId(rc.getId());
    }

    // properly set the state of the resourceCreator field by determining if the proxy represents a person or an institution
    private void resolveResourceCreator() {
        if (!initialized) {
            try {
                if (determineActualCreatorType() == CreatorType.PERSON) {
                    resourceCreator.setCreator(person);
                    Institution institution = person.getInstitution();
                    // FIXME: what is the purpose of this check?
                    if ((institution == null) || StringUtils.isBlank(institution.getName())) {
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
            resourceCreator.setId(getId());
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
        if (!initialized) {
            resolveResourceCreator();
        }
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
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH",
            justification = "ignoring null derefernece because findbugs is not paying attention to the null-check above")
    private CreatorType determineActualCreatorType() {
        if ((institution == null) && (person == null)) {
            throw new TdarRecoverableRuntimeException("resourceCreatorProxy.err_determine_creator_insufficient_info");
        }
        boolean hasNoPersistableValues = institution.hasNoPersistableValues();
        if (hasNoPersistableValues && person.hasNoPersistableValues()) {
            return null;
        }
        if (!hasNoPersistableValues && !person.hasNoPersistableValues()) {
            String err = MessageHelper.getMessage("resourceCreatorProxy.err_fmt2_determine_creator_too_much_info",
                    Arrays.asList(getPerson(), getInstitution(), getType()));
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
        if ((person != null) && !person.hasNoPersistableValues()) {
            return true;
        }
        if ((institution != null) && !institution.hasNoPersistableValues()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String pstring = "null (-1)";
        if (person != null) {
            pstring = String.format("%s (%s)", person.getProperName(), person.getId());
        }
        String istring = "null (-1)";
        if (institution != null) {
            istring = String.format("%s (%s)", institution.getName(), institution.getId());
        }
        String rc = "null";
        if (resourceCreator != null) {
            rc = resourceCreator.toString();
        }
        return String.format("[RCP %s  role:%s rc:%s  p:%s  i:%s]",
                this.hashCode(), role, rc, pstring, istring);
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

    public Set<String> getSeenImportFieldNames() {
        return seenImportFieldNames;
    }

    public void setSeenImportFieldNames(Set<String> seenImportFieldNames) {
        this.seenImportFieldNames = seenImportFieldNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public boolean isValidEmailContact() {
        if (getRole() != ResourceCreatorRole.CONTACT) {
            return false;
        }

        if (isValid()) {
            return StringUtils.isNotBlank(getResourceCreator().getCreator().getEmail());
        }

        return false;
    }

    @SuppressWarnings("rawtypes")
    public List<Creator> getResolved() {
        return resolved;
    }

    @SuppressWarnings("rawtypes")
    public void setResolved(List<Creator> resolved) {
        this.resolved = resolved;
    }
}
