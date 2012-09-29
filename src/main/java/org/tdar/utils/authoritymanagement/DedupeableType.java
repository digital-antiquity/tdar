package org.tdar.utils.authoritymanagement;

import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;


//domain of entity types that we allow users to 'de-dupe'
public enum DedupeableType {
    INSTITUTION(Institution.class, "Institution"),
    PERSON(Person.class, "Person"),
    KEYWORD_CULTURE_KEYWORD(CultureKeyword.class, "Culture Keyword"),
    KEYWORD_GEOGRAPHIC_KEYWORD(GeographicKeyword.class, "Geographic Keyword"),
    KEYWORD_INVESTIGATION_TYPE(InvestigationType.class,  "Investigation Type"),
    KEYWORD_MATERIAL_KEYWORD(MaterialKeyword.class, "Material Keyword"),
    KEYWORD_OTHER_KEYWORD(OtherKeyword.class, "Other Keyword"),
    KEYWORD_SITE_NAME_KEYWORD(SiteNameKeyword.class, "Site Name Keyword"),
    KEYWORD_SITE_TYPE_KEYWORD(SiteTypeKeyword.class, "Site Type Keyword");
    
    private Class<?> type;
    private String label;
    
    public Class<?> getType() {
        return type;
    }
    
    public String getLabel() {
        return label;
    }
    
    private DedupeableType(Class<?> type, String label) {
        this.type = type;
        this.label = label;
    }
    
}
