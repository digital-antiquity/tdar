package org.tdar.core.bean.keyword;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.PatternTokenAnalyzer;

import com.sun.xml.txw2.annotation.XmlElement;

/**
 * $Id$
 * 
 * Base class for hierarchical keywords (tree based)
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>, Matt Cordial
 * @version $Rev$
 */
@MappedSuperclass
@XmlElement
public abstract class HierarchicalKeyword<T extends HierarchicalKeyword<T>> extends Keyword.Base<HierarchicalKeyword<T>> {

    private static final long serialVersionUID = -9098940417785842655L;

    private boolean selectable;

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

    @Fields({ @Field(name = "label", analyzer = @Analyzer(impl = PatternTokenAnalyzer.class)),
            @Field(name = "labelKeyword", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
    public String getParentLabels() {
        if (getParent() == null)
            return "";
        // labelKeyword : "Woodland : Upper Woodland"
        // label : "Woodland"
        // label : "Upper Woodland"
        StringBuilder parentString = new StringBuilder(StringUtils.trim(getParent().getParentLabels()));
        if (parentString.length() > 1) {
            parentString.append(" : ");
        }
        parentString.append(StringUtils.trim(getParent().getLabel()));
        return parentString.toString();
    }

    @Transient
    public List<String> getParentLabelList() {
        List<String> list = new ArrayList<String>();
        if (getParent() == null)
            return list;
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
        //get the level without visiting the ancestors
        if(StringUtils.isBlank(index)) return 0;
        return 1 + StringUtils.countMatches(index, ".");
    }

}
