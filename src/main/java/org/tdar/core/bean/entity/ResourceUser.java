/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.entity;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

/**
 * @author Adam Brin
 * 
 */
@MappedSuperclass
public abstract class ResourceUser extends Persistable.Base implements Comparable<Person>, HasResource<Resource> {

    private static final long serialVersionUID = -7201906471720497045L;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.PERSIST }, optional = false)
    private Person person;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.PERSIST }, optional = false)
    private Resource resource;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String toString() {
        return "user: " + person.getProperName() + " for resource: " + resource.getTitle();
    }

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Compares by last name, first name, and then finally email.
     */
    public int compareTo(Person otherPerson) {
        return getPerson().compareTo(otherPerson);
    }

    public boolean isValid() {
        return true;
    }
}
