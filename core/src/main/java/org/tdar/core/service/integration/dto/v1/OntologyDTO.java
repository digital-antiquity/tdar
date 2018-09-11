package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.service.integration.dto.IntegrationDTO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
class OntologyDTO implements Serializable, IntegrationDTO<Ontology> {

    private static final long serialVersionUID = -7234646396247780253L;
    private Long id;
    private String title;
    private transient Ontology persistable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String displayName) {
        this.title = displayName;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", title, id);
    }

    @Override
    @JsonIgnore
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

    @JsonIgnore
    public Ontology getPersistable() {
        return persistable;
    }

    public void setPersistable(Ontology persistable) {
        this.persistable = persistable;
    }
}