package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;

public class LongAnalyzer implements ColumnAnalyzer {

    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.BIGINT;
    }

    @Override
    public boolean analyze(String value) {
        if (value == null)
            return true;
        if ("".equals(value))
            return true;
        try {
            Long.parseLong(value);
        } catch (NumberFormatException nfx) {
            return false;
        }
        return true;
    }

    @Override
    public int getLength() {
        return 0;
    }
}