package org.tdar.core.bean.citation;

import javax.persistence.CascadeType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;

/**
 * $Id$
 * 
 * Mapped superclass to reduce redundancy of RelatedComparativeCollection and SourceCollection metadata
 * (which are all just special cases of String text associated with a Resource).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@MappedSuperclass
public abstract class Citation extends Persistable.Base {

    private static final long serialVersionUID = 4174558394278154078L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH }, optional = false)
    private Resource resource;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String text;

    @Field
    public String getText() {
        return text;
    }

    public String toString() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
