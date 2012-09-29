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
@Table(name = "site_type_keyword")
@XStreamAlias("siteTypeKeyword")
@Indexed(index = "Keyword")
public class SiteTypeKeyword extends HierarchicalKeyword<SiteTypeKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = 4043710177198125088L;
    private boolean approved;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, optional = true)
    private SiteTypeKeyword parent;

    @ElementCollection()
    @JoinTable(name = "site_type_keyword_synonym")
    private Set<String> synonyms;

    @XmlAttribute
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public SiteTypeKeyword getParent() {
        return parent;
    }

    public void setParent(SiteTypeKeyword parent) {
        this.parent = parent;
    }

    public Set<String> getSynonyms() {
        if (synonyms == null) {
            synonyms = new HashSet<String>();
        }
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }
}
