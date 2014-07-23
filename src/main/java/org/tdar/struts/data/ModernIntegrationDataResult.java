package org.tdar.struts.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.List;

import org.tdar.core.bean.resource.datatable.DataTable;

/**
 * $Id$
 * 
 * A result object for the integrated data. Specific to a single dataset.
 * 
 * @author <a href='mailto:Adam.Brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
public class ModernIntegrationDataResult implements Serializable {

    private static final long serialVersionUID = 3466986630097086581L;
    private List<DataTable> dataTables;

    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    private List<IntegrationColumn> integrationColumns;
    private ResultSet resultSet;

    public void setIntegrationColumns(List<IntegrationColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    public List<IntegrationColumn> getIntegrationColumns() {
        return integrationColumns;
    }
}
