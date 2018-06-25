package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.integration.dto.IntegrationDTO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
class DataTableDTO implements Serializable, IntegrationDTO<DataTable> {

    private static final long serialVersionUID = -3269819489102125775L;
    private Long id;
    private String displayName;
    private transient DataTable persistable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", displayName, id);
    }

    @Override
    @JsonIgnore
    public List<?> getEqualityFields() {
        return Arrays.asList(id);
    }

    @JsonIgnore
    public DataTable getPersistable() {
        return persistable;
    }

    public void setPersistable(DataTable persistable) {
        this.persistable = persistable;
    }
}