package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.service.integration.ColumnType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
public class IntegrationColumnDTO implements Serializable {

    private static final long serialVersionUID = 9061205188321045416L;
    private ColumnType type;
    private String name;
    private List<DataTableColumnDTO> dataTableColumns;
    private OntologyDTO ontology;
    private List<OntologyNodeDTO> nodeSelection = new ArrayList<>();

    public List<DataTableColumnDTO> getDataTableColumns() {
        return dataTableColumns;
    }

    public void setDataTableColumns(List<DataTableColumnDTO> dataTableColumns) {
        this.dataTableColumns = dataTableColumns;
    }

    public OntologyDTO getOntology() {
        return ontology;
    }

    public void setOntology(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public List<OntologyNodeDTO> getNodeSelection() {
        return nodeSelection;
    }

    public void setNodeSelection(List<OntologyNodeDTO> nodeSelection) {
        this.nodeSelection = nodeSelection;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, type);
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}