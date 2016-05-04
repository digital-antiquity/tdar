package org.tdar.core.bean;

import org.tdar.utils.MessageHelper;

public enum RelationType implements Localizable {

    DCTERMS_RELATION("http://purl.org/dc/terms/", "dc", "relation"),
    DCTERMS_PART_OF("http://purl.org/dc/terms/", "dc", "part of"),
    DCTERMS_REPLACES("http://purl.org/dc/terms/", "dc", "replaces"),
    DCTERMS_IS_REPLACED_BY("http://purl.org/dc/terms/", "dc", "is replaced by"),
    DCTERMS_IS_VERSION_OF("http://purl.org/dc/terms/", "dc", "is version of");

    private String prefix;
    private String term;
    private String uri;

    RelationType(String uri, String prefix, String term) {
        this.uri = uri;
        this.prefix = prefix;
        this.term = term;
    }

    public String getJsonKey() {
        return String.format("%s:%s", prefix, term);
    }

    public String getTerm() {
        return term;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

}
