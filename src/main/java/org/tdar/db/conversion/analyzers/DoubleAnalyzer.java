package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;

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
    public boolean analyze(String value) {
        if (value == null)
            return true;
        if ("".equals(value))
            return true;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfx) {
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