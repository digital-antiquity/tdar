package org.tdar.core.service.resource.ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.OntologyNode;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class OntologyNodeWrapper implements Serializable {

    private static final long serialVersionUID = 8411393518053120151L;
    String iri;
    String displayName;
    Long id;
    private String slug;
    List<OntologyNodeWrapper> children = new ArrayList<>();

    public OntologyNodeWrapper() {
    }

    public OntologyNodeWrapper(OntologyNode c) {
        this.iri = c.getIri();
        this.displayName = c.getDisplayName();
        this.id = c.getId();
        this.setSlug(c.getSlug());
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OntologyNodeWrapper> getChildren() {
        return children;
    }

    public void setChildren(List<OntologyNodeWrapper> children) {
        this.children = children;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

}
