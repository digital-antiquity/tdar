package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.Indexed;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "culture_keyword")
@XStreamAlias("cultureKeyword")
@Indexed(index = "Keyword")
public class CultureKeyword extends HierarchicalKeyword<CultureKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = -7196238088495993840L;
    private boolean approved;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = true)
    private CultureKeyword parent;

    @ElementCollection()
    @JoinTable(name = "culture_keyword_synonym")
    private Set<String> synonyms;

    
    @XmlAttribute
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(CultureKeyword parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    @XmlElement(name="parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CultureKeyword getParent() {
        return parent;
    }

    public Set<String> getSynonyms() {
        if(synonyms == null) {
            synonyms = new HashSet<String>();
        }
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }
}