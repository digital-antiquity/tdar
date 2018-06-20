package org.tdar.db;

import java.util.Collection;
import java.util.Map;

import org.tdar.datatable.DataTableColumnType;
import org.tdar.datatable.ImportColumn;
import org.tdar.datatable.ImportTable;

public interface ImportDatabase {

    int getMaxTableLength();

    String normalizeTableOrColumnNames(String name);

    int getMaxColumnNameLength();

    void closePreparedStatements(Collection<ImportTable> dataTables) throws Exception;

    void alterTableColumnType(String name, ImportColumn column, DataTableColumnType type, int length);

    void dropTable(ImportTable dataTable);

    void createTable(ImportTable dataTable) throws Exception;

    <T extends ImportColumn> void addTableRow(ImportTable<T> dataTable, Map<? extends ImportColumn, String> valueColumnMap) throws Exception;

}
