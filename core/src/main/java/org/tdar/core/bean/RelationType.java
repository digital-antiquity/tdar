package org.tdar.core.bean;

import org.tdar.utils.MessageHelper;

public enum RelationType implements Localizable {

    DCTERMS_RELATION("http://purl.org/dc/terms/", "dc", "relation","relation"),
    DCTERMS_PART_OF("http://purl.org/dc/terms/", "dc", "part of","partOf"),
    DCTERMS_REPLACES("http://purl.org/dc/terms/", "dc", "replaces", "replaces"),
    DCTERMS_IS_REPLACED_BY("http://purl.org/dc/terms/", "dc", "is replaced by", "isReplacedBy"),
    DCTERMS_IS_VERSION_OF("http://purl.org/dc/terms/", "dc", "is version of", "isVersionOf"), 
    HAS_VERSION("http://purl.org/dc/terms/", "dc", "has version", "hasVersion"),
    HAS_PART("http://purl.org/dc/terms/", "dc", "has part", "hasPart");

    private String prefix;
    private String term;
    private String shortTerm;
    private String uri;

    RelationType(String uri, String prefix, String term, String shortTerm) {
        this.uri = uri;
        this.prefix = prefix;
        this.term = term;
        this.shortTerm  = shortTerm;
    }

    public String getJsonKey() {
        return String.format("%s:%s", prefix, shortTerm);
    }

    public String getTerm() {
        return term;
    }

    
    public String getShortTerm() {
        return shortTerm;
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
