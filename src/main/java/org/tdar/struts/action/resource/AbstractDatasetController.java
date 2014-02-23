package org.tdar.struts.action.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.URLConstants;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.MeasurementUnit;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PaginationHelper;
import org.tdar.utils.Pair;

public abstract class AbstractDatasetController<R extends InformationResource> extends AbstractInformationResourceController<R> {

    public static final String RETRANSLATE = "retranslate";
    public static final String COLUMNS = "columns";
    public static final String REIMPORT = "reimport";
    private static final long serialVersionUID = 6368347724977529964L;
    public static final String SAVE_VIEW = "SAVE_VIEW";
    public static final String SAVE_MAP_THIS = "SAVE_MAP_THIS";

    private static final String INPUT_COLUMNS = "INPUT_COLUMNS";

    // public static final String SAVE_MAP_NEXT = "SAVE_MAP_NEXT";

    private Integer startRecord = 0;
    private Integer recordsPerPage = 10;

    public enum PostSaveColumnMapActions implements HasLabel {
        SAVE_VIEW("Save, and go to the view page", "Save, and go to the view page"),
        SAVE_MAP_THIS("Save, and return to this edit page", "Save, and return to this edit page");

        private String label;
        private String ontologyLabel;

        private PostSaveColumnMapActions(String label, String ontologyLabel) {
            this.setLabel(label);
            this.setOntologyLabel(ontologyLabel);
        }

        @Override
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getOntologyLabel() {
            return ontologyLabel;
        }

        public void setOntologyLabel(String ontologyLabel) {
            this.ontologyLabel = ontologyLabel;
        }

        public String getResultName(boolean gotoView, Dataset resource) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info(" {} {} ", resource.getTotalNumberOfActiveFiles(), resource.getDataTables());
            if (resource.getTotalNumberOfActiveFiles() < 1 || CollectionUtils.isEmpty(resource.getDataTables())) {
                return AbstractDatasetController.SAVE_VIEW;
            }
            return name();
        }
    }

    // column metadata incoming data
    // Each list contains some specific piece of metadata for the given data table, where
    // the index of the list maps to the ordering of the column names
    private DataTable dataTable;
    private List<DataTableColumn> dataTableColumns;

    private List<List<CategoryVariable>> subcategories = new ArrayList<List<CategoryVariable>>();
    private PostSaveColumnMapActions postSaveAction = PostSaveColumnMapActions.SAVE_VIEW;
    // ontology mapped columns
    private DataTableColumn dataTableColumn;
    // incoming data
    private List<String> dataColumnValues = new ArrayList<String>();

    // stores the next data table column's ontology mappings to visit within this dataset when mapping data column values to ontology nodes.
    // i.e., a cursor into ontologyMappedColumns
    private Long nextColumnId;

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
    private Long rowId;
    private Map<DataTableColumn, String> dataTableRowAsMap;
    private PaginationHelper paginationHelper;
    private InputStream xmlStream;

    @Action(value = REIMPORT, results = { @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID) })
    @WriteableSession
    public String reimport() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        // note this ignores the quota changes -- it's on us
        getDatasetService().reprocess(getDataResource());
        return SUCCESS;
    }

    /**
     * Retranslates the given dataset.
     * XXX: does this need a WritableSession?
     */
    @Action(value = RETRANSLATE, results = { @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID) })
    @WriteableSession
    public String retranslate() throws TdarActionException {
        // note this ignores the quota changes -- it's on us
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        for (DataTable table : getDataResource().getDataTables()) {
            getDatasetService().retranslate(table.getDataTableColumns());
        }
        getDatasetService().createTranslatedFile(getDataResource());
        return SUCCESS;
    }

    public void resolvePostSaveAction(Dataset persistable) {
        if (isHasFileProxyChanges()) {
            if (persistable.getTotalNumberOfActiveFiles() > 0 && CollectionUtils.isNotEmpty(persistable.getDataTables())) {
                setSaveSuccessPath("columns");
            }
        }
    }

    @SkipValidation
    @Action(value = COLUMNS, results = { @Result(name = SUCCESS, location = "../dataset/edit-column-metadata.ftl") })
    public String editColumnMetadata() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        if (getDataResource().getLatestVersions().isEmpty()) {
            addActionError(getText("abstractDatasetController.upload_data_file_first"));
            return INPUT;
        }

        if (CollectionUtils.isEmpty(getDataResource().getDataTables())) {
            addActionError(getText("abstractDatasetController.no_tables"));
            return INPUT;
        }
        // load existing column metadata if any.
        DataTable currentDataTable = getDataTable();
        List<DataTableColumn> columns = new ArrayList<>(currentDataTable.getSortedDataTableColumns());
        // FIXME: replace with Pagination helper

        setPaginationHelper(PaginationHelper.withStartRecord(columns.size(), getRecordsPerPage(), 100, getStartRecord()));

        if (CollectionUtils.size(columns) > getRecordsPerPage()) {
            columns = columns.subList(paginationHelper.getFirstItem(), paginationHelper.getLastItem() + 1);
        }
        for (DataTableColumn column : columns) {
            CategoryVariable categoryVariable = column.getCategoryVariable();
            if (categoryVariable == null) {
                subcategories.add(null);
            } else {
                if (categoryVariable.getType() == CategoryType.CATEGORY) {
                    // make sure that the subcategories get populated with the
                    // children of the parent even though none were selected.
                    subcategories.add(new ArrayList<CategoryVariable>(categoryVariable.getSortedChildren()));
                } else { // category is a subcategory
                    subcategories.add(new ArrayList<CategoryVariable>(categoryVariable.getParent().getSortedChildren()));
                }
            }
        }
        setDataTableColumns(columns);

        getLogger().debug("passing off to Freemarker");
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "save-column-metadata", results = {
            @Result(name = SAVE_VIEW, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID),
            @Result(name = SAVE_MAP_THIS, type = REDIRECT, location = URLConstants.COLUMNS_RESOURCE_ID),
            @Result(name = INPUT_COLUMNS, location = "../dataset/edit-column-metadata.ftl")
    })
    /**
     * Saves column metadata for each column in a given DataTable (set on the controller and retrievable via getDataTable()).
     * 
     * Does some additional work when linking a CodingSheet and/or Ontology to a DataTableColumn.
     * 
     * @return
     * @throws TdarActionException
     */
    public String saveColumnMetadata() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        Pair<Boolean, List<DataTableColumn>> updateResults = new Pair<Boolean, List<DataTableColumn>>(false, new ArrayList<DataTableColumn>());
        try {
            updateResults = getDatasetService().updateColumnMetadata(getDataResource(), getDataTable(), getDataTableColumns(), getAuthenticatedUser());
        } catch (Throwable tde) {
            getLogger().error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT_COLUMNS;
        }
        this.columnsToRemap = updateResults.getSecond();
        getResourceService().logRecordXmlToFilestore(getDataResource());
        postSaveColumnMetadataCleanup();
        return getPostSaveAction().getResultName(!updateResults.getFirst(), (Dataset) getPersistable());
    }

    private List<DataTableColumn> columnsToRemap;

    /**
     * Used to render a row within a {@link Dataset}.
     * The expected URL is of the form /datatable/view-row?dataTableId=5815&rowId=1 where dataTableId = data table id, and rowId is the tDAR row id within
     * the table.
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find and display the row, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "view-row", results = {
            @Result(name = SUCCESS, location = "../dataset/view-row.ftl") })
    @SkipValidation
    public String getDataResultsRow() {
        if (!isViewRowSupported()) {
            return ERROR;
        }
        setTransientViewableStatus(getResource(), getAuthenticatedUser());
        dataTableRowAsMap = new HashMap<>();
        if (Persistable.Base.isNullOrTransient(dataTableId) || Persistable.Base.isNullOrTransient(rowId)) {
            return ERROR;
        }
        DataTable dataTable = getDataTableService().find(dataTableId);
        if (dataTable != null) {
            if (getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), getResource())) {
                dataTableRowAsMap = getDatasetService().selectRowFromDataTable(dataTable, rowId, true);
                if (MapUtils.isEmpty(dataTableRowAsMap)) {
                    return ERROR;
                }
                return SUCCESS;
            }
        }
        return ERROR;
    }

    /**
     * @return The output of the xml request to the database wrapped in a stream
     */
    public InputStream getXmlStream() {
        return xmlStream;
    }

    /**
     * Used to return the contents of a table in a {@link Dataset} wrapped in XML.
     * The expected URL is of the form /dataset/xml?dataTableId=5818 where dataTableId = data table id
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find and display the table, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "xml",
            results = {
                    @Result(name = TdarActionSupport.SUCCESS,
                            type = "stream",
                            params = {
                                    "contentDisposition", "attachment; filename=\"${dataTable.name}.xml\"",
                                    "contentType", "text/xml",
                                    "inputName", "xmlStream",
                            }),
                    @Result(name = TdarActionSupport.ERROR, type = "httpheader", params = { "error", "404" }),
            })
    @SkipValidation
    public String getTableAsXml() {
        xmlStream = null;
        if (!getTdarConfiguration().isXmlExportEnabled()) {
            return ERROR;
        }
        if (Persistable.Base.isNullOrTransient(dataTableId)) {
            return ERROR;
        }
        DataTable dataTable = getDataTableService().find(dataTableId);
        if (dataTable != null) {
            if (getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), getResource())) {
                String dataTableAsXml = getDatasetService().selectTableAsXml(dataTable);
                xmlStream = new ByteArrayInputStream(dataTableAsXml.getBytes(StandardCharsets.UTF_8));
                return SUCCESS;
            }
        }
        return ERROR;
    }

    protected void postSaveColumnMetadataCleanup() {
        if (CollectionUtils.isNotEmpty(columnsToRemap)) {
            if (isAsync()) {
                getDatasetService().remapColumnsAsync(columnsToRemap, getDataResource().getProject());
            } else {
                getDatasetService().remapColumns(columnsToRemap, getDataResource().getProject());
            }
        }
    };

    public List<DataTableColumn> getOntologyMappedColumns() {
        if (ontologyMappedColumns == null) {
            ontologyMappedColumns = getDataTableService().findOntologyMappedColumns(getDataResource());
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
                this.dataTable = getDataTableService().find(dataTableId);
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
        if (Persistable.Base.isNullOrTransient(dataTableId)) {
            getLogger().error("Trying to set data table id to null or -1: " + dataTableId);
            return;
        }
        this.dataTableId = dataTableId;
        // this.dataTable = getDataTableService().find(dataTableId);
    }

    public Map<String, Long> getDistinctColumnValuesWithCounts() {
        if (getDataTableColumn() == null) {
            return Collections.emptyMap();
        }
        return getDataTableService().findAllDistinctValuesWithCounts(dataTableColumn);
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
        if (Persistable.Base.isNullOrTransient(columnId)) {
            getLogger().warn("Trying to set data table column id to null or -1: " + columnId);
            return;
        }
        this.dataTableColumn = getDataTableService().findDataTableColumn(columnId);
        if (dataTableColumn != null && getResource() == null) {
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
        return getDatasetService().canLinkDataToOntology(getDataResource());
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

    public Long getNextColumnId() {
        return nextColumnId;
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
            String status = getOntologyService().isOntologyMapped(column) ? "" : "unmapped";
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

    public PostSaveColumnMapActions getPostSaveAction() {
        return postSaveAction;
    }

    public void setPostSaveAction(PostSaveColumnMapActions postSaveAction) {
        this.postSaveAction = postSaveAction;
    }

    public List<PostSaveColumnMapActions> getAllSaveActions() {
        return Arrays.asList(PostSaveColumnMapActions.values());
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
        return getAnalyzer().getExtensionsForTypes(getPersistable().getResourceType(), ResourceType.DATASET);
    }

    /**
     * @return the dataTableRowAsMap ie: the column header information, and a row with the given rowId the table
     */
    public Map<DataTableColumn, String> getDataTableRowAsMap() {
        return dataTableRowAsMap;
    }

    /**
     * @return the rowId of the row that is in being requested by the view-row call
     */
    public Long getRowId() {
        return rowId;
    }

    /**
     * @param rowId
     *            set the rowId of the row that will be returned in a view-row call
     */
    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public PaginationHelper getPaginationHelper() {
        return paginationHelper;
    }

    public void setPaginationHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public Integer getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(Integer startRecord) {
        this.startRecord = startRecord;
    }

    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(Integer recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

}