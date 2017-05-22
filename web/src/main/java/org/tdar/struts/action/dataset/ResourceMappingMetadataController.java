package org.tdar.struts.action.dataset;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.DatasetMappingService;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/dataset")
@HttpsOnly
public class ResourceMappingMetadataController extends AbstractAuthenticatableAction implements Preparable, PersistableLoadingAction<Dataset> {

    private static final long serialVersionUID = 1252800268993263663L;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient DatasetMappingService datasetMappingService;
    @Autowired
    private transient DatasetService datasetService;

    @Autowired
    private transient DataTableService dataTableService;

    private Dataset persistable;

    private Dataset getDataResource() {
        return getPersistable();
    }
    private Long startTime = -1L;
    private Long id;
    private List<DataTableColumn> columnsToRemap;
    private DataTable dataTable;
    private List<DataTableColumn> dataTableColumns;
    private boolean async = true;
    private Long dataTableId;
    // stores the next data table column's ontology mappings to visit within this dataset when mapping data column values to ontology nodes.
    // i.e., a cursor into ontologyMappedColumns
    private Long nextColumnId;

    @SkipValidation
    @Action(value = "resource-mapping", results = { @Result(name = SUCCESS, location = "../dataset/column-resource-mapping.ftl") })
    public String editColumnMetadata() throws TdarActionException {

        if (getDataResource().getLatestVersions().isEmpty()) {
            addActionError(getText("abstractDatasetController.upload_data_file_first"));
            return INPUT;
        }

        if (CollectionUtils.isEmpty(getDataResource().getDataTables())) {
            addActionError(getText("abstractDatasetController.no_tables"));
            return INPUT;
        }
        setDataTableColumns(getDataTable().getFilenameColumns());

        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @PostOnly
    @Action(value = "save-column-mapping",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/${resource.resourceType.urlNamespace}/${resource.id}"),
                    @Result(name = INPUT, location = "../dataset/column-resource-mapping.ftl")
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
        // checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        try {
            columnsToRemap = datasetService.updateColumnResourceMappingMetadata(this, getDataResource(), getDataTable(), getDataTableColumns(),
                    getAuthenticatedUser(), getStartTime());
        } catch (Throwable tde) {
            getLogger().error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }
        postSaveColumnMetadataCleanup();
        return SUCCESS;
    }

    public Dataset getPersistable() {
        return persistable;
    }

    public void setPersistable(Dataset persistable) {
        this.persistable = persistable;
    }

    public List<DataTableColumn> getColumnsToRemap() {
        return columnsToRemap;
    }

    public void setColumnsToRemap(List<DataTableColumn> columnsToRemap) {
        this.columnsToRemap = columnsToRemap;
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

    protected void postSaveColumnMetadataCleanup() {
        if (CollectionUtils.isNotEmpty(columnsToRemap)) {
            if (isAsync()) {
                datasetMappingService.remapColumnsAsync(columnsToRemap, getDataResource().getProject());
            } else {
                datasetMappingService.remapColumns(columnsToRemap, getDataResource().getProject());
            }
        }
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
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
        if (PersistableUtils.isNullOrTransient(getResource().getProject()) || Project.NULL.equals(getResource().getProject())) {
            addActionError(getText("resourceMappingMetadataController.requires_project"));
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
        return authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_METADATA);
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
