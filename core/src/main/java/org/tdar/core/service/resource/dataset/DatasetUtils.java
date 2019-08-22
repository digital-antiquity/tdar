package org.tdar.core.service.resource.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.datatable.ColumnVisibility;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

/*
 * A static utility class for the DatasetService
 */
public class DatasetUtils {

//    private static final String NULL = "NULL";
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(DatasetUtils.class);

    /*
     * Converts a JDBC @link ResultSet row into a Map of @link DataTableColumn (key) and String (value).
     */
    public static Map<DataTableColumn, String> convertResultSetRowToDataTableColumnMap(final DataTable table, boolean canSeeConfidential, ResultSet rs, boolean returnRowId)
            throws SQLException {
        Map<DataTableColumn, String> results = new LinkedHashMap<>();
        if (returnRowId) {
            // we want this to be the very first entry in the linked hash map
            results.put(table.getColumnByName(DataTableColumn.TDAR_ID_COLUMN), null);
        }
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            DataTableColumn col = table.getColumnByName(columnName);
            // ignore if null (non translated version of translated)
            
            if (col == null) {
                continue;
            }
            if (col.getVisible() == null) {
                col.setVisible(ColumnVisibility.VISIBLE);
            }
            switch (col.getVisible()) {
                case HIDDEN:
                    continue;
                case CONFIDENTIAL:
                    if (canSeeConfidential) {
                        results.put(col, null);
                    }
                    break;
                case VISIBLE:
                default:
                    results.put(col, null);
            }
        }
        for (DataTableColumn key : results.keySet()) {
            String val;
            Object obj = rs.getObject(key.getName());
            if (obj != null) {
                val = obj.toString();
                results.put(key, val);
            }
        }
        return results;
    }

}
