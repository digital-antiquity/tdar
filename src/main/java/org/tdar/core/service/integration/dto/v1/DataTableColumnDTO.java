package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.Persistable;

public class DataTableColumnDTO implements Serializable, Persistable {
    private static final long serialVersionUID = 1717839026465656147L;

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String displayName) {
        this.name = displayName;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

}
