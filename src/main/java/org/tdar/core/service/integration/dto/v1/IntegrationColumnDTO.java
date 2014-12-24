package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.service.integration.ColumnType;

public class IntegrationColumnDTO implements Serializable {

    private static final long serialVersionUID = 9061205188321045416L;
    private ColumnType type;
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

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

}