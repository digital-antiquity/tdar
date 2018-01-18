package org.tdar.core.bean.keyword;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
//import com.sun.xml.txw2.annotation.XmlElement;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * 
 * Base class for hierarchical keywords (tree based)
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>, Matt Cordial
 * @version $Rev$
 */
@MappedSuperclass
// @XmlElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "hierKwdbase")
@XmlTransient
public abstract class HierarchicalKeyword<T extends HierarchicalKeyword<T>> extends AbstractKeyword<T> {

    private static final long serialVersionUID = -9098940417785842655L;

    private boolean selectable;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String index;

    @XmlAttribute
    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @XmlAttribute
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private T parent;

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(T parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public T getParent() {
        return parent;
    }

    @Transient
    @ElementCollection
    public List<String> getParentLabelList() {
        List<String> list = new ArrayList<String>();
        if (getParent() == null) {
            return list;
        }
        list.add(getParent().getLabel());
        list.addAll(getParent().getParentLabelList());
        return list;
    }

    @Override
    public boolean isDedupable() {
        return getParent() == null;
    }

    @Transient
    public int getLevel() {
        // get the level without visiting the ancestors
        if (StringUtils.isBlank(index)) {
            return 0;
        }
        return 1 + StringUtils.countMatches(index, ".");
    }

}
