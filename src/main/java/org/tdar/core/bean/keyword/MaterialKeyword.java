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
@Table(name="material_keyword")
@XStreamAlias("materialKeyword")
@Indexed(index = "Keyword")
public class MaterialKeyword extends ControlledKeyword.Base<MaterialKeyword> {
	
	private static final long serialVersionUID = -8439705822874264175L;

    @ElementCollection()
    @JoinTable(name = "material_keyword_synonym")
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
