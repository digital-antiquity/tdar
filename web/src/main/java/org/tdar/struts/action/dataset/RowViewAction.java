package org.tdar.struts.action.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value = {
        @Namespace("/dataset/row"),
        @Namespace("/geospatial/row"),
        @Namespace("/sensory-data/row")
})
public class RowViewAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -4346591781165839553L;
    @Autowired
    private transient DatasetService datasetService;
    @Autowired
    private transient DataTableService dataTableService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private Long id;
    private Long dataTableId;
    private DataTable dataTable;
    private Dataset persistable;
    private Long rowId;
    private Map<DataTableColumn, String> dataTableRowAsMap = new HashMap<>();

    /**
     * Used to render a row within a {@link Dataset}.
     * The expected URL is of the form /datatable/row/datasetId/dataTableId/rowId where dataTableId = data table id, and rowId is the tDAR row id within
     * the table.
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find and display the row, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "{id}/{dataTableId}/{rowId}", results = {
            @Result(name = SUCCESS, location = "../../dataset/view-row.ftl") })
    @SkipValidation
    public String getDataResultsRow() {
        if (!getTdarConfiguration().isViewRowSupported()) {
            return ERROR;
        }
        setTransientViewableStatus(getResource(), getAuthenticatedUser());
        if (PersistableUtils.isNullOrTransient(dataTableId) || PersistableUtils.isNullOrTransient(rowId)) {
            return ERROR;
        }
        if (dataTable != null) {
            if (authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getResource())) {
                dataTableRowAsMap = datasetService.selectRowFromDataTable(dataTable, rowId, true);
                if (MapUtils.isEmpty(dataTableRowAsMap)) {
                    return ERROR;
                }
                return SUCCESS;
            }
        }
        return ERROR;
    }

    @Override
    public void prepare() throws Exception {
        persistable = datasetService.find(getId());
        if (dataTableId != null) {
            this.dataTable = dataTableService.find(dataTableId);
        } else {
            Set<DataTable> dataTables = persistable.getDataTables();
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

    public Long getDataTableId() {
        return dataTableId;
    }

    public void setDataTableId(Long dataTableId) {
        this.dataTableId = dataTableId;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public Dataset getPersistable() {
        return persistable;
    }

    public void setPersistable(Dataset persistable) {
        this.persistable = persistable;
    }

    public Dataset getDataset() {
        return persistable;
    }

    public Dataset getResource() {
        return persistable;
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

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
    }
}
