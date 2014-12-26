package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.utils.PersistableUtils;

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
            List<Long> columnIds = PersistableUtils.extractIds(column.getDataTableColumns());

            IntegrationColumn integrationColumn = new IntegrationColumn(column.getType());
            List<DataTableColumn> dataTableColumns = service.findAll(DataTableColumn.class, columnIds);
            integrationColumn.setColumns(dataTableColumns);
            if (column.getType() == ColumnType.INTEGRATION) {
                Ontology sharedOntology = service.find(Ontology.class, column.getOntology().getId());
                integrationColumn.setSharedOntology(sharedOntology);
                List<Long> selectedNodeIds = PersistableUtils.extractIds(column.getNodeSelection());

                Set<OntologyNode> selectedNodes = new HashSet<>(service.findAll(OntologyNode.class, selectedNodeIds));
                integrationColumn.getFilteredOntologyNodes().addAll(selectedNodes);
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
        List<Long> datasetIds = PersistableUtils.extractIds(datasets);
        List<Dataset> datasets_ = service.findAll(Dataset.class, datasetIds);
        // FIXME: no place to put the datasets on IntegrationContext currently
    }

    /**
     * FIXME: add error checking for missing ids
     * 
     * @param service
     */
    public void validateOntologies(GenericService service) {
        List<Long> ontologyIds = PersistableUtils.extractIds(ontologies);
        List<Ontology> ontologies_ = service.findAll(Ontology.class, ontologyIds);
        // FIXME: no place on IntegrationContext for all participating ontologies currently
    }

    /**
     * FIXME: add error checking for missing ids
     * 
     * @param service
     */
    public void validateDataTables(GenericService service) {
        List<Long> dataTableIds = PersistableUtils.extractIds(dataTables);
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

    private static class DatasetDTO implements Serializable, Persistable {

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

    private static class DataTableColumnDTO implements Serializable, Persistable {
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

    private static class DataTableDTO implements Serializable, Persistable {

        private static final long serialVersionUID = -3269819489102125775L;
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

        @Override
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }

    private static class IntegrationColumnDTO implements Serializable {

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
    
    private static class OntologyDTO implements Serializable, Persistable {

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

    private static class OntologyNodeDTO implements Serializable, Persistable {
        private static final long serialVersionUID = 6020897284883456005L;

        private Long id;
        private String iri;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }
        
        @Override
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }

    }
}
