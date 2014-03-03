package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

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
    public boolean analyze(String value) {
        if (value == null)
            return true;
        if ("".equals(value))
            return true;
        if (value.matches(EXCEL_BAD_REGEX)) {
            throw new TdarRecoverableRuntimeException("charAnalyzer.excel_data_error");
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