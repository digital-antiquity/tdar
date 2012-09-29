package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * $Id$
 * 
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "investigation_type")
@XStreamAlias("investigationType")
@Indexed(index = "Keyword")
public class InvestigationType extends Keyword.Base<InvestigationType> implements ControlledKeyword {

    private static final long serialVersionUID = 2557655317256194003L;

    @OneToMany(orphanRemoval = true,cascade=CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<InvestigationType> synonyms = new HashSet<InvestigationType>();

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
