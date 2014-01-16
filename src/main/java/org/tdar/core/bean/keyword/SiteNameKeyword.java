package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

/**
 * Lists the name of the site in the resource 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "site_name_keyword")
@Indexed(index = "Keyword")
public class SiteNameKeyword extends UncontrolledKeyword.Base<SiteNameKeyword> {

    private static final long serialVersionUID = 60750909588980398L;

    public static final String INHERITANCE_TOGGLE = "inheriting_site_information";

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<SiteNameKeyword> synonyms = new HashSet<SiteNameKeyword>();

    @Override
    public Set<SiteNameKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<SiteNameKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}
