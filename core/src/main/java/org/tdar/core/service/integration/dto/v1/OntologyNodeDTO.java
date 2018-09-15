package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.service.integration.dto.IntegrationDTO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility=Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
class OntologyNodeDTO implements Serializable, IntegrationDTO<OntologyNode> {
    private static final long serialVersionUID = 6020897284883456005L;

    private Long id;
    private String iri;
    private transient OntologyNode persistable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", iri, id);
    }

    @Override
    @JsonIgnore
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

    @JsonIgnore
    public OntologyNode getPersistable() {
        return persistable;
    }

    public void setPersistable(OntologyNode persistable) {
        this.persistable = persistable;
    }
}