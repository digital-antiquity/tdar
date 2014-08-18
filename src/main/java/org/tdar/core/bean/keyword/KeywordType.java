package org.tdar.core.bean.keyword;

public enum KeywordType {

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
    
}
