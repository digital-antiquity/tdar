package org.tdar.db.model.abstracts;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.datatable.ImportColumn;
import org.tdar.datatable.ImportTable;

/**
 * A base class for target Databases that can be written to via a
 * DatabaseConverter.
 * 
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface TargetDatabase extends Database {

    /**
     * Returns a table name consistent with this target database's allowable
     * table names.
     */

    String normalizeTableOrColumnNames(String input);

    @Transactional(value = "tdarDataTx", readOnly = false)
    void closePreparedStatements(Collection<ImportTable> dataTables) throws Exception;

    String getFullyQualifiedTableName(String tableName);

    @Transactional(value = "tdarDataTx", readOnly = false)
    void dropTable(String tableName);

    @Transactional(value = "tdarDataTx", readOnly = false)
    void dropTable(ImportTable dataTable);

    @Transactional(value = "tdarDataTx", readOnly = false)
    void createTable(ImportTable dataTable) throws Exception;

    @Transactional(value = "tdarDataTx", readOnly = false)
    <T extends ImportColumn> void addTableRow(ImportTable<T> dataTable, Map<? extends ImportColumn, String> valueColumnMap) throws Exception;

    @Transactional(value = "tdarDataTx", readOnly = true)
    List<String> selectNonNullDistinctValues(DataTableColumn column, boolean useUntranslatedValues);

    /**
     * @param dataType
     * @return
     */
    @Transactional(value = "tdarDataTx", readOnly = true)
    String toImplementedTypeDeclaration(DataTableColumnType dataType, int precision);

    @Deprecated
    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    @Deprecated
    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, String... orderBy);

    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTableInImportOrder(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor);

    @Transactional(value = "tdarDataTx", readOnly = true)
    Map<String, Long> selectDistinctValuesWithCounts(DataTableColumn dataTableColumn);

    @Transactional(value = "tdarDataTx", readOnly = true)
    List<String> selectDistinctValues(DataTableColumn column, boolean sort);

    @Transactional(value = "tdarDataTx", readOnly = true)
    List<List<String>> selectAllFromTable(DataTable dataTable, ResultSetExtractor<List<List<String>>> resultSetExtractor, boolean includeGenerated,
            String query);

    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectRowFromTable(DataTable dataTable, ResultSetExtractor<T> resultSetExtractor, Long rowId);

    @Transactional(value = "tdarDataTx", readOnly = true)
    String selectTableAsXml(DataTable dataTable);

    int getMaxColumnNameLength();

    @Transactional(value = "tdarDataTx", readOnly = false)
    Map<DataTableColumn, String> selectAllFromTableCaseInsensitive(DataTableColumn column, String key,
            ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor);

    @Transactional(value = "tdarDataTx", readOnly = true)
    boolean checkTableExists(ImportTable dataTable);

}
