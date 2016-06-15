package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

/**
 * Material Type keyword (controlled).
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "material_keyword")
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.MaterialKeyword")
@Cacheable
public class MaterialKeyword extends Keyword.Base<MaterialKeyword> implements ControlledKeyword, SuggestedKeyword {

    private static final long serialVersionUID = -8439705822874264175L;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<MaterialKeyword> synonyms = new HashSet<MaterialKeyword>();

    private boolean approved;

    public MaterialKeyword() {}
    
    public MaterialKeyword(String string) {
        this.setLabel(string);
    }

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

    @Override
    public String getUrlNamespace() {
        return KeywordType.MATERIAL_TYPE.getUrlNamespace();
    }

    @XmlAttribute
    @Override
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

}
