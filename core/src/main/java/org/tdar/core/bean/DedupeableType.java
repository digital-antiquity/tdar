package org.tdar.core.bean;

import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.utils.MessageHelper;

/**
 * Controls the types of objects that can be deduped.
 * 
 * @author abrin
 * 
 */
// domain of entity types that we allow users to 'de-dupe'
// FIXME: consider using an INTERFACE instead of a ENUM and then autowiring a list of the thing that support that interface
@SuppressWarnings("rawtypes")
public enum DedupeableType implements HasLabel, Localizable {
    INSTITUTION(Institution.class, "Institution"),
    PERSON(Person.class, "Person"),
    KEYWORD_CULTURE_KEYWORD(CultureKeyword.class,
            "Culture Keyword"),
    KEYWORD_GEOGRAPHIC_KEYWORD(GeographicKeyword.class, "Geographic Keyword"),
    KEYWORD_INVESTIGATION_TYPE(InvestigationType.class,
            "Investigation Type"),
    KEYWORD_MATERIAL_KEYWORD(MaterialKeyword.class, "Material Keyword"),
    KEYWORD_OTHER_KEYWORD(OtherKeyword.class,
            "Other Keyword"),
    KEYWORD_SITE_NAME_KEYWORD(SiteNameKeyword.class,
            "Site Name Keyword"),
    KEYWORD_SITE_TYPE_KEYWORD(SiteTypeKeyword.class, "Site Type Keyword");

    private Class<? extends Dedupable> type;
    private String label;

    public Class<? extends Dedupable> getType() {
        return type;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    private DedupeableType(Class<? extends Dedupable> type, String label) {
        this.type = type;
        this.label = label;
    }

}
