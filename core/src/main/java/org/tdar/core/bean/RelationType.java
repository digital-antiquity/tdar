package org.tdar.core.bean;

public enum RelationType {

    DCTERMS_RELATION("http://purl.org/dc/terms/", "dc","relation");

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

}
