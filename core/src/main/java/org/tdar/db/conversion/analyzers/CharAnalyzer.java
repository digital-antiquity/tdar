package org.tdar.db.conversion.analyzers;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.exception.TdarRecoverableRuntimeException;

public class CharAnalyzer implements ColumnAnalyzer {
    private static final String EXCEL_BAD_REGEX = "(.*)(#(REF|NUM|N/A|VALUE|NAME|DIV))(.*)";
    private int len = 0;

    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.VARCHAR;
    }

    /**
     * Analyze the String for length and validity
     */
    @Override
    public boolean analyze(String value, DataTableColumn column, int rowNumber) {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        if (value.matches(EXCEL_BAD_REGEX)) {
            String columnName = "unknown";
            String tableName = "unknown";
            if (StringUtils.isNotBlank(column.getDisplayName())) {
                columnName = column.getDisplayName();
            }
            DataTable table = column.getDataTable();
            if ((table != null) && StringUtils.isNotBlank(table.getDisplayName())) {
                tableName = table.getDisplayName();
            }
            throw new TdarRecoverableRuntimeException("charAnalyzer.excel_data_error", Arrays.asList(rowNumber, columnName, tableName));
        }
        if (value.length() > len) {
            len = value.length();
        }
        return true;
    }

    /**
     * get the maximum length exposed -- mapped to the SQL length of the varchar
     */
    @Override
    public int getLength() {
        return len;
    }
}