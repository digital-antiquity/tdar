package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.dataTable.DataTable;

/**
 * $Id$
 * 
 * A result object for the integrated data. Specific to a single dataset.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class IntegrationDataResult implements Serializable {

    private static final long serialVersionUID = 4923542129590685653L;

    private DataTable dataTable;
    private List<List<String>> rowData = new ArrayList<List<String>>();
    private List<IntegrationColumn> integrationColumns;

    public List<List<String>> getRowData() {
        return rowData;
    }

    public void setRowData(List<List<String>> rowData) {
        this.rowData = rowData;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void setIntegrationColumns(List<IntegrationColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    public List<IntegrationColumn> getIntegrationColumns() {
        return integrationColumns;
    }
}
