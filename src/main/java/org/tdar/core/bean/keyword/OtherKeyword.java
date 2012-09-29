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
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name="other_keyword")
@XStreamAlias("otherKeyword")
@Indexed(index = "Keyword")
public class OtherKeyword extends UncontrolledKeyword.Base<OtherKeyword> {

    private static final long serialVersionUID = -6649756235199570108L;

    @ElementCollection()
    @JoinTable(name = "other_keyword_synonym")
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
