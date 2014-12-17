package org.tdar.core.dao.resource.integration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class IntegrationOntologySearchResult extends AbstractIntegrationSearchResult {

    private static final long serialVersionUID = -296348695798571158L;
    private List<OntologyProxy> ontologies = new ArrayList<>();

    public List<OntologyProxy> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<OntologyProxy> ontologies) {
        this.ontologies = ontologies;
    }
}
