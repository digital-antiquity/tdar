package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    @OneToMany(orphanRemoval = true,cascade=CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<CultureKeyword> synonyms = new HashSet<CultureKeyword>();


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = true)
    private CultureKeyword parent;

    
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

    public Set<CultureKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<CultureKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}