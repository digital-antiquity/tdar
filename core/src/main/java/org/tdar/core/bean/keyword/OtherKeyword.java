package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

/**
 * Represents a "general" or non-specific keyword
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "other_keyword")
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.OtherKeyword")
@Cacheable
public class OtherKeyword extends UncontrolledKeyword.Base<OtherKeyword> {

    private static final long serialVersionUID = -6649756235199570108L;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<OtherKeyword> synonyms = new HashSet<OtherKeyword>();

    public OtherKeyword() {
    }

    public OtherKeyword(String name) {
        setLabel(name);
    }

    @Override
    public Set<OtherKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<OtherKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.OTHER_KEYWORD.getUrlNamespace();
    }

}
