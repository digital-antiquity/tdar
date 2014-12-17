package org.tdar.core.dao.resource.integration;

import java.io.Serializable;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Ontology;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect 
public class OntologyProxy implements Serializable {

    private static final long serialVersionUID = -7517599753972933585L;

    private TdarUser submitter;
    private Ontology ontology;
    
    public OntologyProxy(Ontology ontology) {
        this.ontology = ontology;
        this.submitter = ontology.getSubmitter();
    }

    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

}
