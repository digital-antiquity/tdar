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

//domain of entity types that we allow users to 'de-dupe'
// FIXME: consider using an INTERFACE instead of a ENUM and then autowiring a list of the thing that support that interface
@SuppressWarnings("rawtypes")
public enum DedupeableType implements HasLabel {
    INSTITUTION(Institution.class, MessageHelper.getMessage("dedupableType.institution")),
    PERSON(Person.class, MessageHelper.getMessage("dedupableType.person")),
    KEYWORD_CULTURE_KEYWORD(CultureKeyword.class, MessageHelper.getMessage("dedupableType.culture")),
    KEYWORD_GEOGRAPHIC_KEYWORD(GeographicKeyword.class, MessageHelper.getMessage("dedupableType.geographic")),
    KEYWORD_INVESTIGATION_TYPE(InvestigationType.class, MessageHelper.getMessage("dedupableType.investigation_type")),
    KEYWORD_MATERIAL_KEYWORD(MaterialKeyword.class, MessageHelper.getMessage("dedupableType.material")),
    KEYWORD_OTHER_KEYWORD(OtherKeyword.class, MessageHelper.getMessage("dedupableType.other")),
    KEYWORD_SITE_NAME_KEYWORD(SiteNameKeyword.class, MessageHelper.getMessage("dedupableType.site_name")),
    KEYWORD_SITE_TYPE_KEYWORD(SiteTypeKeyword.class, MessageHelper.getMessage("dedupableType.site_type"));

    private Class<? extends Dedupable> type;
    private String label;

    public Class<? extends Dedupable> getType() {
        return type;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private DedupeableType(Class<? extends Dedupable> type, String label) {
        this.type = type;
        this.label = label;
    }

}
