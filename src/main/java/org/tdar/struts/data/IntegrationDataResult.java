package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

/**
 * $Id$
 * 
 * A result object for the integrated data.  Specific to a single dataset.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class IntegrationDataResult implements Serializable {
    
    private static final long serialVersionUID = 4923542129590685653L;

    private DataTable dataTable;
    
    // both integration columns
    private List<DataTableColumn> integrationColumns = new ArrayList<DataTableColumn>();
    private List<DataTableColumn> columnsToDisplay = new ArrayList<DataTableColumn>();
    
    private List<IntegrationRowData> rowData = new ArrayList<IntegrationRowData>();

    public List<IntegrationRowData> getRowData() {
        return rowData;
    }
    
    public List<DataTableColumn> getIntegrationColumns() {
        return integrationColumns;
    }

    public void setIntegrationColumns(List<DataTableColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    public void setRowData(List<IntegrationRowData> rowData) {
        this.rowData = rowData;
    }

    public List<DataTableColumn> getColumnsToDisplay() {
        return columnsToDisplay;
    }

    public void setColumnsToDisplay(List<DataTableColumn> columnsToDisplay) {
        this.columnsToDisplay = columnsToDisplay;
    }


    public DataTable getDataTable() {
        return dataTable;
    }


    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }
    
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(" table ").append(getDataTable().getDisplayName()).append(" using integration columns ");
        Iterator<DataTableColumn> iterator = getIntegrationColumns().iterator();
        while (iterator.hasNext()) {
            DataTableColumn col = iterator.next();
            builder.append(col.getDisplayName());
            if (iterator.hasNext())
                builder.append(", ");
        }
        builder.append(" with display columns ");
        iterator = getColumnsToDisplay().iterator();
        while (iterator.hasNext()) {
            DataTableColumn col = iterator.next();
            builder.append(col.getDisplayName());
            if (iterator.hasNext())
                builder.append(", ");
        }
        return builder.toString();
    }
}
