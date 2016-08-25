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
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Describes the type of site in the resource
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Entity
@Table(name = "site_type_keyword", indexes = {
        @Index(name = "sitetype_appr", columnList = "approved, id") })
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.SiteTypeKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="site_type_keyword_id"))
 })
@XmlRootElement
public class SiteTypeKeyword extends HierarchicalKeyword<SiteTypeKeyword> implements SuggestedKeyword {

    private static final long serialVersionUID = 4043710177198125088L;
    private boolean approved;

    @XmlAttribute
    @Override
    @JsonView(JsonLookupFilter.class)
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.SITE_TYPE_KEYWORD.getUrlNamespace();
    }

}
