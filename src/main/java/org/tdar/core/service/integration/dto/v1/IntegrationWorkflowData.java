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
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.dto.AbstractIntegrationWorkflowData;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.utils.PersistableUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect
public class IntegrationWorkflowData extends AbstractIntegrationWorkflowData implements Serializable, IntegrationWorkflowWrapper {

    private static final long serialVersionUID = -4483089478294270554L;

    private int version = 1;
    private Long id = -1L;
    private String title;
    private String description;

    @Override
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
     * @throws IntegrationDeserializationException
     */
    @Override
    public IntegrationContext toIntegrationContext(GenericDao service) throws IntegrationDeserializationException {
        validate(service);
        integrationContext.setErrorMessages(errors);
        for (IntegrationColumnDTO ic_ : getColumns()) {
            IntegrationColumn col = new IntegrationColumn(ic_.getType());
            col.getColumns().addAll(service.findAll(DataTableColumn.class, PersistableUtils.extractIds(ic_.getDataTableColumns())));
            if (ic_.getType() == ColumnType.INTEGRATION) {
                col.getFilteredOntologyNodes().addAll(service.findAll(OntologyNode.class, PersistableUtils.extractIds(ic_.getNodeSelection())));
                col.setSharedOntology(service.find(Ontology.class, ic_.getOntology().getId()));
            }
            integrationContext.getIntegrationColumns().add(col);
        }
        integrationContext.setDataTables(service.findAll(DataTable.class, PersistableUtils.extractIds(getDataTables())));
        return integrationContext;
    }

    /**
     * FIXME: add error checking for missing entity ids or ontology node values
     * 
     * @param service
     * @throws IntegrationDeserializationException
     */
    private void validateIntegrationColumns(GenericDao service) throws IntegrationDeserializationException {
        Set<DataTableColumnDTO> dtcs = new HashSet<>();
        Set<OntologyNodeDTO> nodes = new HashSet<>();
        for (IntegrationColumnDTO column : getColumns()) {
            dtcs.addAll(column.getDataTableColumns());
            nodes.addAll(column.getNodeSelection());
        }
        super.validate(service, new ArrayList<DataTableColumnDTO>(dtcs), DataTableColumn.class);
        super.validate(service, new ArrayList<OntologyNodeDTO>(nodes), OntologyNode.class);
    }

    @Override
    public void validate(GenericDao service) throws IntegrationDeserializationException {
        super.validate(service, getDataTables(), DataTable.class);
        super.validate(service, getDatasets(), Dataset.class);
        super.validate(service, getOntologies(), Ontology.class);
        validateIntegrationColumns(service);
    }

    @Override
    public boolean isValid() {
        return errors.isEmpty();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<DataTableDTO> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTableDTO> dataTables) {
        this.dataTables = dataTables;
    }

    public List<DatasetDTO> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetDTO> datasets) {
        this.datasets = datasets;
    }

    public List<OntologyDTO> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<OntologyDTO> ontologies) {
        this.ontologies = ontologies;
    }

    public List<IntegrationColumnDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<IntegrationColumnDTO> columns) {
        this.columns = columns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class DatasetDTO implements Serializable, Persistable {

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
        public String toString() {
            return String.format("%s [%s]", title, id);
        }


        @Override
        @JsonIgnore
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class DataTableColumnDTO implements Serializable, Persistable {
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
        public String toString() {
            return String.format("%s [%s]", name, id);
        }

        @Override
        @JsonIgnore
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class DataTableDTO implements Serializable, Persistable {

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
        public String toString() {
            return String.format("%s [%s]", displayName, id);
        }

        @Override
        @JsonIgnore
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class IntegrationColumnDTO implements Serializable {

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

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class OntologyDTO implements Serializable, Persistable {

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
        public String toString() {
            return String.format("%s [%s]", title, id);
        }


        @Override
        @JsonIgnore
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }

    @JsonAutoDetect
    @JsonInclude(Include.NON_NULL)
    static class OntologyNodeDTO implements Serializable, Persistable {
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
        public String toString() {
            return String.format("%s [%s]", iri, id);
        }

        @Override
        @JsonIgnore
        public List<?> getEqualityFields() {
            return Arrays.asList(id);
        }
    }
}
