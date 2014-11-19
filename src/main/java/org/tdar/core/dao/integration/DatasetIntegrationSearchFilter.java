package org.tdar.core.dao.integration;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.Ontology;

public class DatasetIntegrationSearchFilter extends IntegrationSearchFilter {

    private static final long serialVersionUID = -5221366878292263318L;

    private List<Ontology> ontologies = new ArrayList<>();

    public List<Ontology> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<Ontology> ontologies) {
        this.ontologies = ontologies;
    }
    

}
