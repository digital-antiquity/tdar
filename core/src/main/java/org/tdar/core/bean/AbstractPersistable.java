package org.tdar.core.bean;

import java.util.Collections;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonIdNameFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Convenience base class for Persistable entities providing JPA annotated
 * fields for ID and a property-aware equals()/hashCode() implementations.
 */
@MappedSuperclass
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "base")
@XmlTransient
public abstract class AbstractPersistable implements Persistable {

    private static final long serialVersionUID = -458438238558572364L;

    protected final static String[] DEFAULT_JSON_PROPERTIES = { "id" };

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Uses GenerationType.IDENTITY, which translates to the (big)serial column type for
     * hibernate+postgres, i.e., one sequence table per entity type
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({ JsonIdNameFilter.class })
    private Long id = -1L;

    @Override
    @XmlAttribute(name = "id")
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @XmlTransient
    public boolean isTransient() {
        return PersistableUtils.isTransient(this);
    }

    /**
     * Returns true if:
     * <ol>
     * <li>object identity holds
     * <li>both object types are consistent and the ids for both this persistable object and the incoming persistable object are .equals()
     * <li>OR both object types are consistent and all of the class-specific equality fields specified in getEqualityFields() are .equals()
     * </ol>
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if ((object instanceof Persistable) && getClass().isInstance(object)) {
            return PersistableUtils.isEqual(this, getClass().cast(object));
        }
        return false;
    }

    /**
     * Returns a sensible hashCode() for persisted objects. For transient/unsaved objects, uses
     * the default Object.hashCode().
     */
    @Override
    public int hashCode() {
        Logger logger = LoggerFactory.getLogger(getClass());
        int hashCode = PersistableUtils.toHashCode(this);
        if (logger.isTraceEnabled()) {
            Object[] obj = { hashCode, getClass().getSimpleName(), getId() };
            logger.trace("setting hashCode to {} ({}) {}", obj);
        }
        return hashCode;
    }

    /**
     * By default, base the hashcode off of object's inherent hashcode.
     */
    @Override
    @XmlTransient
    public List<?> getEqualityFields() {
        return Collections.emptyList();
    }
}
