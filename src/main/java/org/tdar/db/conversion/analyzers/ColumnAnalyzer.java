package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;

public interface ColumnAnalyzer {
    public boolean analyze(String value);
    public DataTableColumnType getType();
    public int getLength();
}
