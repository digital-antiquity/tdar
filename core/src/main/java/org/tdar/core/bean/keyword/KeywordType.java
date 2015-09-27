package org.tdar.core.bean.keyword;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.utils.MessageHelper;

public enum KeywordType implements Localizable {

    CULTURE_KEYWORD(CultureKeyword.class, "culture-keyword"),
    INVESTIGATION_TYPE(InvestigationType.class, "investigation-type"),
    MATERIAL_TYPE(MaterialKeyword.class, "material-type"),
    GEOGRAPHIC_KEYWORD(GeographicKeyword.class, "geographic-keyword"),
    OTHER_KEYWORD(OtherKeyword.class, "other-keyword"),
    SITE_NAME_KEYWORD(SiteNameKeyword.class, "site-name"),
    SITE_TYPE_KEYWORD(SiteTypeKeyword.class, "site-type"),
    TEMPORAL_KEYWORD(TemporalKeyword.class, "temporal-keyword");

    private Class<? extends Keyword> keywordClass;
    private String urlSuffix;

    private KeywordType(Class<? extends Keyword> cls, String urlSuffix) {
        this.setKeywordClass(cls);
        this.setUrlSuffix(urlSuffix);
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

//    public String getSearchDescriptionKey() {
//        return "searchParameters." + getFieldName();
//    }

//    public String getFieldName() {
//        switch (this) {
//            case CULTURE_KEYWORD:
//                return QueryFieldNames.ACTIVE_CULTURE_KEYWORDS;
//            case INVESTIGATION_TYPE:
//                return QueryFieldNames.ACTIVE_INVESTIGATION_TYPES;
//            case GEOGRAPHIC_KEYWORD:
//                return QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS;
//            case MATERIAL_TYPE:
//                return QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS;
//            case OTHER_KEYWORD:
//                return QueryFieldNames.ACTIVE_OTHER_KEYWORDS;
//            case SITE_NAME_KEYWORD:
//                return QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS;
//            case SITE_TYPE_KEYWORD:
//                return QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS;
//            case TEMPORAL_KEYWORD:
//                return QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS;
//        }
//        return null;
//    }

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

    public String getJoinTable() {
        switch (this) {
            case CULTURE_KEYWORD:
                return "resource_culture_keyword";
            case GEOGRAPHIC_KEYWORD:
                return "resource_geographic_keyword";
            case INVESTIGATION_TYPE:
                return "resource_investigation_type";
            case MATERIAL_TYPE:
                return "resource_material_keyword";
            case OTHER_KEYWORD:
                return "resource_other_keyword";
            case SITE_NAME_KEYWORD:
                return "resource_site_name_keyword";
            case SITE_TYPE_KEYWORD:
                return "resource_site_type_keyword";
            case TEMPORAL_KEYWORD:
                return "resource_temporal_keyword";
        }
        return null;
    }

    public String getJoinTableKey() {
        switch (this) {
            case CULTURE_KEYWORD:
                return "culture_keyword_id";
            case GEOGRAPHIC_KEYWORD:
                return "geographic_keyword_id";
            case INVESTIGATION_TYPE:
                return "investigation_type_id";
            case MATERIAL_TYPE:
                return "material_keyword_id";
            case OTHER_KEYWORD:
                return "other_keyword_id";
            case SITE_NAME_KEYWORD:
                return "site_name_keyword_id";
            case SITE_TYPE_KEYWORD:
                return "site_type_keyword_id";
            case TEMPORAL_KEYWORD:
                return "temporal_keyword_id";
        }
        return null;
    }

    public String getTableName() {
        switch (this) {
            case CULTURE_KEYWORD:
                return "culture_keyword";
            case GEOGRAPHIC_KEYWORD:
                return "geographic_keyword";
            case INVESTIGATION_TYPE:
                return "investigation_type";
            case MATERIAL_TYPE:
                return "material_keyword";
            case OTHER_KEYWORD:
                return "other_keyword";
            case SITE_NAME_KEYWORD:
                return "site_name_keyword";
            case SITE_TYPE_KEYWORD:
                return "site_type_keyword";
            case TEMPORAL_KEYWORD:
                return "temporal_keyword";
        }
        return null;
    }

    public String getUrlNamespace() {
        return "browse/" + urlSuffix;
    }

    public static KeywordType fromPath(String keywordPath) {
        for (KeywordType type : values()) {
            if (type.urlSuffix.equals(keywordPath)) {
                return type;
            }
        }
        return null;
    }

    public String getUrlSuffix() {
        return urlSuffix;
    }

    private void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

}
