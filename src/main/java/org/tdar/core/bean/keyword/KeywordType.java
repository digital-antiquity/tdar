package org.tdar.core.bean.keyword;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.MessageHelper;

public enum KeywordType implements Localizable {

    CULTURE_KEYWORD(CultureKeyword.class),
    INVESTIGATION_TYPE(InvestigationType.class),
    MATERIAL_TYPE(MaterialKeyword.class),
    GEOGRAPHIC_KEYWORD(GeographicKeyword.class),
    OTHER_KEYWORD(OtherKeyword.class),
    SITE_NAME_KEYWORD(SiteNameKeyword.class),
    SITE_TYPE_KEYWORD(SiteTypeKeyword.class),
    TEMPORAL_KEYWORD(TemporalKeyword.class);

    private Class<? extends Keyword> keywordClass;

    private KeywordType(Class<? extends Keyword> cls) {
        this.setKeywordClass(cls);
    }

    public Class<? extends Keyword> getKeywordClass() {
        return keywordClass;
    }

    public void setKeywordClass(Class<? extends Keyword> keywordClass) {
        this.keywordClass = keywordClass;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public String getSearchDescriptionKey() {
        return "searchParameters." + getFieldName();
    }

    public String getFieldName() {
        switch (this) {
            case CULTURE_KEYWORD:
                return QueryFieldNames.ACTIVE_CULTURE_KEYWORDS;
            case INVESTIGATION_TYPE:
                return QueryFieldNames.ACTIVE_INVESTIGATION_TYPES;
            case GEOGRAPHIC_KEYWORD:
                return QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS;
            case MATERIAL_TYPE:
                return QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS;
            case OTHER_KEYWORD:
                return QueryFieldNames.ACTIVE_OTHER_KEYWORDS;
            case SITE_NAME_KEYWORD:
                return QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS;
            case SITE_TYPE_KEYWORD:
                return QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS;
            case TEMPORAL_KEYWORD:
                return QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS;
        }
        return null;
    }

    public String getInheritanceToggleField() {
        switch (this) {
            case CULTURE_KEYWORD:
                return InformationResource.CULTURE_INHERITANCE_TOGGLE;
            case INVESTIGATION_TYPE:
                return InformationResource.INVESTIGATION_TYPE_INHERITANCE_TOGGLE;
            case GEOGRAPHIC_KEYWORD:
                return InformationResource.GEOGRAPHIC_INHERITANCE_TOGGLE;
            case MATERIAL_TYPE:
                return InformationResource.MATERIAL_TYPE_INHERITANCE_TOGGLE;
            case OTHER_KEYWORD:
                return InformationResource.OTHER_INHERITANCE_TOGGLE;
            case SITE_NAME_KEYWORD:
            case SITE_TYPE_KEYWORD:
                return InformationResource.SITE_NAME_INHERITANCE_TOGGLE;
            case TEMPORAL_KEYWORD:
                return InformationResource.TEMPORAL_INHERITANCE_TOGGLE;
        }
        return null;
    }

    public String getUrlNamespace() {
        switch (this) {
            case CULTURE_KEYWORD:
                return "browse/culture";
            case INVESTIGATION_TYPE:
                return "browse/investigationType";
            case GEOGRAPHIC_KEYWORD:
                return "browse/geographic";
            case MATERIAL_TYPE:
                return "browse/material";
            case OTHER_KEYWORD:
                return "browse/other";
            case SITE_NAME_KEYWORD:
                return "browse/siteName";
            case SITE_TYPE_KEYWORD:
                return "browse/siteType";
            case TEMPORAL_KEYWORD:
                return "browse/temporal";
        }
        return null;
    }

}
