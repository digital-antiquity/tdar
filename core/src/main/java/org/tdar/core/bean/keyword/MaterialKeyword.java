package org.tdar.core.bean.keyword;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
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
 * Material Type keyword (controlled).
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@Entity
@Table(name = "material_keyword")
@Check(constraints = "label <> ''")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.keyword.MaterialKeyword")
@Cacheable
@AssociationOverrides({
    @AssociationOverride(name = "assertions",
       joinColumns = @JoinColumn(name="material_keyword_id"))
 })
@XmlRootElement
public class MaterialKeyword extends AbstractKeyword<MaterialKeyword> implements ControlledKeyword, SuggestedKeyword {

    private static final long serialVersionUID = -8439705822874264175L;


    private boolean approved;

    public MaterialKeyword() {}
    
    public MaterialKeyword(String string) {
        this.setLabel(string);
    }

    @Override
    public String getUrlNamespace() {
        return KeywordType.MATERIAL_TYPE.getUrlNamespace();
    }

    @XmlAttribute
    @Override
    @JsonView(JsonLookupFilter.class)
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

}
