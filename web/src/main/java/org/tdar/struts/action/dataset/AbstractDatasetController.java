package org.tdar.struts.action.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.MeasurementUnit;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.utils.PersistableUtils;
import org.tdar.workflows.RequiredOptionalPairs;

public abstract class AbstractDatasetController<R extends Dataset> extends AbstractInformationResourceController<R> {

    private static final long serialVersionUID = 6368347724977529964L;

    @Autowired
    private transient DatasetService datasetService;

    @Autowired
    private transient DataTableService dataTableService;

    @Autowired
    private transient OntologyService ontologyService;

    // column metadata incoming data
    // Each list contains some specific piece of metadata for the given data table, where
    // the index of the list maps to the ordering of the column names
    private DataTable dataTable;
    private List<DataTableColumn> dataTableColumns;

    private List<List<CategoryVariable>> subcategories = new ArrayList<List<CategoryVariable>>();
    // ontology mapped columns
    private DataTableColumn dataTableColumn;
    // incoming data
    private List<String> dataColumnValues = new ArrayList<String>();

    // outgoing ontology-related data
    // the set of coding rules that have "hits" in the DataTableColumn
    private List<CodingRule> mappedCodingRules = new ArrayList<CodingRule>();
    private List<DataTableColumn> ontologyMappedColumns;
    private List<String> ontologyMappedColumnStatus;

    private List<OntologyNode> ontologyNodes;

    private SortedMap<String, List<OntologyNode>> suggestions;

    private List<String> ontologyNames = new ArrayList<String>();
    private List<String> codingSheetNames = new ArrayList<String>();

    private Long dataTableId;

    @Override
    public void resolvePostSaveAction(R persistable) {
        setSaveSuccessPath(persistable.getResourceType().getUrlNamespace());
        if (isHasFileProxyChanges()) {
            if ((persistable.getTotalNumberOfActiveFiles() > 0) && CollectionUtils.isNotEmpty(persistable.getDataTables())) {
                setSaveSuccessPath(persistable.getUrlNamespace() + "/columns");
            }
        }
    }

    public List<DataTableColumn> getOntologyMappedColumns() {
        if (ontologyMappedColumns == null) {
            ontologyMappedColumns = dataTableService.findOntologyMappedColumns(getDataResource());
        }
        return ontologyMappedColumns;
    }

    /**
     * Returns the current data table to be viewed (only by the
     * editColumnMetadata method / columns action). If there is no current data
     * table specified via an incoming dataTableId query parameter, will return
     * the first DataTable in this Dataset's DataTables Set.
     * 
     * @return
     */
    public DataTable getDataTable() {
        getLogger().trace(dataTable + " dtID:" + dataTableId);
        if (dataTable == null) {
            if (dataTableId != null) {
                this.dataTable = dataTableService.find(dataTableId);
            } else {
                Set<DataTable> dataTables = getDataResource().getDataTables();
                if (!CollectionUtils.isEmpty(dataTables)) {
                    dataTable = dataTables.iterator().next();
                }
            }
        }
        return dataTable;
    }

    public void setDataTableId(Long dataTableId) {
        if (PersistableUtils.isNullOrTransient(dataTableId)) {
            getLogger().error("Trying to set data table id to null or -1: " + dataTableId);
            return;
        }
        this.dataTableId = dataTableId;
        // this.dataTable = dataTableService.find(dataTableId);
    }

    public Map<String, Long> getDistinctColumnValuesWithCounts() {
        if (getDataTableColumn() == null) {
            return Collections.emptyMap();
        }
        return dataTableService.findAllDistinctValuesWithCounts(dataTable, dataTableColumn);
    }

    public DataTableColumn getDataTableColumn() {
        if (dataTableColumn == null) {
            getOntologyMappedColumns();
            if (!CollectionUtils.isEmpty(ontologyMappedColumns)) {
                dataTableColumn = ontologyMappedColumns.get(0);
            }
        }
        return dataTableColumn;
    }

    public void setColumnId(Long columnId) {
        if (PersistableUtils.isNullOrTransient(columnId)) {
            getLogger().warn("Trying to set data table column id to null or -1: " + columnId);
            return;
        }
        this.dataTableColumn = dataTableService.findDataTableColumn(columnId);
        if ((dataTableColumn != null) && (getResource() == null)) {
            setId(dataTableColumn.getDataTable().getDataset().getId());
        }
    }

    public List<List<CategoryVariable>> getSubcategories() {
        return subcategories;
    }

    public List<OntologyNode> getOntologyNodes() {
        return ontologyNodes;
    }

    public void setDataColumnValues(List<String> matchingColumnValues) {
        this.dataColumnValues = matchingColumnValues;
    }

    public boolean isOntologyLinked() {
        return datasetService.canLinkDataToOntology(getDataResource());
    }

    private Dataset getDataResource() {
        return (Dataset) getPersistable();
    }

    public List<String> getDataColumnValues() {
        return dataColumnValues;
    }

    public SortedMap<String, List<OntologyNode>> getSuggestions() {
        return suggestions;
    }

    public List<String> getCodingSheetNames() {
        return codingSheetNames;
    }

    public List<String> getOntologyNames() {
        return ontologyNames;
    }

    public List<String> getOntologyMappedColumnStatus() {
        ontologyMappedColumnStatus = new ArrayList<String>();
        for (DataTableColumn column : getOntologyMappedColumns()) {
            String status = ontologyService.isOntologyMapped(column) ? "" : "unmapped";
            ontologyMappedColumnStatus.add(status);
        }
        getLogger().trace("{}", ontologyMappedColumnStatus);
        return ontologyMappedColumnStatus;
    }

    public List<MeasurementUnit> getAllMeasurementUnits() {
        return Arrays.asList(MeasurementUnit.values());
    }

    public List<DataTableColumnEncodingType> getAllColumnEncodingTypes() {
        return Arrays.asList(DataTableColumnEncodingType.values());
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public List<DataTableColumn> getDataTableColumns() {
        return dataTableColumns;
    }

    public void setDataTableColumns(List<DataTableColumn> dataTableColumns) {
        this.dataTableColumns = dataTableColumns;
    }

    /**
     * @return the mappedDataValues
     */
    public List<CodingRule> getMappedCodingRules() {
        return mappedCodingRules;
    }

    /**
     * @param mappedDataValues
     *            the mappedDataValues to set
     */
    public void setMappedCodingRules(List<CodingRule> mappedDataValues) {
        this.mappedCodingRules = mappedDataValues;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        Set<RequiredOptionalPairs> extensionsForType = getAnalyzer().getExtensionsForType(getPersistable().getResourceType(), ResourceType.DATASET);
        Set<String> exts = new HashSet<>();
        for (RequiredOptionalPairs pair : extensionsForType) {
            exts.addAll(pair.getOptional());
            exts.addAll(pair.getRequired());
        }
        return exts;
    }

    @Override
    protected void postSaveCallback(String actionMessage) {
        super.postSaveCallback(actionMessage);
        if (isHasFileProxyChanges() && getDataResource().hasMappingColumns()) {
            datasetService.remapAllColumnsAsync(getId(), getProject().getId());
        }
    }

}