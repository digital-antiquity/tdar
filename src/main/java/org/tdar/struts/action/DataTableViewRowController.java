package org.tdar.struts.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.struts.action.AuthenticationAware.Base;

@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/datatable")
public class DataTableViewRowController extends Base {

    private static final long serialVersionUID = 9050899110159727873L;
    private Long id;
    private Long rowId;
    private String datasetName;
    private String datasetDescription;
    private Map<DataTableColumn, String> dataTableRowAsMap;

    /**
     * Used to render a row within a {@link Dataset}.
     * The expected URL is of the form /datatable/view-row?id=5815&rowId=1 where id = data table id, and rowId is the tDAR row id within the table.
     * Note that the method will simply set up empty fields if the user doesn't have permission to view the page....
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find the table, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "view-row",
            results = { @Result(name = "success", location = "view-row.ftl", type = "freemarker") })
    public String getDataResultsRow() {
        if (!isViewRowSupported()) {
            return ERROR;
        }
            
        dataTableRowAsMap = new HashMap<>();
        datasetName = "";
        datasetDescription = "";
        if (Persistable.Base.isNullOrTransient(id)) {
            return ERROR;
        }
        DataTable dataTable = getDataTableService().find(id);
        if (dataTable == null) {
            return ERROR;
        }
        Dataset dataset = dataTable.getDataset();
        if (userCanView(dataset)) {
            datasetDescription = dataset.getDescription();
            datasetName = dataset.getName();
            dataTableRowAsMap = getDatasetService().selectRowFromDataTable(dataTable, rowId, true);
        }
        return SUCCESS;
    }

    private boolean userCanView(Dataset dataset) {
        return dataset.isPublicallyAccessible() || getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), dataset);
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

    /**
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * @return the datasetDescription
     */
    public String getDatasetDescription() {
        return datasetDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
