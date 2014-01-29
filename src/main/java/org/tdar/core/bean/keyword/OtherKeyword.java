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
 * Represents a "general" or non-specific keyword 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "other_keyword")
@Indexed(index = "Keyword")
public class OtherKeyword extends UncontrolledKeyword.Base<OtherKeyword> {

    private static final long serialVersionUID = -6649756235199570108L;

    public static final String INHERITANCE_TOGGLE = "inheriting_other_information";

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<OtherKeyword> synonyms = new HashSet<OtherKeyword>();

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

}
