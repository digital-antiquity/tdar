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
 * Temporal term coverage
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "temporal_keyword")
@XStreamAlias("temporalKeyword")
@Indexed(index = "Keyword")
public class TemporalKeyword extends UncontrolledKeyword.Base<TemporalKeyword> {

    private static final long serialVersionUID = -626136232824053935L;

    @ElementCollection()
    @JoinTable(name = "temporal_keyword_synonym")
    private Set<String> synonyms;

    public Set<String> getSynonyms() {
        if(synonyms == null) {
            synonyms = new HashSet<String>();
        }
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

}
