package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;

public interface ColumnAnalyzer {

    /**
     * @return true if the value argument could be written to a database as the analysers DataTableColumn type,
     *         false otherwise.
     */
    public boolean analyze(String value);

    /**
     * @return The DataTableColumn/SQL type the analyser is trying to detect
     */
    public DataTableColumnType getType();

    /**
     * @return The maximum length found, if the SQL type has a variable length, 0 otherwise
     */
    public int getLength();
}
