package org.tdar.core.bean.keyword;

import org.tdar.core.bean.Localizable;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.MessageHelper;

public enum KeywordType implements Localizable {

    CULTURE_KEYWORD(CultureKeyword.class),
    INVESTIGATION_TYPE(InvestigationType.class),
    MATERIAL_KEYWORD(MaterialKeyword.class),
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

    public String getFieldName() {
        switch (this) {
            case CULTURE_KEYWORD:
                return QueryFieldNames.ACTIVE_CULTURE_KEYWORDS;
            case INVESTIGATION_TYPE:
                return QueryFieldNames.ACTIVE_INVESTIGATION_TYPES;
            case MATERIAL_KEYWORD:
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
    
}
