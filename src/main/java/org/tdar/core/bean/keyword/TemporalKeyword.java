package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Check;
import org.hibernate.search.annotations.Indexed;

/**
 * Temporal term coverage
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "temporal_keyword")
@Indexed(index = "Keyword")
@Check(constraints="label <> ''")
public class TemporalKeyword extends UncontrolledKeyword.Base<TemporalKeyword> {

    private static final long serialVersionUID = -626136232824053935L;

    public static final String INHERITANCE_TOGGLE = "inheriting_temporal_information";

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<TemporalKeyword> synonyms = new HashSet<TemporalKeyword>();

    @Override
    public Set<TemporalKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<TemporalKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}
