package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.integration.dto.IntegrationDTO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
class DataTableColumnDTO implements Serializable, IntegrationDTO<DataTableColumn> {
    private static final long serialVersionUID = 1717839026465656147L;

    private Long id;
    private String name;
    private transient DataTableColumn persistable;

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
    public String toString() {
        return String.format("%s [%s]", name, id);
    }

    @Override
    @JsonIgnore
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

    @JsonIgnore
    public DataTableColumn getPersistable() {
        return persistable;
    }

    public void setPersistable(DataTableColumn persistable) {
        this.persistable = persistable;
    }
}