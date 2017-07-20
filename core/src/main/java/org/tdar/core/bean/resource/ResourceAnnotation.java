package org.tdar.core.bean.resource;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.HasResource;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.utils.json.JsonProjectLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * A ResourceAnnotation represents a semi-controlled organizational identifier consisting
 * of a semi-controlled key, an arbitrary annotation value, and the resource tagged with
 * the annotation.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "resource_annotation", indexes = {
        @Index(name = "resource_id_keyid", columnList = "resource_id, resourceannotationkey_id, id") })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.ResourceAnnotation")
@Cacheable
public class ResourceAnnotation extends AbstractPersistable implements HasResource<Resource> {

    private static final long serialVersionUID = 8517883471101372051L;

    public ResourceAnnotation() {
    }

    public ResourceAnnotation(ResourceAnnotationKey key, String value) {
        setResourceAnnotationKey(key);
        setValue(value);
    }

    @ManyToOne(optional = false, cascade = { CascadeType.DETACH, CascadeType.MERGE })
    @JsonView(JsonProjectLookupFilter.class)
    private ResourceAnnotationKey resourceAnnotationKey;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @JsonView(JsonLookupFilter.class)
    private String value;

    public String getPairedValue() {
        return getResourceAnnotationKey().getKey() + ":" + getValue();
    }

    @Transient
    @XmlTransient
    public String getFormattedValue() {
        ResourceAnnotationDataType annotationDataType = resourceAnnotationKey.getAnnotationDataType();
        if (annotationDataType.isFormatString()) {
            // do format string stuff.
            return resourceAnnotationKey.format(value);
        }
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ResourceAnnotationKey getResourceAnnotationKey() {
        return resourceAnnotationKey;
    }

    public void setResourceAnnotationKey(ResourceAnnotationKey resourceAnnotationKey) {
        this.resourceAnnotationKey = resourceAnnotationKey;
    }

    @Override
    public String toString() {
        return String.format("[%s :: %s (%s)]", resourceAnnotationKey, value, hashCode());
    }

    @Override
    public boolean isValid() {
        if (resourceAnnotationKey == null) {
            return false;
        }
        if (StringUtils.isEmpty(resourceAnnotationKey.getKey())) {
            return false;
        }
        
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean isValidForController() {
        return true;
    }

}
