package org.tdar.db.conversion.analyzers;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.datatable.DataTableColumnType;

public class DoubleAnalyzer implements ColumnAnalyzer {

    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.DOUBLE;
    }

    /**
     * Analyze whether the String can be mapped to a Double
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
            Double.parseDouble(value);
        } catch (NumberFormatException nfx) {
            return false;
        }

        String lastChar = value.substring(value.length() - 1);
        // handles cases like "1F" which Double.parseDouble handles but cannot be casted to double in postgres
        if (StringUtils.isAlpha(lastChar)) {
            return false;
        }

        return true;
    }

    /**
     * For a double, always 0
     */
    @Override
    public int getLength() {
        return 0;
    }
}