package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * <p>
 * A persistable pointer to a resource that has been "bookmarked" by a user. Bookmarked resources serve two purposes:
 * <ul>
 * <li>Bookmarks facilitate a rudimentary, user-specific organizational tool for users.
 * <li>Bookmarked datasets serve as a the "pool" from which a user may choose to include in a dataset integration task.
 * </ul>
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "bookmarked_resource",
        uniqueConstraints = @UniqueConstraint(columnNames = { "person_id", "resource_id" }),
        indexes = {
                @Index(name = "bookmarked_resource_person_id_idx", columnList = "person_id"),
                @Index(name = "bookmarked_resource_resource_id_idx", columnList = "resource_id")
        })
public class BookmarkedResource extends AbstractPersistable {

    private static final long serialVersionUID = -5112227003063546552L;

    @ManyToOne(optional = false)
    private TdarUser person;

    @ManyToOne(optional = false)
    private Resource resource;

    // an alias for this bookmarked resource - if not present, uses the name of the resource.
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
    }

    @XmlAttribute(name = "resourceRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
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

    @Override
    public String toString() {
        if (StringUtils.isEmpty(name)) {
            return String.format("(%d, %s, %s)", getId(), getPerson(), getResource());
        } else {
            return name;
        }
    }

    @Override
    public List<?> getEqualityFields() {
        // ab probably okay as not nullable fields
        return Arrays.asList(person, resource);
    }

}
