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
 * Represents a "general" or non-specific keyword
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "other_keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.OtherKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="other_keyword_id"))
 })
@XmlRootElement
public class OtherKeyword extends AbstractKeyword<OtherKeyword> implements UncontrolledKeyword{

    private static final long serialVersionUID = -6649756235199570108L;


    public OtherKeyword() {
    }

    public OtherKeyword(String name) {
        setLabel(name);
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.OTHER_KEYWORD.getUrlNamespace();
    }

}
