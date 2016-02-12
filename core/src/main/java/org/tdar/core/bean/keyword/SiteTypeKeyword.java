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
 * Describes the type of site in the resource
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "site_type_keyword", indexes = {
        @Index(name = "sitetype_appr", columnList = "approved, id") })
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.SiteTypeKeyword")
@Cacheable
public class SiteTypeKeyword extends HierarchicalKeyword<SiteTypeKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = 4043710177198125088L;
    private boolean approved;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private SiteTypeKeyword parent;

    @XmlAttribute
    @Override
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Override
    public SiteTypeKeyword getParent() {
        return parent;
    }

    @Override
    public void setParent(SiteTypeKeyword parent) {
        this.parent = parent;
    }

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<SiteTypeKeyword> synonyms = new HashSet<SiteTypeKeyword>();

    @Override
    public Set<SiteTypeKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<SiteTypeKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.SITE_TYPE_KEYWORD.getUrlNamespace();
    }

}
