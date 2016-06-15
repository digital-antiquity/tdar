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
 * Lists the name of the site in the resource
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "site_name_keyword")
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.SiteNameKeyword")
@Cacheable
public class SiteNameKeyword extends UncontrolledKeyword.Base<SiteNameKeyword> {

    private static final long serialVersionUID = 60750909588980398L;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<SiteNameKeyword> synonyms = new HashSet<SiteNameKeyword>();

    @Override
    public Set<SiteNameKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<SiteNameKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    //@Field(name = QueryFieldNames.SITE_CODE, analyzer = //@Analyzer(impl = SiteCodeTokenizingAnalyzer.class))
    public String getSiteCode() {
        return getLabel();
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.SITE_NAME_KEYWORD.getUrlNamespace();
    }

}
