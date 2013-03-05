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

    public static final String TDAR_ID_COLUMN = "id_row_tdar";

    public String normalizeTableOrColumnNames(String input);

    public void closePreparedStatements(Collection<DataTable> dataTables) throws Exception;

    public String getFullyQualifiedTableName(String tableName);

    public void dropTable(String tableName);

    public void dropTable(DataTable dataTable);

    public void createTable(DataTable dataTable) throws Exception;

    public <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse);

    public void addTableRow(DataTable dataTable, Map<DataTableColumn, String> valueColumnMap) throws Exception;

    public List<String> selectNonNullDistinctValues(DataTableColumn column);

    /**
     * @param dataType
     * @return
     */
    public String toImplementedTypeDeclaration(DataTableColumnType dataType, int precision);

    @Deprecated
    public <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    public <T> T selectAllFromTableInImportOrder(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues);

    public <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor);

    public Map<String, Long> selectDistinctValuesWithCounts(DataTableColumn dataTableColumn);

    public String generateOntologyEnhancedSelect(DataTable table, List<IntegrationColumn> integrationColumns,
            Map<List<OntologyNode>, Map<DataTable, Integer>> pivot);

//    public List<String[]> query(String selectSql, ParameterizedRowMapper<String[]> parameterizedRowMapper);

    public String getResultSetValueAsString(ResultSet resultSet, int resultSetPosition, DataTableColumn column) throws SQLException;

    public List<String> selectDistinctValues(DataTableColumn column);

    public List<String[]> query(String selectSql, ParameterizedRowMapper<String[]> parameterizedRowMapper);

    public List<List<String>> selectAllFromTable(DataTable dataTable, ResultSetExtractor<List<List<String>>> resultSetExtractor, boolean includeGenerated,  String query);

}
