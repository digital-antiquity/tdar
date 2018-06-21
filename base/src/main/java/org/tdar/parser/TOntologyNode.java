package org.tdar.parser;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class TOntologyNode implements Serializable {

    private static final long serialVersionUID = 674136436816167708L;
    private Long importOrder;
    private String description;
    private String displayName;
    private String index;
    private String iri;
    private Integer intervalStart;
    private Integer intervalEnd;
    private TOntologyNode parentNode;
    private String uri;
    private Set<String> synonyms = new HashSet<>();
    private Set<TOntologyNode> synonymNodes = new HashSet<>();

    public TOntologyNode(String iri, String label) {
        this.iri = iri;
        this.displayName = label;
    }

    public TOntologyNode() {
    }

    public Long getImportOrder() {
        return importOrder;
    }

    public void setImportOrder(Long importOrder) {
        this.importOrder = importOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public Integer getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(Integer intervalStart) {
        this.intervalStart = intervalStart;
    }

    public Integer getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(Integer intervalEnd) {
        this.intervalEnd = intervalEnd;
    }

    public TOntologyNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(TOntologyNode parentNode) {
        this.parentNode = parentNode;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setSynonymNodes(Set<TOntologyNode> synonymNodes) {
        this.synonymNodes = synonymNodes;
    }

    public Set<TOntologyNode> getSynonymNodes() {
        return synonymNodes;
    }

}
