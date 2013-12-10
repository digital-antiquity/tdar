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
 * $Id$
 * 
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "material_keyword")
@Indexed(index = "Keyword")
public class MaterialKeyword extends Keyword.Base<MaterialKeyword> implements ControlledKeyword {

    private static final long serialVersionUID = -8439705822874264175L;

    public static final String INHERITANCE_TOGGLE = "inheriting_material_information";

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_keyword_id")
    private Set<MaterialKeyword> synonyms = new HashSet<MaterialKeyword>();

    @Override
    public Set<MaterialKeyword> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<MaterialKeyword> synonyms) {
        this.synonyms = synonyms;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }

}
