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
 * Represents the type of Investigation or research described by the resource.
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "investigation_type")
@Indexed(index = "Keyword")
public class InvestigationType extends Keyword.Base<InvestigationType> implements ControlledKeyword {

    private static final long serialVersionUID = 2557655317256194003L;

    public static final String INHERITANCE_TOGGLE = "inheriting_investigation_information";

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<InvestigationType> synonyms = new HashSet<InvestigationType>();

    @Override
    public Set<InvestigationType> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<InvestigationType> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}
