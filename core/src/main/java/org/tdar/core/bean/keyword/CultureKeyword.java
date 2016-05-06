package org.tdar.core.bean.keyword;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

/**
 * Represents a Culture described by a resource.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "culture_keyword", indexes = {
        @Index(name = "cltkwd_appr", columnList = "approved, id")
})
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.CultureKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="culture_keyword_id"))
 })
@XmlRootElement
public class CultureKeyword extends HierarchicalKeyword<CultureKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = -7196238088495993840L;

    private boolean approved;

    public CultureKeyword() {
    }

    public CultureKeyword(String string) {
        setLabel(string);
    }

    @XmlAttribute
    @Override
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.CULTURE_KEYWORD.getUrlNamespace();
    }

}