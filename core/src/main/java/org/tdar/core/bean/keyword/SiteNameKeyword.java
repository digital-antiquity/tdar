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
 * Lists the name of the site in the resource
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "site_name_keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.SiteNameKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="site_name_keyword_id"))
 })
@XmlRootElement
public class SiteNameKeyword extends AbstractKeyword<SiteNameKeyword> implements UncontrolledKeyword {

    private static final long serialVersionUID = 60750909588980398L;


    public String getSiteCode() {
        return getLabel();
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.SITE_NAME_KEYWORD.getUrlNamespace();
    }

}
