package org.tdar.core.service.integration.dto.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.dto.AbstractIntegrationWorkflowData;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.IntegrationWorkflowWrapper;
import org.tdar.utils.PersistableUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.opensymphony.xwork2.TextProvider;

//@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
// @JsonIgnoreProperties(ignoreUnknown = true) //allow superset objects
public class IntegrationWorkflowData extends AbstractIntegrationWorkflowData implements Serializable, IntegrationWorkflowWrapper {

    // hash keys
    public static final String ONTOLOGY = "ontology";
    public static final String DATA_TABLE = "dataTable";
    public static final String DATASET = "dataset";
    public static final String DATA_TABLE_COLUMN = "dataTableColumn";
    public static final String NODE = "node";

    private static final long serialVersionUID = -4483089478294270554L;

    private int version = 1;
    private Long id = -1L;
    private String title;
    private String description;
    private Map<String, List<String>> fieldErrors = new HashMap<>();

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
    public IntegrationContext toIntegrationContext(GenericDao service, TextProvider provider) throws IntegrationDeserializationException {
        validate(service, provider);
        integrationContext.setErrorMessages(errors);
        for (IntegrationColumnDTO ic_ : getColumns()) {
            IntegrationColumn col = new IntegrationColumn(ic_.getType());
            col.getColumns().addAll(service.findAll(DataTableColumn.class, PersistableUtils.extractIds(ic_.getDataTableColumns())));
            if (ic_.getType() == ColumnType.INTEGRATION) {
                col.getFilteredOntologyNodes().addAll(service.findAll(OntologyNode.class, PersistableUtils.extractIds(ic_.getNodeSelection())));
                col.setSharedOntology(service.find(Ontology.class, ic_.getOntology().getId()));
                // if empty --add all
                if (CollectionUtils.isEmpty(col.getFilteredOntologyNodes())) {
                    col.getFilteredOntologyNodes().addAll(col.getSharedOntology().getOntologyNodes());
                }
            }
            col.setName(ic_.getName());
            integrationContext.getIntegrationColumns().add(col);
        }
        integrationContext.setDataTables(service.findAll(DataTable.class, PersistableUtils.extractIds(getDataTables())));
        integrationContext.setTitle(title);
        integrationContext.setDescription(description);
        return integrationContext;
    }

    /**
     * Validates the data object passing in a dao and a text provider. Checks for:
     * ** NULLs or invalid Object References
     * ** Invalid hierarchical references DataTable <-> DataTableColumn
     * ** Changes in Ontology Mappings
     * ** Invalid Count columns
     */
    @Override
    public void validate(GenericDao service, TextProvider provider) throws IntegrationDeserializationException {
        hydrateAllObjects(service);

        List<Long> datasetIds = PersistableUtils.extractIds(getDatasets());
        List<Long> dataTableIds = PersistableUtils.extractIds(getDataTables());
        // remove nulls for general error checking
        datasetIds.removeAll(Collections.singleton(null));
        dataTableIds.removeAll(Collections.singleton(null));
        try {
            validateEntriesForNulls(service, provider, getFieldErrors(), datasetIds);

            if (CollectionUtils.isEmpty(getColumns())) {
                return;
            }
            // check for references
            for (IntegrationColumnDTO col : getColumns()) {
                if (col == null) {
                    continue;
                }
                validateDataTableColumn(provider, dataTableIds, col);
                validateOntologyNode(provider, col);
            }
        } catch (Exception e) {
            logger.error("integration context validation error:", e);
            errors.add(provider.getText("integrationWorkflowData.genericError", Arrays.asList(e.getMessage())));
        }
        if (CollectionUtils.isNotEmpty(errors) || MapUtils.isNotEmpty(fieldErrors)) {
            throw new IntegrationDeserializationException(errors, fieldErrors);
        }
    }

    /**
     * validate that the ontology node is valid:
     * check parent is right
     * 
     * @param provider
     * @param col
     */
    private void validateOntologyNode(TextProvider provider, IntegrationColumnDTO col) {
        if (col.getType() == ColumnType.INTEGRATION) {
            for (OntologyNodeDTO node : col.getNodeSelection()) {
                if (PersistableUtils.isNullOrTransient(node.getPersistable()) || PersistableUtils.isNullOrTransient(col.getOntology())) {
                    continue;
                }
                if (!Objects.equal(col.getOntology().getId(), node.getPersistable().getOntology().getId())) {
                    checkAddKey(getFieldErrors(), NODE).add(
                            provider.getText("integrationWorkflowData.bad_node_mapping", Arrays.asList(node, col.getOntology())));
                }
            }
        }
    }

    /**
     * check that the DTC is valid - part of mapped table; ontology is valid and mapepd; is count column numeric
     * 
     * @param provider
     * @param dataTableIds
     * @param col
     */
    private void validateDataTableColumn(TextProvider provider, List<Long> dataTableIds, IntegrationColumnDTO col) {
        for (DataTableColumnDTO dtc : col.getDataTableColumns()) {
            if (dtc == null || PersistableUtils.isNullOrTransient(dtc.getPersistable())) {
                continue;
            }

            // make sure the DataTableColumn is in a valid DataTable
            if (!dataTableIds.contains(dtc.getPersistable().getDataTable().getId())) {
                checkAddKey(getFieldErrors(), DATA_TABLE_COLUMN).add(provider.getText("integrationWorkflowData.bad_datatable_column", Arrays.asList(dtc)));
            }
            // make sure the Ontologies match
            if (col.getType() == ColumnType.INTEGRATION &&
                    !Objects.equal(col.getOntology().getId(), getOntologyId(dtc))) {
                checkAddKey(getFieldErrors(), DATA_TABLE_COLUMN).add(
                        provider.getText("integrationWorkflowData.bad_datatable_column_ontology_mapping", Arrays.asList(dtc, col.getOntology())));
            }
            // make sure a count column is still a count column
            if (col.getType() == ColumnType.COUNT &&
                    (!dtc.getPersistable().getColumnEncodingType().isCount() || !dtc.getPersistable().getColumnDataType().isNumeric())) {
                checkAddKey(getFieldErrors(), DATA_TABLE_COLUMN).add(
                        provider.getText("integrationWorkflowData.bad_datatable_column_count", Arrays.asList(dtc, col.getOntology())));
            }
        }
    }

    private Long getOntologyId(DataTableColumnDTO dtc) {
        try {
            CodingSheet defaultCodingSheet = dtc.getPersistable().getDefaultCodingSheet();
            if (defaultCodingSheet == null || defaultCodingSheet.getDefaultOntology() == null) {
                return null;
            }
            return defaultCodingSheet.getDefaultOntology().getId();
        } catch (NullPointerException npe) {
            logger.debug("null pointer getting ontology id", npe);
        }
        return null;
    }

    /**
     * make sure that we don't have NULLs in mappings (once hydrated, make sure that we still have valid refernces)
     * 
     * @param service
     * @param provider
     * @param fieldErrors
     * @param datasetIds
     */
    private void validateEntriesForNulls(GenericDao service, TextProvider provider, Map<String, List<String>> fieldErrors, List<Long> datasetIds) {
        super.validateForNulls(service, getDatasets(), fieldErrors, DATASET, provider);
        super.validateForNulls(service, getDataTables(), fieldErrors, DATA_TABLE, provider);
        super.validateForNulls(service, getOntologies(), fieldErrors, ONTOLOGY, provider);
        for (IntegrationColumnDTO column : getColumns()) {
            super.validateForNulls(service, column.getDataTableColumns(), fieldErrors, DATA_TABLE_COLUMN, provider);
            super.validateForNulls(service, column.getNodeSelection(), fieldErrors, NODE, provider);
            super.validateForNulls(service, Arrays.asList(column.getOntology()), fieldErrors, ONTOLOGY, provider);
        }

        if (CollectionUtils.isEmpty(datasetIds)) {
            // escape out if we have no datasets -- not immplemented
            return;
        }

        for (DataTableDTO dt : getDataTables()) {
            if (PersistableUtils.isNotNullOrTransient(dt.getPersistable()) && !datasetIds.contains(dt.getPersistable().getDataset().getId())) {
                checkAddKey(fieldErrors, DATA_TABLE).add(provider.getText("integrationWorkflowData.bad_datatable", Arrays.asList(dt)));
            }
        }
    }

    /**
     * load database versions and set them as the "persistable"
     * 
     * @param service
     * @throws IntegrationDeserializationException
     */
    private void hydrateAllObjects(GenericDao service) throws IntegrationDeserializationException {
        hydrate(service, getDataTables(), DataTable.class);
        hydrate(service, getDatasets(), Dataset.class);
        hydrate(service, getOntologies(), Ontology.class);
        for (IntegrationColumnDTO column : getColumns()) {
            hydrate(service, column.getDataTableColumns(), DataTableColumn.class);
            hydrate(service, column.getNodeSelection(), OntologyNode.class);
            hydrate(service, Arrays.asList(column.getOntology()), Ontology.class);
        }
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

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    @JsonIgnore
    public boolean hasErrors() {
        if (CollectionUtils.isNotEmpty(errors)) {
            return true;
        }

        if (MapUtils.isNotEmpty(fieldErrors)) {
            return true;
        }
        return false;
    }
}
