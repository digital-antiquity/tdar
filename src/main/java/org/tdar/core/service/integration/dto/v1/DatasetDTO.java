package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.Persistable;

public class DatasetDTO implements Serializable, Persistable {

    private static final long serialVersionUID = -7582567713165436710L;
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

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }
}
