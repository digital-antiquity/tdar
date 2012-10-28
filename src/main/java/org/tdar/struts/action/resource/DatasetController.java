package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.MeasurementUnit;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;

/**
 * $Id$
 * <p>
 * Manages CRUD requests for Dataset metadata including column-level metadata that enables translation of a column via a CodingSheet, mapping of column data
 * values to nodes within an ontology, and association of individual rows within a table to Resources within tdar (e.g., a Mimbres image database).
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/dataset")
@Result(name = "input", location = "edit.ftl")
public class DatasetController extends AbstractInformationResourceController<Dataset> {

    private static final long serialVersionUID = 2874916865886637108L;

    public static final String SAVE_VIEW = "SAVE_VIEW";
    public static final String SAVE_MAP_THIS = "SAVE_MAP_THIS";

    private static final String INPUT_COLUMNS = "INPUT_COLUMNS";

    // public static final String SAVE_MAP_NEXT = "SAVE_MAP_NEXT";

    public enum PostSaveColumnMapActions implements HasLabel {
        SAVE_VIEW("Save, and go to the view page", "Save, and go to the view page"),
        SAVE_MAP_THIS("Save, and return to this edit page", "Save, and return to this edit page");

        private String label;
        private String ontologyLabel;

        private PostSaveColumnMapActions(String label, String ontologyLabel) {
            this.setLabel(label);
            this.setOntologyLabel(ontologyLabel);
        }

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

        public String getResultName(boolean gotoView) {
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

    @Actions({ @Action("citations") })
    @Override
    public String execute() {
        if (isNullOrNew()) {
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @Action(value = "reimport", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}") })
    @WriteableSession
    public String reimport() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getDatasetService().reprocess(getPersistable());
        return SUCCESS;
    }

    /**
     * Retranslates the given dataset.
     * XXX: does this need a WritableSession?
     */
    @Action(value = "retranslate", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}") })
    @WriteableSession
    public String retranslate() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        for (DataTable table : getPersistable().getDataTables()) {
            getDatasetService().retranslate(table.getDataTableColumns());
        }
        getDatasetService().createTranslatedFile(getPersistable());
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "columns", results = { @Result(name = SUCCESS, location = "edit-column-metadata.ftl") })
    public String editColumnMetadata() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        if (getPersistable().getLatestVersions().isEmpty()) {
            addActionError("You should upload a data file before attempting to register column metadata.");
            return INPUT;
        }

        if (CollectionUtils.isEmpty(getPersistable().getDataTables())) {
            addActionError("No data tables were found for this resource.");
            return INPUT;
        }
        // load existing column metadata if any.
        DataTable currentDataTable = getDataTable();

        for (DataTableColumn column : currentDataTable.getSortedDataTableColumns()) {
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
        setDataTableColumns(currentDataTable.getSortedDataTableColumns());

        getLogger().debug("passing off to Freemarker");
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "save-column-metadata", results = {
            @Result(name = SAVE_VIEW, type = "redirect", location = "view?id=${resource.id}"),
            @Result(name = SAVE_MAP_THIS, type = "redirect", location = "columns?id=${resource.id}"),
            @Result(name = INPUT_COLUMNS, location = "edit-column-metadata.ftl")
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
        boolean hasOntologies = false;
        try {
            hasOntologies = getDatasetService().updateColumnMetadata(getPersistable(), getDataTable(), getDataTableColumns(), getAuthenticatedUser());
        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT_COLUMNS;
        }
        return getPostSaveAction().getResultName(!hasOntologies);
    }

    @Override
    protected String save(Dataset dataset) {
        getLogger().debug("Saving dataset: {}", dataset);
        // save basic metadata
        super.saveBasicResourceMetadata();

        super.saveInformationResourceProperties();
        getDatasetService().saveOrUpdate(dataset);
        // HACK: implicitly cache fullUsers via call to getProjectAsJson() as workaround for TDAR-1162. This is the software equivalent of turning the radio up
        // to mask weird sounds your engine is making

        handleUploadedFiles();
        boolean fileChanged = false;
        for (FileProxy proxy : getFileProxies()) {
            if (proxy.getAction().equals(FileAction.ADD) || proxy.getAction().equals(FileAction.REPLACE)) {
                fileChanged = true;
            }
        }
        // logger.debug("{}", getFileProxies());
        if (fileChanged) {
            setSaveSuccessPath("columns");
        }
        return SUCCESS;
    }

    public List<Dataset> getAllSubmittedDatasets() {
        return getDatasetService().findBySubmitter(getAuthenticatedUser());
    }

    public List<DataTableColumn> getOntologyMappedColumns() {
        if (ontologyMappedColumns == null) {
            ontologyMappedColumns = getDataTableService().findOntologyMappedColumns(getPersistable());
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
        logger.trace(dataTable + " dtID:" + dataTableId);
        if (dataTable == null) {
            if (dataTableId != null) {
                this.dataTable = getDataTableService().find(dataTableId);
            } else {
                Set<DataTable> dataTables = getPersistable().getDataTables();
                if (!CollectionUtils.isEmpty(dataTables)) {
                    dataTable = dataTables.iterator().next();
                }
            }
        }
        return dataTable;
    }

    public void setDataTableId(Long dataTableId) {
        if (Persistable.Base.isNullOrTransient(dataTableId)) {
            logger.error("Trying to set data table id to null or -1: " + dataTableId);
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
            logger.warn("Trying to set data table column id to null or -1: " + columnId);
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

    public String getContentDisposition() {
        return String.format("filename=\"dataset_%s.xls\"", getPersistable().getId());
    }

    public List<OntologyNode> getOntologyNodes() {
        return ontologyNodes;
    }

    public void setDataColumnValues(List<String> matchingColumnValues) {
        this.dataColumnValues = matchingColumnValues;
    }

    public boolean isOntologyLinked() {
        return getDatasetService().canLinkDataToOntology(getPersistable());
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
        logger.trace("{}", ontologyMappedColumnStatus);
        return ontologyMappedColumnStatus;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.DATASET);
    }

    public List<MeasurementUnit> getAllMeasurementUnits() {
        return Arrays.asList(MeasurementUnit.values());
    }

    public List<DataTableColumnEncodingType> getAllColumnEncodingTypes() {
        return Arrays.asList(DataTableColumnEncodingType.values());
    }

    public void setDataset(Dataset dataset) {
        setPersistable(dataset);
    }

    public Dataset getDataset() {
        return getPersistable();
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
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

    @Override
    public boolean supportsMultipleFileUpload() {
        return false;
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
}
