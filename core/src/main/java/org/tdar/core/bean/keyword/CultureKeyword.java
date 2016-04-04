package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * Represents a Culture described by a resource.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "culture_keyword", indexes = {
        @Index(name = "cltkwd_appr", columnList = "approved, id")
})
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.CultureKeyword")
@Cacheable
public class CultureKeyword extends HierarchicalKeyword<CultureKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = -7196238088495993840L;

    private boolean approved;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<CultureKeyword> synonyms = new HashSet<CultureKeyword>();

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private CultureKeyword parent;

    public CultureKeyword() {
    }

    public CultureKeyword(String string) {
        setLabel(string);
    }

    @XmlAttribute
    @Override
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
    @Override
    public void setParent(CultureKeyword parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Override
    public CultureKeyword getParent() {
        return parent;
    }

    @Override
    public Set<CultureKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<CultureKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.CULTURE_KEYWORD.getUrlNamespace();
    }
}