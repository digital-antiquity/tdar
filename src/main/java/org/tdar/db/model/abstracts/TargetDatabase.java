package org.tdar.db.model.abstracts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.struts.data.IntegrationColumn;

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
    static final String TDAR_ID_COLUMN = "id_row_tdar";

    String normalizeTableOrColumnNames(String input);

    void closePreparedStatements(Collection<DataTable> dataTables) throws Exception;

    String getFullyQualifiedTableName(String tableName);

    void dropTable(String tableName);

    void dropTable(DataTable dataTable);

    void createTable(DataTable dataTable) throws Exception;

    <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse);

    void addTableRow(DataTable dataTable, Map<DataTableColumn, String> valueColumnMap) throws Exception;

    List<String> selectNonNullDistinctValues(DataTableColumn column);

    /**
     * @param dataType
     * @return
     */
    String toImplementedTypeDeclaration(DataTableColumnType dataType, int precision);

    @Deprecated
    <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    <T> T selectAllFromTableInImportOrder(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor);

    Map<String, Long> selectDistinctValuesWithCounts(DataTableColumn dataTableColumn);

    String generateOntologyEnhancedSelect(DataTable table, List<IntegrationColumn> integrationColumns,
            Map<List<OntologyNode>, Map<DataTable, Integer>> pivot);

    // List<String[]> query(String selectSql, ParameterizedRowMapper<String[]> parameterizedRowMapper);

    String getResultSetValueAsString(ResultSet resultSet, int resultSetPosition, DataTableColumn column) throws SQLException;

    List<String> selectDistinctValues(DataTableColumn column);

    List<String[]> query(String selectSql, ParameterizedRowMapper<String[]> parameterizedRowMapper);

    List<List<String>> selectAllFromTable(DataTable dataTable, ResultSetExtractor<List<List<String>>> resultSetExtractor, boolean includeGenerated,
            String query);

    <T> T selectRowFromTable(DataTable dataTable, ResultSetExtractor<T> resultSetExtractor, Long rowId);

    String selectTableAsXml(DataTable dataTable);

    int getMaxColumnNameLength();

}
