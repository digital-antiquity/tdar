package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
public class TableDetailsProxy implements Serializable {

    private static final long serialVersionUID = 2285948917451215790L;

    private List<DataTable> dataTables = new ArrayList<>();
    private List<Ontology> sharedOntologies = new ArrayList<>();

    @JsonView(JsonIntegrationDetailsFilter.class)
    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public List<Ontology> getMappedOntologies() {
        return sharedOntologies;
    }

    public void setSharedOntologies(List<Ontology> ontologies) {
        this.sharedOntologies = ontologies;
    }
}
