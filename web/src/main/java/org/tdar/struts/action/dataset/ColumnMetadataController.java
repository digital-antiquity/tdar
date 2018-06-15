package org.tdar.struts.action.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.UrlConstants;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.MeasurementUnit;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PaginationHelper;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value = {
        @Namespace("/dataset/columns"),
        @Namespace("/geospatial/columns"),
        @Namespace("/sensory-data/columns")
})
@HttpsOnly
public class ColumnMetadataController extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<Dataset> {

    private static final long serialVersionUID = 657544410406621681L;
    public static final String COLUMNS = "columns";
    public static final String SAVE_VIEW = "SAVE_VIEW";
    public static final String SAVE_MAP_THIS = "SAVE_MAP_THIS";
    private Long startTime;
    private static final String INPUT_COLUMNS = "INPUT_COLUMNS";

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient DatasetService datasetService;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    @Autowired
    private transient DataTableService dataTableService;

    private Dataset persistable;
    private List<CategoryVariable> allDomainCategories;

    private Dataset getDataResource() {
        return getPersistable();
    }

    private Long id;
    private String tableDescription;
    private DataTable dataTable;
    private List<DataTableColumn> dataTableColumns;
    private PaginationHelper paginationHelper;
    private Long dataTableId;
    // stores the next data table column's ontology mappings to visit within this dataset when mapping data column values to ontology nodes.
    // i.e., a cursor into ontologyMappedColumns
    private Long nextColumnId;

    private List<List<CategoryVariable>> subcategories = new ArrayList<List<CategoryVariable>>();
    private PostSaveColumnMapActions postSaveAction = PostSaveColumnMapActions.SAVE_VIEW;

    private Integer startRecord = 0;
    private Integer recordsPerPage = 10;

    public enum PostSaveColumnMapActions implements HasLabel, Localizable {
        SAVE_VIEW("Save, and go to the view page", "Save, and go to the view page"),
        SAVE_MAP_THIS("Save, and return to this edit page",
                "Save, and return to this edit page");

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

        @Override
        public String getLocaleKey() {
            return MessageHelper.formatLocalizableKey(this);
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
            if ((resource.getTotalNumberOfActiveFiles() < 1) || CollectionUtils.isEmpty(resource.getDataTables())) {
                return ColumnMetadataController.SAVE_VIEW;
            }
            return name();
        }
    }

    @SkipValidation
    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "../../dataset/edit-column-metadata.ftl"),
    })
    public String editColumnMetadata() throws TdarActionException {

        if (getDataResource().getLatestVersions().isEmpty()) {
            addActionError(getText("abstractDatasetController.upload_data_file_first"));
            return INPUT;
        }

        if (CollectionUtils.isEmpty(getDataResource().getDataTables())) {
            addActionError(getText("abstractDatasetController.no_tables"));
            return INPUT;
        }
        List<DataTableColumn> columns = initializePaginationHelper();
        setTableDescription(getDataTable().getDescription());

        if (CollectionUtils.size(columns) > getRecordsPerPage()) {
            columns = columns.subList(getPaginationHelper().getFirstItem(), getPaginationHelper().getLastItem() + 1);
        }
        buildSubcategoriesList(columns);
        setDataTableColumns(columns);

        return SUCCESS;
    }

    private void buildSubcategoriesList(List<DataTableColumn> columns) {
        for (DataTableColumn column : columns) {
            CategoryVariable categoryVariable = column.getCategoryVariable();
            if (categoryVariable == null) {
                getSubcategories().add(null);
            } else {
                if (categoryVariable.getType() == CategoryType.CATEGORY) {
                    // make sure that the subcategories get populated with the
                    // children of the parent even though none were selected.
                    getSubcategories().add(new ArrayList<CategoryVariable>(categoryVariable.getSortedChildren()));
                } else { // category is a subcategory
                    getSubcategories().add(new ArrayList<CategoryVariable>(categoryVariable.getParent().getSortedChildren()));
                }
            }
        }
    }

    @SkipValidation
    @WriteableSession
    @PostOnly
    @Action(value = "save-column-metadata",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SAVE_VIEW, type = TDAR_REDIRECT, location = "/${resource.resourceType.urlNamespace}/${resource.id}"),
                    @Result(name = SAVE_MAP_THIS, type = TDAR_REDIRECT, location = UrlConstants.COLUMNS_RESOURCE_ID),
                    @Result(name = INPUT_COLUMNS, location = "../../dataset/edit-column-metadata.ftl")
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
        boolean hasOntologies = false;
        initializePaginationHelper();
        getDataTable().setDescription(getTableDescription());
        try {
            hasOntologies = datasetService.updateColumnMetadata(this, getDataResource(), getDataTable(), getDataTableColumns(), getAuthenticatedUser(),
                    startTime);
        } catch (Throwable tde) {
            getLogger().error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT_COLUMNS;
        }
        return getPostSaveAction().getResultName(!hasOntologies, getDataResource());
    }

    public Dataset getPersistable() {
        return persistable;
    }

    public void setPersistable(Dataset persistable) {
        this.persistable = persistable;
    }

    private List<DataTableColumn> initializePaginationHelper() {
        DataTable currentDataTable = getDataTable();
        List<DataTableColumn> columns = new ArrayList<>(currentDataTable.getSortedDataTableColumns());
        setPaginationHelper(PaginationHelper.withStartRecord(columns.size(), getRecordsPerPage(), 100, getStartRecord()));
        return columns;
    }

    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(Integer recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public Integer getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(Integer startRecord) {
        this.startRecord = startRecord;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
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

    public List<List<CategoryVariable>> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<List<CategoryVariable>> subcategories) {
        this.subcategories = subcategories;
    }

    public List<PostSaveColumnMapActions> getAllSaveActions() {
        return Arrays.asList(PostSaveColumnMapActions.values());
    }

    public PaginationHelper getPaginationHelper() {
        return paginationHelper;
    }

    public void setPaginationHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        checkValidRequest(this);

        if (dataTableId != null) {
            this.dataTable = dataTableService.find(dataTableId);
        } else {
            Set<DataTable> dataTables = getDataResource().getDataTables();
            if (!CollectionUtils.isEmpty(dataTables)) {
                dataTable = dataTables.iterator().next();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), Permissions.MODIFY_METADATA);
    }

    @Override
    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public Long getNextColumnId() {
        return nextColumnId;
    }

    public void setNextColumnId(Long nextColumnId) {
        this.nextColumnId = nextColumnId;
    };

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
        return dataTable;
    }

    public void setDataTableId(Long dataTableId) {
        this.dataTableId = dataTableId;
    }

    public List<MeasurementUnit> getAllMeasurementUnits() {
        return Arrays.asList(MeasurementUnit.values());
    }

    public List<DataTableColumnEncodingType> getAllColumnEncodingTypes() {
        return Arrays.asList(DataTableColumnEncodingType.values());
    }

    public Dataset getDataset() {
        return persistable;
    }

    public Dataset getResource() {
        return persistable;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

    public List<CategoryVariable> getAllDomainCategories() {
        if (allDomainCategories == null) {
            allDomainCategories = categoryVariableService.findAllCategoriesSorted();
        }
        return allDomainCategories;
    }

    @Override
    public boolean isRightSidebar() {
        return true;
    }

    public String getTableDescription() {
        return tableDescription;
    }

    public void setTableDescription(String tableDescription) {
        this.tableDescription = tableDescription;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the startTime
     */
    public Long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
