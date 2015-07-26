package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
public class OntologyProxy implements Serializable {

    private static final long serialVersionUID = -7517599753972933585L;

    private TdarUser submitter;
    private Ontology ontology;
    private List<OntologyNode> nodes;

    public OntologyProxy(Ontology ontology) {
        this.ontology = ontology;
        this.submitter = ontology.getSubmitter();
        this.nodes = ontology.getSortedOntologyNodesByImportOrder();
    }

    @JsonView(JsonIntegrationFilter.class)
    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    @JsonView(JsonIntegrationFilter.class)
    public List<OntologyNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<OntologyNode> nodes) {
        this.nodes = nodes;
    }

}
