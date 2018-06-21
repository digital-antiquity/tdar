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
import org.tdar.db.ImportDatabase;

/**
 * A base class for target Databases that can be written to via a
 * DatabaseConverter.
 * 
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface TargetDatabase extends Database , ImportDatabase {

    /**
     * Returns a table name consistent with this target database's allowable
     * table names.
     */

    String getFullyQualifiedTableName(String tableName);

    void dropTable(String tableName);

    List<String> selectNonNullDistinctValues(ImportTable table, ImportColumn column, boolean useUntranslatedValues);

    /**
     * @param dataType
     * @return
     */
    String toImplementedTypeDeclaration(DataTableColumnType dataType, int precision);

    @Deprecated
    <T> T selectAllFromTable(ImportTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    @Deprecated
    <T> T selectAllFromTable(ImportTable table, ResultSetExtractor<T> resultSetExtractor, String... orderBy);

    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTableInImportOrder(ImportTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    @Transactional(value = "tdarDataTx", readOnly = true)
    <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor);

    @Transactional(value = "tdarDataTx", readOnly = true)
    Map<String, Long> selectDistinctValuesWithCounts(ImportTable table, ImportColumn dataTableColumn);

    @Transactional(value = "tdarDataTx", readOnly = true)
    List<String> selectDistinctValues(ImportTable table, ImportColumn column, boolean sort);

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
