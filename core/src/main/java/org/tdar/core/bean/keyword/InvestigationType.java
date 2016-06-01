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
 * Represents the type of Investigation or research described by the resource.
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "investigation_type")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.InvestigationType")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="investigation_type_id"))
 })
@XmlRootElement
public class InvestigationType extends AbstractKeyword<InvestigationType> implements ControlledKeyword {

    private static final long serialVersionUID = 2557655317256194003L;

    @Override
    public String getUrlNamespace() {
        return KeywordType.INVESTIGATION_TYPE.getUrlNamespace();
    }

}
