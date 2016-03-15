package org.tdar.core.dao.integration;

import java.util.ArrayList;
import java.util.List;

import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
public class IntegrationOntologySearchResult extends AbstractIntegrationSearchResult {

    private static final long serialVersionUID = -296348695798571158L;
    private List<OntologyProxy> ontologies = new ArrayList<>();

    @JsonView(JsonIntegrationFilter.class)
    public List<OntologyProxy> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<OntologyProxy> ontologies) {
        this.ontologies = ontologies;
    }
}
