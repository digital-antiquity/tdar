package org.tdar.core.bean.keyword;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.tdar.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.index.analyzer.PatternTokenAnalyzer;

import com.sun.xml.txw2.annotation.XmlElement;

/**
 * $Id$
 * 
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

    public boolean isSelectable() {
        return selectable;
    }

//    public int compareTo(HierarchicalKeyword<T> o) {
//        return getIndex().compareTo(o.getIndex());
//    }


    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public abstract void setParent(T parent);

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

}
