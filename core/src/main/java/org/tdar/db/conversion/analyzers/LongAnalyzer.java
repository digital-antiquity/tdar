package org.tdar.db.conversion.analyzers;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;

/**
 * Determines whether the column contains a Long value
 * 
 * @author abrin
 * 
 */
public class LongAnalyzer implements ColumnAnalyzer {

    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.BIGINT;
    }

    /**
     * Analyze if the String is a Long
     */
    @Override
    public boolean analyze(String value, DataTableColumn column, int row) {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        try {
            Long.parseLong(value);
        } catch (NumberFormatException nfx) {
            return false;
        }
        String lastChar = value.substring(value.length() - 1);
        // handles cases like "1D" which Double.parseDouble handles but cannot be casted to double in postgres
        if (StringUtils.isAlpha(lastChar)) {
            return false;
        }
        return true;
    }

    /**
     * Return the SQL declared length (for a long this is always 0
     * 
     */
    @Override
    public int getLength() {
        return 0;
    }
}