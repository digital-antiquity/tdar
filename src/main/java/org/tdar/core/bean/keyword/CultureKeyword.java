package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "culture_keyword")
@org.hibernate.annotations.Table( appliesTo="culture_keyword", indexes = {
        @Index(name="cltkwd_appr", columnNames={"approved", "id"})
})
@Indexed(index = "Keyword")
public class CultureKeyword extends HierarchicalKeyword<CultureKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = -7196238088495993840L;

    public static final String INHERITANCE_TOGGLE = "inheriting_cultural_information";
    private boolean approved;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<CultureKeyword> synonyms = new HashSet<CultureKeyword>();

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
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
    @XmlElement(name = "parentRef")
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