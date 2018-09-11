package org.tdar.db.conversion.analyzers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.TDataTableColumn;
import org.tdar.exception.TdarRecoverableRuntimeException;

public class CharAnalyzer implements ColumnAnalyzer<String> {
    private static final String EXCEL_BAD_REGEX = "(.*)(#(REF|NUM|N/A|VALUE|NAME|DIV))(.*)";
    private int len = 0;
    private Map<String, Integer> values = new HashMap();

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
    public boolean analyze(String value, TDataTableColumn column, int rowNumber) {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        if (value.matches(EXCEL_BAD_REGEX)) {
            String columnName = "unknown";
            if (StringUtils.isNotBlank(column.getDisplayName())) {
                columnName = column.getDisplayName();
            }
//            DataTable table = column.getDataTable();
//            if ((table != null) && StringUtils.isNotBlank(table.getDisplayName())) {
//                tableName = table.getDisplayName();
//            }
            throw new TdarRecoverableRuntimeException("charAnalyzer.excel_data_error", Arrays.asList(rowNumber, columnName));
        }
        if (value.length() > len) {
            len = value.length();
        }
        Integer def = values.getOrDefault(StringUtils.substring(value, 0,255), 0);
        values.put(value, def + 1);
        return true;
    }

    /**
     * get the maximum length exposed -- mapped to the SQL length of the varchar
     */
    @Override
    public int getLength() {
        return len;
    }
    
    @Override
    public Map<String,Integer> getValues() {
        return values;
    }
}