package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;

public class IntegrationWorkflowData implements Serializable {

    private static final long serialVersionUID = -4483089478294270554L;

    private int version = 1;

    private String title;
    private String description;

    public int getVersion() {
        return version;
    }

    private List<IntegrationColumnDTO> columns = new ArrayList<>();
    private List<DataTableDTO> dataTables = new ArrayList<>();
    private List<DatasetDTO> datasets = new ArrayList<>();
    private List<OntologyDTO> ontologies = new ArrayList<>();

    private IntegrationContext integrationContext = new IntegrationContext();

    private List<String> errors = new ArrayList<>();

    /**
     * Returns true if this
     * 
     * @param service
     * @return
     */
    public IntegrationContext toIntegrationContext(GenericService service) {
        validateIntegrationColumns(service);
        validateDatasets(service);
        validateOntologies(service);
        validateDataTables(service);
        integrationContext.setErrorMessages(errors);
        return integrationContext;
    }

    /**
     * FIXME: add error checking for missing entity ids or ontology node values
     * 
     * @param service
     */
    public void validateIntegrationColumns(GenericService service) {
        ArrayList<IntegrationColumn> integrationColumns = new ArrayList<>();
        // FIXME: less efficient than having a master list of ids and pulling all entities in one fell swoop instead of iteratively querying
        for (IntegrationColumnDTO column : columns) {
            ArrayList<Long> columnIds = new ArrayList<>();
            for (DataTableColumnDTO dtc : column.dataTableColumns) {
                columnIds.add(dtc.id);
            }
            IntegrationColumn.ColumnType columnType = IntegrationColumn.ColumnType.valueOf(column.type);
            IntegrationColumn integrationColumn = new IntegrationColumn(columnType);
            List<DataTableColumn> dataTableColumns = service.findAll(DataTableColumn.class, columnIds);
            integrationColumn.setColumns(dataTableColumns);
            if (columnType == IntegrationColumn.ColumnType.INTEGRATION) {
                // FIXME: verify that this ontology is only needed for true "integration" columns.
                Ontology sharedOntology = service.find(Ontology.class, column.ontology.id);
                integrationColumn.setSharedOntology(sharedOntology);
                ArrayList<Long> selectedNodeIds = new ArrayList<>();
                for (OntologyNodeDTO ond : column.nodeSelection) {
                    selectedNodeIds.add(ond.getId());
                }
                Set<OntologyNode> selectedNodes = new HashSet<>(service.findAll(OntologyNode.class, selectedNodeIds));
                // XXX: what is the difference between ontologyNodesForSelect & filteredOntologyNodes?
                integrationColumn.setOntologyNodesForSelect(selectedNodes);
            }
            integrationColumns.add(integrationColumn);
        }
        integrationContext.setIntegrationColumns(integrationColumns);
    }

    /**
     * FIXME: add error checking for missing ids
     * 
     * @param service
     */
    public void validateDatasets(GenericService service) {
        List<Long> datasetIds = new ArrayList<>();
        for (DatasetDTO d : datasets) {
            datasetIds.add(d.id);
        }
        List<Dataset> datasets = service.findAll(Dataset.class, datasetIds);
        // FIXME: no place to put the datasets on IntegrationContext currently
    }

    /**
     * FIXME: add error checking for missing ids
     * 
     * @param service
     */
    public void validateOntologies(GenericService service) {
        List<Long> ontologyIds = new ArrayList<>();
        for (OntologyDTO o : ontologies) {
            ontologyIds.add(o.id);
        }
        List<Ontology> ontologies = service.findAll(Ontology.class, ontologyIds);
        // FIXME: no place on IntegrationContext for all participating ontologies currently
    }

    /**
     * FIXME: add error checking for missing ids
     * 
     * @param service
     */
    public void validateDataTables(GenericService service) {
        List<Long> dataTableIds = new ArrayList<>();
        for (DataTableDTO d : dataTables) {
            dataTableIds.add(d.id);
        }
        List<DataTable> tables = service.findAll(DataTable.class, dataTableIds);
        integrationContext.setDataTables(tables);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColumns(List<IntegrationColumnDTO> columns) {
        this.columns = columns;
    }

    public void setDatasets(List<DatasetDTO> datasets) {
        this.datasets = datasets;
    }

    public void setOntologies(List<OntologyDTO> ontologies) {
        this.ontologies = ontologies;
    }

    public void setDataTables(List<DataTableDTO> dataTables) {
        this.dataTables = dataTables;
    }

    public List<String> getErrors() {
        return errors;
    }

    private static class IntegrationColumnDTO implements Serializable {
        private String type;
        private List<DataTableColumnDTO> dataTableColumns;
        private OntologyDTO ontology;
        private List<OntologyNodeDTO> nodeSelection = new ArrayList<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

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

    }

    private static class DatasetDTO implements Serializable {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    private static class DataTableDTO implements Serializable {
        private Long id;
        private String displayName;

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
    }

    private static class OntologyDTO implements Serializable {
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
    }

    // FIXME: consider replacing objects with just ids
    private static class OntologyNodeDTO implements Serializable {
        private Long id;
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
    }

    private static class DataTableColumnDTO implements Serializable {
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
    }

}
