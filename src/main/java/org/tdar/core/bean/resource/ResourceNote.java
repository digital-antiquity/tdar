package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Field;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * $Id$
 * <p>
 * 
 * @author Adam Brin
 * @version $Revision$
 */

@Entity
@XStreamAlias("resourceNote")
@Table(name = "resource_note")
public class ResourceNote extends Persistable.Base implements HasResource<Resource> {

    private static final long serialVersionUID = 8517883471101372051L;

    @ManyToOne(optional = false)
    private Resource resource;

    @Column(length = 2048)
    @Field
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type")
    @Field
    @XStreamAsAttribute
    private ResourceNoteType type;

    public ResourceNote() {
    }

    public ResourceNote(ResourceNoteType type, String note) {
        this.type = type;
        this.note = note;
    }

    @XmlIDREF
    @XmlAttribute(name = "resourceId")
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getNote() {
        if (note == null)
            return "";
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @XmlAttribute
    public ResourceNoteType getType() {
        if (type == null)
            return ResourceNoteType.GENERAL;
        return type;
    }

    public void setType(ResourceNoteType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getType().getLabel() + ":" + getNote();
    }

    public boolean isValid() {
        if (type != null && !StringUtils.isEmpty(note)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidForController() {
        return true;
    }
}
