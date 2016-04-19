package org.tdar.core.bean;

public enum RelationType {

    DCTERMS_RELATION("http://purl.org/dc/terms/", "dc");

    private String prefix;
    private Object uri;

    RelationType(String uri, String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Object getUri() {
        return uri;
    }

    public void setUri(Object uri) {
        this.uri = uri;
    }

}
