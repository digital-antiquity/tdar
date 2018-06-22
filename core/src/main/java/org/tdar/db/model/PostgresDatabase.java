package org.tdar.db.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.db.builder.SqlSelectBuilder;
import org.tdar.db.builder.SqlTools;
import org.tdar.db.builder.WhereCondition;
import org.tdar.db.builder.WhereCondition.Condition;
import org.tdar.db.builder.WhereCondition.ValueCondition;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.db.postgres.PostgresImportDatabase;

/**
 * $Id$
 * 
 * Used to interact with the data import postgresql database.
 * 
 * FIXME: switch to SimpleJdbcTemplate, use PreparedStatement api when possible (e.g. all values)
 * 
 * @version $Revision$
 */
@Component(value = "target")
public class PostgresDatabase extends PostgresImportDatabase implements TargetDatabase {

    private static final String SELECT_ROW_COUNT = "SELECT COUNT(0) FROM %s";
    private static final String SELECT_ALL_FROM_TABLE_WHERE = "SELECT * FROM \"%s\" WHERE \"%s\"=?";
    private static final String SELECT_ALL_FROM_TABLE_WHERE_LOWER = "SELECT * FROM \"%s\" WHERE lower(\"%s\")=lower(?)";
    private static final String ALTER_DROP_COLUMN = "ALTER TABLE \"%s\" DROP COLUMN \"%s\"";
    private static final String UPDATE_UNMAPPED_CODING_SHEET = "UPDATE \"%s\" SET \"%s\"='" + NO_CODING_SHEET_VALUE + " ' || \"%s\" WHERE \"%s\" IS NULL";
    private static final String UPDATE_COLUMN_SET_VALUE_TRIM = "UPDATE \"%s\" SET \"%s\"=? WHERE trim(\"%s\")=?";
    private static final String UPDATE_COLUMN_SET_VALUE = "UPDATE \"%s\" SET \"%s\"=? WHERE \"%s\"=?";
    public static final String ADD_COLUMN = "ALTER TABLE \"%s\" ADD COLUMN \"%s\" character varying";
    public static final String ADD_NUMERIC_COLUMN = "ALTER TABLE \"%s\" ADD COLUMN \"%s\" bigint";
    private static final String RENAME_COLUMN = "ALTER TABLE \"%s\" RENAME COLUMN \"%s\" TO \"%s\"";
    private static final String UPDATE_COLUMN_TO_NULL = "UPDATE \"%s\" SET \"%s\"=NULL";
    private static final String ORIGINAL_KEY = "__o";

    private final Logger logger = LoggerFactory.getLogger(getClass());


    private JdbcTemplate jdbcTemplate;



    @Transactional(value = "tdarDataTx", readOnly = false)
    public void createTable(final String createTableStatement) {
        logger.debug(createTableStatement);
        jdbcTemplate.execute(createTableStatement);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(value = "tdarDataTx", readOnly = true)
    public List query(String sql, RowMapper rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) {
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    @Deprecated
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTable(ImportTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = getSelectAll(table, includeGeneratedValues);
        return jdbcTemplate.query(builder.toSql(), resultSetExtractor);
    }

    @Override
    @Deprecated
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTable(ImportTable table, ResultSetExtractor<T> resultSetExtractor, String... orderBy) {
        SqlSelectBuilder builder = getSelectAll(table, false);
        builder.getOrderBy().addAll(Arrays.asList(orderBy));
        return jdbcTemplate.query(new LowMemoryStatementCreator(builder.toSql()), resultSetExtractor);
    }

    private SqlSelectBuilder getSelectAll(ImportTable table, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = new SqlSelectBuilder();
        if (includeGeneratedValues) {
            builder.getColumns().add(DataTableColumn.TDAR_ROW_ID.getName());
        }
        List<ImportColumn> columns = table.getDataTableColumns();
        columns.sort(new Comparator<ImportColumn>() {

            @Override
            public int compare(ImportColumn o1, ImportColumn o2) {
                Integer c1 = 0;
                Integer c2 = 0;
                if (o1.getImportOrder() != null) {
                    c1 = o1.getImportOrder();
                }
                if (o2.getImportOrder() != null) {
                    c2 = o2.getImportOrder();
                }
                return c1.compareTo(c2);
            }

        });
        for (ImportColumn dtc : columns) {
            builder.getColumns().add(dtc.getName());

            if (dtc instanceof DataTableColumn) {
                DataTableColumn col = (DataTableColumn) dtc;
                if (col.getDefaultCodingSheet() != null && includeGeneratedValues) {
                    builder.getColumns().add(generateOriginalColumnName(col));
                }
            }
        }
        builder.getTableNames().add(table.getName());
        return builder;
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTableInImportOrder(ImportTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = getSelectAll(table, includeGeneratedValues);
        builder.getOrderBy().add(DataTableColumn.TDAR_ROW_ID.getName());
        LowMemoryStatementCreator lmsc = new LowMemoryStatementCreator(builder.toSql());
        return jdbcTemplate.query(lmsc, resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<String> selectDistinctValues(ImportTable table , ImportColumn dataTableColumn, boolean sort) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        boolean groupByAsDistinct = true;
        // trying out http://stackoverflow.com/a/6598931/667818
        if (groupByAsDistinct) {
            builder.setDistinct(true);
        } else {
            builder.getGroupBy().add(dataTableColumn.getName());
        }
        builder.getColumns().add(dataTableColumn.getName());
        builder.getTableNames().add(table.getName());
        if (sort) {
            builder.getOrderBy().add(dataTableColumn.getName());
        }
        logger.trace(builder.toSql());
        return jdbcTemplate.queryForList(builder.toSql(), String.class);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public Map<String, Long> selectDistinctValuesWithCounts(ImportTable table, ImportColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyMap();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.setDistinct(true);
        builder.setCountColumn(dataTableColumn.getName());
        builder.getGroupBy().add(dataTableColumn.getName());
        builder.getColumns().add(dataTableColumn.getName());
        builder.getTableNames().add(table.getName());
        builder.getOrderBy().add(dataTableColumn.getName());

        final Map<String, Long> toReturn = new HashMap<String, Long>();
        query(builder.toSql(), new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                toReturn.put(rs.getString(0), rs.getLong(1));
                return null;
            }
        });
        return toReturn;
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<String> selectNonNullDistinctValues(ImportTable table, ImportColumn dataTableColumn, boolean useUntranslatedValues) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.getTableNames().add(table.getName());
        builder.setDistinct(true);
        String name = dataTableColumn.getName();
        if (useUntranslatedValues) {
            String original = generateOriginalColumnName((DataTableColumn)dataTableColumn);
            if (hasColumn(table.getName(), original)) {
                name = original;
            }
        }
        builder.getOrderBy().add(name);
        builder.getColumns().add(name);
        WhereCondition notNull = new WhereCondition(name);
        notNull.setValueCondition(ValueCondition.NOT_EQUALS);
        WhereCondition notBlank = new WhereCondition(name);
        notBlank.setValueCondition(ValueCondition.NOT_EQUALS);
        notBlank.setCondition(Condition.AND);
        builder.getWhere().add(notNull);
        if (!dataTableColumn.getColumnDataType().isNumeric()) {
            builder.getWhere().add(notBlank);
        }

        return jdbcTemplate.queryForList(builder.toSql(), String.class);
    }


    // FIXME: allows for totally free form queries, refine this later?
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return jdbcTemplate.query(psc, pss, rse);
    }



    @Transactional(value = "tdarDataTx", readOnly = true)
    public boolean hasColumn(final String tableName, final String columnName) {
        logger.trace("Checking if " + tableName + " has column " + columnName);
        try {
            Boolean columnExists = (Boolean) JdbcUtils.extractDatabaseMetaData(
                    getDataSource(), new DatabaseMetaDataCallback() {
                        @Override
                        public Object processMetaData(DatabaseMetaData metadata)
                                throws SQLException, MetaDataAccessException {
                            ResultSet columnsResultSet = metadata.getColumns(null, null, tableName, columnName);
                            // if the column exists the result set should be a singleton so next() should be suitable
                            return columnsResultSet.next();
                        }
                    });
            return columnExists;
        } catch (MetaDataAccessException e) {
            logger.error("Couldn't access tdar data import database metadata", e);
        }
        return false;
    }



    private String generateOriginalColumnName(DataTableColumn column) {
        return column.getName() + ORIGINAL_KEY + column.getId();
    }

    /**
     * 
     * @param column
     * @param codingSheet
     */
    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void translateInPlace(final DataTableColumn column, final CodingSheet codingSheet) {
        DataTable dataTable = column.getDataTable();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        String tableName = dataTable.getName();
        String columnName = column.getName();
        final DataTableColumnType columnDataType = column.getColumnDataType();

        // Makes the assumption that there isn't already a column with the name
        // <column-to-be-translated>_original
        String originalColumnName = generateOriginalColumnName(column);
        // this isn't a good enough test to tell if we are translating for the first time. For instance, if we translate a column, and then set the coding sheet
        // to null again, and then translate again, the column will have a <column-name>_original copy but the default coding sheet will be null. Instead we
        // have to search for the <column-name>__o<id> column and make a somewhat dangerous assumption that if this column exists, then we are the ones
        // to have created it.
        if (hasColumn(tableName, originalColumnName)) {
            // this column has already been translated once. Wipe the current
            // translation and replace it with the new.
            String clearTranslatedColumnSql = String.format(UPDATE_COLUMN_TO_NULL, tableName, columnName);
            jdbcTemplate.execute(clearTranslatedColumnSql);
        } else {
            // FIXME: preserve original column order
            // if this column has never had a default coding sheet, rename column to column_original
            String renameColumnSql = String.format(RENAME_COLUMN, tableName, columnName, originalColumnName);
            jdbcTemplate.execute(renameColumnSql);
            // and then create the column again.
            String createColumnSql = String.format(ADD_COLUMN, tableName, columnName);
            jdbcTemplate.execute(createColumnSql);

        }

        String sql = UPDATE_COLUMN_SET_VALUE;
        switch (columnDataType) {
            case TEXT:
            case VARCHAR:
                sql = UPDATE_COLUMN_SET_VALUE_TRIM;
                break;
            default:
                break;
        }

        final String updateColumnSql = String.format(sql, tableName, columnName, originalColumnName);
        logger.debug("translating column from " + tableName + " (" + columnName + ")");
        PreparedStatementCreator translateColumnPreparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(updateColumnSql);
            }
        };
        PreparedStatementCallback<Object> translateColumnCallback = new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                Map<String, String> codeMap = createSanitizedKeyMap(codingSheet);

                for (String code : codeMap.keySet()) {
                    String term = codeMap.get(code);
                    // 1st parameter is the translated term that we want to set
                    preparedStatement.setString(1, term);
                    logger.trace("code: {} term: {} {}[{}]", code, term, columnDataType, updateColumnSql);
                    // 2nd parameter is the where condition, the code that we want to translate.
                    boolean okToExecute = false;
                    switch (columnDataType) {
                        case BIGINT:
                            try {
                                preparedStatement.setLong(2, Long.valueOf(code));
                                okToExecute = true;
                            } catch (Exception e) {
                                logger.debug("problem casting code {} to a Column's Long Mapping (ignoring)", code);
                            }
                            break;
                        case DOUBLE:
                            try {
                                preparedStatement.setDouble(2, Double.valueOf(code));
                                okToExecute = true;
                            } catch (Exception e) {
                                logger.debug("problem casting {} to a Double", code);
                            }
                            break;
                        case VARCHAR:
                        case TEXT:
                            preparedStatement.setString(2, code);
                            okToExecute = true;
                            break;
                        default:
                            break;
                    }
                    if (okToExecute) {
                        logger.trace("Prepared statement is: " + preparedStatement.toString());
                        preparedStatement.addBatch();
                    } else {
                        logger.debug("code: {} was not a valid type for {}", code, columnDataType);
                    }
                }
                return preparedStatement.executeBatch();
            }
        };
        // executes translation step
        jdbcTemplate.execute(translateColumnPreparedStatementCreator, translateColumnCallback);
        // the last step is to update all untranslated rows
        // SQL is essentially: update tableName set translatedColumnName='No
        // coding sheet value for code: ' || originalColumnName where
        // translatedColumnName is null
        String updateUntranslatedRows = String.format(UPDATE_UNMAPPED_CODING_SHEET, tableName, columnName, originalColumnName, columnName);
        // getLogger().debug("updating untranslated rows: " +
        // updateUntranslatedRows);
        jdbcTemplate.execute(updateUntranslatedRows);
    }

    /**
     * Takes a Coding Rule and tries to deal with appropriate permutations of padded integers
     * 
     * @param codingSheet
     * @return
     */
    private Map<String, String> createSanitizedKeyMap(final CodingSheet codingSheet) {
        Map<String, String> codeMap = new HashMap<>();
        for (CodingRule codingRule : codingSheet.getCodingRules()) {
            codeMap.put(codingRule.getCode(), codingRule.getTerm());
        }
        ;

        // handling issues of 01 vs. 1
        for (String code : new ArrayList<String>(codeMap.keySet())) {
            try {
                Integer integer = Integer.parseInt(code);
                String newCode = String.valueOf(integer);
                if (!codeMap.containsKey(newCode)) {
                    codeMap.put(newCode, codeMap.get(code));
                }
            } catch (NumberFormatException exception) {
            }
        }
        return codeMap;
    }

    @Override
    @Transactional(readOnly = false)
    public void untranslate(DataTableColumn column) {
        DataTable dataTable = column.getDataTable();
        String tableName = dataTable.getName();
        String originalName = generateOriginalColumnName(column);
        String translatedName = column.getName();
        if (!hasColumn(dataTable.getName(), originalName)) {
            return;
        }
        String sqlDrop = String.format(ALTER_DROP_COLUMN, tableName, translatedName);
        String sqlRename = String.format(RENAME_COLUMN, tableName, originalName, translatedName);
        logger.debug(sqlDrop);
        logger.debug(sqlRename);
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        jdbcTemplate.execute(sqlDrop);
        jdbcTemplate.execute(sqlRename);
    }

    @Transactional(value = "tdarDataTx", readOnly = false)
    public void executeUpdateOrDelete(final String createTableStatement) {
        logger.debug(createTableStatement);
        jdbcTemplate.execute(createTableStatement);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor) {
        return jdbcTemplate.query(String.format(SELECT_ALL_FROM_TABLE_WHERE, column.getDataTable().getName(), column.getName()),
                new String[] { key },
                resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public Map<DataTableColumn, String> selectAllFromTableCaseInsensitive(DataTableColumn column, String key,
            ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor) {
        return jdbcTemplate.query(String.format(SELECT_ALL_FROM_TABLE_WHERE_LOWER, column.getDataTable().getName(), column.getName()),
                new String[] { key }, resultSetExtractor);
    }

    @Transactional(value = "tdarDataTx", readOnly = false)
    public void renameColumn(DataTableColumn column, String newName) {
        logger.warn("RENAMING COLUMN " + column + " TO " + newName, new Exception("altering column should only be done by tests."));
        String sql = String.format(RENAME_COLUMN, column.getDataTable().getName(), column.getName(), newName);
        column.setName(newName);
        jdbcTemplate.execute(sql);
    }

    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<String> getColumnNames(ResultSet resultSet) throws SQLException {
        List<String> columnNames = new ArrayList<String>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        for (int columnIndex = 0; columnIndex < metadata.getColumnCount(); columnIndex++) {
            String columnName = metadata.getColumnName(columnIndex + 1);
            columnNames.add(columnName);
        }
        return columnNames;
    }

    @Transactional(value = "tdarDataTx", readOnly = true)
    public int getRowCount(ImportTable dataTable) {
        String sql = String.format(SELECT_ROW_COUNT, dataTable.getName());
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<String> selectAllFrom(final DataTableColumn column) {
        if (column == null) {
            return Collections.emptyList();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.getColumns().add(column.getName());
        builder.getTableNames().add(column.getDataTable().getName());
        return jdbcTemplate.queryForList(builder.toSql(), String.class);
    }


    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<List<String>> selectAllFromTable(ImportTable dataTable, ResultSetExtractor<List<List<String>>> resultSetExtractor, boolean includeGenerated,
            String query) {
        List<String> coalesce = new ArrayList<String>();
        List<ImportColumn> columns =(List<ImportColumn>)(List)dataTable.getDataTableColumns();
        for (ImportColumn column : columns) {
            coalesce.add(String.format("coalesce(\"%s\",'') ", column.getName()));
        }

        String selectColumns = "*";
        if (!includeGenerated) {
            selectColumns = "\"" + StringUtils.join(dataTable.getColumnNames(), "\", \"") + "\"";
        }

        String vector = StringUtils.join(coalesce, " || ' ' || ");
        String sql = String.format("SELECT %s from \"%s\" where to_tsvector('english', %s) @@ to_tsquery(%s)", selectColumns, dataTable.getName(), vector,
                SqlTools.quote(query, false));
        logger.debug(sql);
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectRowFromTable(ImportTable dataTable, ResultSetExtractor<T> resultSetExtractor, Long rowId) {
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.getTableNames().add(dataTable.getName());
        WhereCondition where = new WhereCondition(DataTableColumn.TDAR_ROW_ID.getName());
        where.setValue(rowId);
        builder.getWhere().add(where);
        return jdbcTemplate.query(builder.toSql(), resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public String selectTableAsXml(ImportTable dataTable) {
        String sql = String.format("select table_to_xml('%s',true,false,'');", dataTable.getName());
        logger.debug(sql);
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public boolean checkTableExists(ImportTable dataTable) {
        return checkTableExists(dataTable.getName());
    }

    private boolean checkTableExists(String table) {
        String sql = String.format("select * from \"%s\" limit 1", table);
        logger.trace(sql);
        try {
            SqlRowSet queryForRowSet = jdbcTemplate.queryForRowSet(sql);
            queryForRowSet.next();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
