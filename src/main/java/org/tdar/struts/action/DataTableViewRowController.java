package org.tdar.struts.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
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
    private Long dataTableId;
    private Long rowId;
    private Dataset dataset;
    private Map<DataTableColumn, String> dataTableRowAsMap;

    /**
     * Used to render a row within a {@link Dataset}.
     * The expected URL is of the form /datatable/view-row?dataTableId=5815&rowId=1 where dataTableId = data table id, and rowId is the tDAR row id within 
     * the table.
     * 
     * @return com.opensymphony.xwork2.SUCCESS if able to find and display the table, com.opensymphony.xwork2.ERROR if not.
     */
    @Action(value = "view-row")
    public String getDataResultsRow() {
        if (!isViewRowSupported()) {
            return ERROR;
        }
        dataTableRowAsMap = new HashMap<>();
        dataset = null;
        if (Persistable.Base.isNullOrTransient(dataTableId)) {
            return ERROR;
        }
        DataTable dataTable = getDataTableService().find(dataTableId);
        if (dataTable != null) {
            dataset = dataTable.getDataset();
            if (getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), dataset)) {
                dataTableRowAsMap = getDatasetService().selectRowFromDataTable(dataTable, rowId, true);
                return SUCCESS;
            }
        }
        return ERROR;
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
     * @return the data set that rows are being returned from
     */
    public Dataset getDataset() {
        return dataset;
    }

     public Long getDataTableId() {
        return dataTableId;
    }

    public void setDataTableId(Long id) {
        this.dataTableId = id;
    }

}
