package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.Persistable;

public class OntologyNodeDTO implements Serializable, Persistable {
    private static final long serialVersionUID = 6020897284883456005L;

    private Long id;
    private String iri;

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
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

}
