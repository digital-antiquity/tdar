package org.tdar.core.bean.keyword;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;

//import com.sun.xml.txw2.annotation.XmlElement;

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
public abstract class HierarchicalKeyword<T extends HierarchicalKeyword<T>> extends Keyword.Base<HierarchicalKeyword<T>> {

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

    public abstract void setParent(T parent);

    @XmlTransient
    public abstract T getParent();

    //@Fields({ //@Field(name = "label"),
            //@Field(name = "labelKeyword", analyzer = //@Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
    @Transient
    @ElementCollection
    //@IndexedEmbedded
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
