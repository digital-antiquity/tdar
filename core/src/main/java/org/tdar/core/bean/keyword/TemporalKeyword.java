package org.tdar.core.bean.keyword;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

/**
 * Temporal term coverage
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "temporal_keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.TemporalKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="temporal_keyword_id"))
 })
@XmlRootElement
public class TemporalKeyword extends AbstractKeyword<TemporalKeyword> implements UncontrolledKeyword {

    private static final long serialVersionUID = -626136232824053935L;

    public TemporalKeyword(String string) {
        this.setLabel(string);
    }
    
    public TemporalKeyword() {
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.TEMPORAL_KEYWORD.getUrlNamespace();
    }

}
