package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

/**
 * $Id$
 * <p>
 * A persistable pointer to a resource, stored by a registered user of tDAR.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "bookmarked_resource")
public class BookmarkedResource extends Persistable.Base {

    private static final long serialVersionUID = -5112227003063546552L;

    @ManyToOne(optional = false)
    @Index(name = "bookmarked_resource_person_id_idx")
    private Person person;

    @ManyToOne(optional = false)
    @Index(name = "bookmarked_resource_resource_id_idx")
    private Resource resource;

    // an alias for this bookmarked resource - if not present, uses the name of the resource.
    @Length(max = 255)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        if (StringUtils.isEmpty(name)) {
            return String.format("(%d, %s, %s)", getId(), getPerson().getEmail(), getResource());
        } else {
            return name;
        }
    }

    public List<?> getEqualityFields() {
        // ab probably okay as not nullable fields
        return Arrays.asList(person, resource);
    }

}
