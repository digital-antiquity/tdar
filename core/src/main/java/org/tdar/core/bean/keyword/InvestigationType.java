package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

/**
 * Represents the type of Investigation or research described by the resource.
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "investigation_type")
//@Indexed(index = "Keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.InvestigationType")
@Cacheable
public class InvestigationType extends Keyword.Base<InvestigationType> implements ControlledKeyword {

    private static final long serialVersionUID = 2557655317256194003L;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<InvestigationType> synonyms = new HashSet<InvestigationType>();

    public InvestigationType() {}
    
    public InvestigationType(String string) {
        setLabel(string);
    }

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

    @Override
    public String getUrlNamespace() {
        return KeywordType.INVESTIGATION_TYPE.getUrlNamespace();
    }
}
