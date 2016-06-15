package org.tdar.core.bean.citation;

import java.util.Arrays;

import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

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
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
@XmlType(name = "citation")
@XmlTransient
public abstract class Citation extends Persistable.Base implements HasResource<Resource> {

    private static final long serialVersionUID = 4174558394278154078L;

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "text" };

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Length(max = FieldLength.FIELD_LENGTH_1024)
    @JsonView(JsonLookupFilter.class)
    private String text;

    @Override
    public java.util.List<?> getEqualityFields() {
        return Arrays.asList(text);
    };

    //@Field
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean isValid() {
        if (!StringUtils.isEmpty(text)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidForController() {
        return true;
    }

}
