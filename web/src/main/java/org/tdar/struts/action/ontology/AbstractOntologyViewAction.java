package org.tdar.struts.action.ontology;

import java.util.List;
import java.util.Objects;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.struts.action.resource.AbstractSupportingResourceViewAction;

public abstract class AbstractOntologyViewAction extends AbstractSupportingResourceViewAction<Ontology> {

    private static final long serialVersionUID = -7901012726097964225L;
    private OntologyNode node;
    private String iri;
    private String redirectIri;
    private List<Dataset> datasetsWithMappingsToNode;

    protected OntologyNode getNodeByIri() {
        String iri_ = getIri();
        getLogger().trace("id: {} iri: {} slug: {}", getId(), iri_, getSlug());
        OntologyNode node_ = getOntology().getNodeByIri(OntologyNode.normalizeIri(iri_));
        if (node_ == null) {
            node_ = fallbackCheckForIri(iri_);
        }
        getLogger().trace("iri: {} node: {}", getIri(), node_);
        return node_;
    }

    protected OntologyNode getNodeBySlug() {
        String iri_ = getIri();
        getLogger().trace("id: {} iri: {} slug: {}", getId(), iri_, getSlug());
        OntologyNode node_ = getOntology().getNodeBySlug(iri_);
        return node_;
    }

    public OntologyNode getNode() {
        return node;
    }

    public void setNode(OntologyNode node) {
        this.node = node;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public Ontology getOntology() {
        return (Ontology) getResource();
    }

    /**
     * Checks for IRI, also removes parenthesis which may be removed by struts
     * 
     * @param normalizeIri
     * @param node_
     * @return
     */
    protected OntologyNode fallbackCheckForIri(String normalizeIri) {
        getLogger().trace("normalizedIri:{}", normalizeIri);
        for (OntologyNode node_ : getOntology().getOntologyNodes()) {
            String iri_ = node_.getNormalizedIri().replaceAll("[\\(\\)\\\\.']", "");
            getLogger().trace("|{}|<--{}-->|{}|", iri_, Objects.equals(iri_, normalizeIri), normalizeIri);
            if (Objects.equals(normalizeIri, iri_)) {
                return node_;
            }
        }
        return null;
    }

    public List<Dataset> getDatasetsWithMappingsToNode() {
        return datasetsWithMappingsToNode;
    }

    public void setDatasetsWithMappingsToNode(List<Dataset> datasetsWithMappingsToNode) {
        this.datasetsWithMappingsToNode = datasetsWithMappingsToNode;
    }

    public String getRedirectIri() {
        return redirectIri;
    }

    public void setRedirectIri(String redirectIri) {
        this.redirectIri = redirectIri;
    }

}
