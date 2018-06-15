package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.datatable.DataTableColumnType;

public interface ColumnAnalyzer {

    /**
     * @return true if the value argument could be written to a database as the analysers DataTableColumn type,
     *         false otherwise.
     */
    boolean analyze(String value, DataTableColumn column, int rowNumber);

    /**
     * @return The DataTableColumn/SQL type the analyser is trying to detect
     */
    DataTableColumnType getType();

    /**
     * @return The maximum length found, if the SQL type has a variable length, 0 otherwise
     */
    int getLength();
}
