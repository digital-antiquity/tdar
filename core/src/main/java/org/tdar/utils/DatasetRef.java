package org.tdar.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTableColumn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
public class DatasetRef implements Serializable {

    private static final long serialVersionUID = -5773311255385514184L;
    private List<Long> collectionIds = new ArrayList<>();
    private Set<DataTableColumn> columns = new HashSet<>();
    private String title;
    private Long id;

    public List<Long> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<Long> collectionIds) {
        this.collectionIds = collectionIds;
    }

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

    public Set<DataTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(Set<DataTableColumn> columns) {
        this.columns = columns;
    }
}
