package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;
import org.tdar.utils.json.JsonNodeParticipationFilter;

@JsonAutoDetect
public class IntegrationColumnProxy implements Serializable {

    private static final long serialVersionUID = -6564768298774032395L;

    private Ontology sharedOntology;
    private List<OntologyNode> flattenedNodes = new ArrayList<>();
    
    public Ontology getSharedOntology() {
        return sharedOntology;
    }

    public void setSharedOntology(Ontology shared) {
        this.sharedOntology = shared;
    }

    @JsonView(JsonNodeParticipationFilter.class)
    public List<OntologyNode> getFlattenedNodes() {
        return flattenedNodes;
    }

    public void setFlattenedNodes(List<OntologyNode> flattenedNodes) {
        this.flattenedNodes = flattenedNodes;
    }

}
