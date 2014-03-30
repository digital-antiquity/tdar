package org.tdar.core.service.resource.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.db.model.abstracts.TargetDatabase;

/*
 * A static utility class for the DatasetService
 */
public class DatasetUtils {

    private static final String NULL = "NULL";

    /*
     * Converts a JDBC @link ResultSet row into a Map of @link DataTableColumn (key) and String (value).
     */
    public static Map<DataTableColumn, String> convertResultSetRowToDataTableColumnMap(final DataTable table, ResultSet rs, boolean returnRowId) throws SQLException {
        Map<DataTableColumn, String> results = new LinkedHashMap<>();
        if (returnRowId) {
            // we want this to be the very first entry in the linked hash map
            results.put(table.getColumnByName(TargetDatabase.TDAR_ID_COLUMN), null);
        }
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            DataTableColumn col = table.getColumnByName(rs.getMetaData().getColumnName(i));
            // ignore if null (non translated version of translated)
            if (col != null && col.isVisible()) {
                results.put(col, null);
            }
        }
        for (DataTableColumn key : results.keySet()) {
            String val = NULL;
            Object obj = rs.getObject(key.getName());
            if (obj != null) {
                val = obj.toString();
            }
            results.put(key, val);
        }
        return results;
    }

}
