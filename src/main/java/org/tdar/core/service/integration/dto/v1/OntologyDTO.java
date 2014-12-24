package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.Persistable;


public class OntologyDTO implements Serializable, Persistable {

    private static final long serialVersionUID = -7234646396247780253L;
    private Long id;
    private String title;
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
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }
}