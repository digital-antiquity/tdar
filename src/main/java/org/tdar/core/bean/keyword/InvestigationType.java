package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
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
@Table(name="investigation_type")
@XStreamAlias("investigationType")
@Indexed(index = "Keyword")
public class InvestigationType extends ControlledKeyword.Base<InvestigationType> {

	private static final long serialVersionUID = 2557655317256194003L;

    @ElementCollection()
    @JoinTable(name = "investigation_type_synonym")
    private Set<String> synonyms;

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
