package org.tdar.db.model;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.RowOperations;
import org.tdar.db.conversion.analyzers.DateAnalyzer;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.odata.server.AbstractDataRecord;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationContext;
import org.tdar.struts.data.ModernIntegrationDataResult;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * Used to interact with the data import postgresql database.
 * 
 * FIXME: switch to SimpleJdbcTemplate, use PreparedStatement api when possible (e.g. all values)
 * 
 * @version $Revision$
 */
public class PostgresDatabase implements TargetDatabase, RowOperations {

    public static final int MAX_VARCHAR_LENGTH = 500;
    private static final String SELECT_ALL_FROM_TABLE = "SELECT %s FROM %s";
    private static final String SELECT_ROW_FROM_TABLE = "SELECT * FROM %s WHERE " + TDAR_ID_COLUMN + " = %s";
    private static final String SELECT_ALL_FROM_TABLE_WITH_ORDER = "SELECT %s FROM %s order by " + TargetDatabase.TDAR_ID_COLUMN;
    private static final String SELECT_ROW_COUNT = "SELECT COUNT(0) FROM %s";
    private static final String SELECT_ALL_FROM_COLUMN = "SELECT \"%s\" FROM %s";
    private static final String SELECT_ALL_FROM_TABLE_WHERE = "SELECT %s FROM %s WHERE \"%s\"=?";
    // private static final String SELECT_ALL_FROM_TABLE_WHERE = "SELECT %s FROM %s WHERE \"%s\"=\'%s\'";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private static final String SELECT_DISTINCT = "SELECT DISTINCT \"%s\" FROM %s ORDER BY \"%s\"";
    private static final String SELECT_DISTINCT_WITH_COUNT = "SELECT DISTINCT \"%s\", count(" + TargetDatabase.TDAR_ID_COLUMN
            + ") FROM %s group by \"%s\" ORDER BY \"%s\"";
    private static final String SELECT_DISTINCT_NOT_BLANK = "SELECT DISTINCT \"%s\" FROM %s WHERE \"%s\" IS NOT NULL AND \"%s\" !='' ORDER BY \"%s\"";
    private static final String SELECT_DISTINCT_NOT_BLANK_NUM = "SELECT DISTINCT \"%s\" FROM %s WHERE \"%s\" IS NOT NULL ORDER BY \"%s\"";
    private static final String ALTER_DROP_COLUMN = "ALTER TABLE %s DROP COLUMN \"%s\"";
    private static final String UPDATE_UNMAPPED_CODING_SHEET = "UPDATE %s SET \"%s\"='No coding sheet value for code: ' || \"%s\" WHERE \"%s\" IS NULL";
    private static final String UPDATE_COLUMN_SET_VALUE = "UPDATE %s SET \"%s\"=? WHERE \"%s\"=?";
    private static final String ADD_COLUMN = "ALTER TABLE %s ADD COLUMN \"%s\" character varying";
    private static final String RENAME_COLUMN = "ALTER TABLE %s RENAME COLUMN \"%s\" TO \"%s\"";
    private static final String UPDATE_COLUMN_TO_NULL = "UPDATE %s SET \"%s\"=NULL";
    private static final String ORIGINAL_KEY = "_original_";
    private static final String INSERT_STATEMENT = "INSERT INTO %1$s (%2$s) VALUES(%3$s)";
    private static final String CREATE_TABLE = "CREATE TABLE %1$s (" + TDAR_ID_COLUMN + " bigserial, %2$s)";
    private static final String CREATE_TEMPORARY_TABLE = "CREATE TEMPORARY TABLE %1$s (" + TDAR_ID_COLUMN + " bigserial, %2$s)";
    private static final String SQL_ALTER_TABLE = "ALTER TABLE \"%1$s\" ALTER \"%2$s\" TYPE %3$s USING \"%2$s\"::%3$s";
    public static final HashSet<String> RESERVED_COLUMN_NAMES = new HashSet<String>(
            Arrays.asList("all", "analyse", "analyze", "and", "any", "array", "as", "asc", "asymmetric", "both", "case", "cast",
                    "check", "collate", "column", "constraint", "create", "current_date", "current_role", "current_time", "current_timestamp", "current_user",
                    "default", "deferrable", "desc", "distinct", "do", "double", "else", "end", "except", "for", "foreign", "from", "grant", "group", "having",
                    "in",
                    "initially", "intersect", "into", "leading", "limit", "localtime", "localtimestamp", "new", "not", "null", "off", "offset", "old", "on",
                    "only",
                    "or", "order", "placing", "primary", "references", "select", "session_user", "some", "symmetric", "table", "then", "to", "trailing",
                    "union",
                    "unique", "user", "using", "when", "where", "false", "true", "authorization", "between", "binary", "cross", "freeze", "full", "ilike",
                    "inner",
                    "is", "isnull", "join", "left", "like", "natural", "notnull", "outer", "overlaps", "right", "similar", "verbose")
            );
    public static final String DEFAULT_TYPE = "text";
    public static final String SCHEMA_NAME = "public";
    public static final int BATCH_SIZE = 5000;
    public static final int MAX_NAME_SIZE = 63;
    public static final int MAX_ALLOWED_COLUMNS = 500;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // FIXME: replace with LoadingCache for simpler usage (and better concurrent performance)
    private ConcurrentMap<DataTable, Pair<PreparedStatement, Integer>> preparedStatementMap =
            new ConcurrentHashMap<DataTable, Pair<PreparedStatement, Integer>>();

    private JdbcTemplate jdbcTemplate;

    @Override
    public int getMaxTableLength() {
        return MAX_NAME_SIZE;
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRES;
    }

    @Override
    public String getFullyQualifiedTableName(String tableName) {
        return SCHEMA_NAME + '.' + tableName;
    }

    @Override
    public void dropTable(final DataTable dataTable) {
        dropTable(dataTable.getName());
    }

    @Override
    public void dropTable(final String tableName) {
        try {
            jdbcTemplate.execute(String.format(DROP_TABLE, getFullyQualifiedTableName(tableName)));
        } catch (BadSqlGrammarException exception) {
            if (isAcceptableException(exception.getSQLException())) {
                return;
            }
            throw new TdarRecoverableRuntimeException(exception);
        }
    }

    public void addOrExecuteBatch(DataTable dataTable, boolean force) {
        Pair<PreparedStatement, Integer> statementPair = preparedStatementMap.get(dataTable);
        logger.trace("adding or executing batch for {} with statement pair {}", dataTable, statementPair);
        if (statementPair == null)
            return;

        PreparedStatement statement = statementPair.getFirst();
        int batchNum = statementPair.getSecond().intValue() + 1;

        statementPair.setSecond(batchNum);
        if (batchNum < BATCH_SIZE && !force) {
            try {
                statement.addBatch();
            } catch (SQLException e) {
                logger.error("an error ocurred while processing a prepared statement ", e);
            }
            return;
        }
        try {
            String success = "all";
            int[] numUpdates = statement.executeBatch();
            for (int i = 0; i < numUpdates.length; i++) {
                if (numUpdates[i] == -2) {
                    logger.error("Execution " + i + ": unknown number of rows updated");
                    success = "some";
                } else
                    logger.trace("Execution " + i + " successful: " + numUpdates[i] + " rows updated");
            }
            logger.debug(numUpdates.length + " inserts/updates commited " + success + " successful");
            // cleanup
        } catch (SQLException e) {
            logger.warn("sql exception", e.getNextException());
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("postgresDatabase.prepared_statement_fail"), e);
        } finally {
            try {
                statement.clearBatch();
                statement.getConnection().close();
                preparedStatementMap.remove(dataTable);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("postgresDatabase.could_not_close"), e);
            }
        }
    }

    public void createTable(final String createTableStatement) {
        logger.debug(createTableStatement);
        jdbcTemplate.execute(createTableStatement);
    }

    private boolean isAcceptableException(SQLException exception) {
        return exception.getSQLState().equals(getSqlDropStateError());
    }

    @SuppressWarnings("all")
    public List query(String sql, RowMapper rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) {
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    @Deprecated
    public <T> T selectAllFromTable(DataTable table,
            ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        String selectColumns = "*";
        if (!includeGeneratedValues) {
            selectColumns = "\"" + StringUtils.join(table.getColumnNames(), "\", \"") + "\"";
        }

        return jdbcTemplate.query(String.format(SELECT_ALL_FROM_TABLE, selectColumns, table.getName()), resultSetExtractor);
    }

    @Override
    public <T> T selectAllFromTableInImportOrder(DataTable table,
            ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        String selectColumns = "*";
        if (!includeGeneratedValues) {
            selectColumns = "\"" + StringUtils.join(table.getColumnNames(), "\", \"") + "\"";
        }

        String sql = String.format(SELECT_ALL_FROM_TABLE_WITH_ORDER, selectColumns, table.getName());
        logger.debug(sql);
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    public List<String> selectDistinctValues(DataTableColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        String distinctSql = String.format(SELECT_DISTINCT, dataTableColumn.getName(), dataTableColumn.getDataTable().getName(), dataTableColumn.getName());
        return jdbcTemplate.queryForList(distinctSql, String.class);
    }

    @Override
    public Map<String, Long> selectDistinctValuesWithCounts(DataTableColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyMap();
        }
        String distinctSql = String.format(SELECT_DISTINCT_WITH_COUNT, dataTableColumn.getName(), dataTableColumn.getDataTable().getName(),
                dataTableColumn.getName(),
                dataTableColumn.getName());

        final Map<String, Long> toReturn = new HashMap<String, Long>();
        query(distinctSql, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                toReturn.put(rs.getString(0), rs.getLong(1));
                return null;
            }
        });
        return toReturn;
    }

    @Override
    public List<String> selectNonNullDistinctValues(
            DataTableColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        String templateSql = SELECT_DISTINCT_NOT_BLANK;
        if (dataTableColumn.getColumnDataType().isNumeric())
            templateSql = SELECT_DISTINCT_NOT_BLANK_NUM;

        String distinctSql = String.format(templateSql, dataTableColumn.getName(), dataTableColumn.getDataTable().getName(),
                dataTableColumn.getName(), dataTableColumn.getName(), dataTableColumn.getName());
        logger.debug(distinctSql);
        return jdbcTemplate.queryForList(distinctSql, String.class);
    }

    @Override
    public String normalizeTableOrColumnNames(String name) {
        String result = name.trim().replaceAll("[^\\w]", "_").toLowerCase();
        if (result.length() > MAX_NAME_SIZE) {
            result = result.substring(0, MAX_NAME_SIZE);
        }
        if (RESERVED_COLUMN_NAMES.contains(result)) {
            result = "col_" + result;
        }
        if (result.equals("")) {
            result = "col_blank";
        }

        if (StringUtils.isNumeric(result.substring(0, 1))) {
            // FIXME: document this
            result = "c" + result;
        }
        return result;
    }

    // FIXME: allows for totally free form queries, refine this later?
    @Override
    public <T> T query(PreparedStatementCreator psc,
            PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return jdbcTemplate.query(psc, pss, rse);
    }

    public String getSqlDropStateError() {
        return "42P01";
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

    /**
     * Callers that invoke this method must manage the returned Connection
     * properly and manually close it after use.
     */
    protected Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasColumn(final String tableName, final String columnName) {
        logger.debug("Checking if " + tableName + " has column " + columnName);
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

    @Override
    public void alterTableColumnType(String tableName, DataTableColumn column,
            DataTableColumnType type) {
        alterTableColumnType(tableName, column, type, -1);
    }

    @Override
    public void alterTableColumnType(String tableName, DataTableColumn column,
            DataTableColumnType columnType, int length) {
        String type = toImplementedTypeDeclaration(columnType, length);
        String sqlAlterTable = SQL_ALTER_TABLE;
        String sql = String.format(sqlAlterTable, tableName, column.getName(), type);
        logger.trace(sql);
        getJdbcTemplate().execute(sql);
    }

    public String getDefaultTypeDeclaration() {
        return DEFAULT_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.db.model.abstracts.TargetDatabase#toLocalType(org.tdar.core.
     * bean.resource.DataTableColumnType, int)
     */
    @Override
    public String toImplementedTypeDeclaration(DataTableColumnType dataType,
            int length) {
        String str = getDefaultTypeDeclaration();

        // enforcing a minimum width on all column lengths (TDAR-1105)
        if (length < 1) {
            length = 1;
        }

        switch (dataType) {
            case BIGINT:
                str = "bigint";
                break;
            case DOUBLE:
                str = "double precision";
                break;
            case DATE:
            case DATETIME:
                str = "timestamp with time zone";
                break;
            case VARCHAR:
                if (length <= MAX_VARCHAR_LENGTH) {
                    str = "character varying (" + length + ")";
                    break;
                }
            default:
                str = getDefaultTypeDeclaration();
        }
        logger.trace("creating definition: {}", str);
        return str;
    }

    @Override
    public void createTable(DataTable dataTable) {
        dropTable(dataTable);
        String createTable = CREATE_TABLE;
        String COL_DEF = "\"%1$s\" %2$s";

        // take all of the columns that we're dealing with and
        StringBuilder tableColumnBuilder = new StringBuilder();
        Set<String> columnNames = new HashSet<String>();
        Iterator<DataTableColumn> iterator = dataTable.getDataTableColumns().iterator();
        int i = 1;
        if (dataTable.getDataTableColumns().size() > MAX_ALLOWED_COLUMNS) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("postgresDatabase.datatable_to_long"));
        }

        while (iterator.hasNext()) {
            DataTableColumn column = iterator.next();
            if (columnNames.contains(column.getName())) {
                column.setName(column.getName() + i);
                column.setDisplayName(column.getDisplayName() + i);
            }
            columnNames.add(column.getName());
            i++;
            tableColumnBuilder.append(String.format(COL_DEF, column.getName(),
                    this.toImplementedTypeDeclaration(column.getColumnDataType(), column.getLength())));
            if (iterator.hasNext()) {
                tableColumnBuilder.append(", ");

            }
        }

        String sql = String.format(createTable, dataTable.getName(), tableColumnBuilder.toString());
        logger.info("creating table: {}", dataTable.getName());
        logger.debug(sql);
        getJdbcTemplate().execute(sql);
    }

    public PreparedStatement createPreparedStatement(DataTable dataTable, List<DataTableColumn> list) throws SQLException {
        String insertSQL = INSERT_STATEMENT;

        List<String> columnNameList = dataTable.getColumnNames();
        String columnNames = StringUtils.join(columnNameList, ", ");
        String valuePlaceHolder = StringUtils.repeat("?", ", ", columnNameList.size());
        String sql = String.format(insertSQL, dataTable.getName(), columnNames, valuePlaceHolder);
        logger.trace(sql);

        return getConnection().prepareStatement(sql);
    }

    private <K, V> V getOrCreate(K key, ConcurrentMap<K, V> concurrentMap, Callable<V> newValueCreator) throws Exception {
        V value = concurrentMap.get(key);
        if (value == null) {
            V newValue = newValueCreator.call();
            value = concurrentMap.putIfAbsent(key, newValue);
            if (value == null) {
                value = newValue;
            }
        }
        return value;
    }

    private Callable<Pair<PreparedStatement, Integer>> createPreparedStatementPairCallable(final DataTable dataTable) {
        return new Callable<Pair<PreparedStatement, Integer>>() {
            @Override
            public Pair<PreparedStatement, Integer> call() throws Exception {
                return Pair.create(createPreparedStatement(dataTable, dataTable.getDataTableColumns()), 0);
            }
        };
    }

    @Override
    public void addTableRow(DataTable dataTable, Map<DataTableColumn, String> valueColumnMap) throws Exception {
        if (MapUtils.isEmpty(valueColumnMap))
            return;

        Pair<PreparedStatement, Integer> statementPair = getOrCreate(dataTable, preparedStatementMap, createPreparedStatementPairCallable(dataTable));
        PreparedStatement preparedStatement = statementPair.getFirst();
        logger.trace("{}", valueColumnMap);

        int i = 1;

        for (DataTableColumn column : dataTable.getDataTableColumns()) {
            String colValue = valueColumnMap.get(column);
            setPreparedStatementValue(preparedStatement, i, column, colValue);
            i++;
        }
        // push everything into batches
        addOrExecuteBatch(dataTable, false);
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int i, DataTableColumn column, String colValue) throws SQLException {
        // not thread-safe
        DateFormat dateFormat = new SimpleDateFormat();
        DateFormat accessDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        if (!StringUtils.isEmpty(colValue)) {
            switch (column.getColumnDataType()) {
                case BOOLEAN:
                    preparedStatement.setBoolean(i, Boolean.parseBoolean(colValue));
                    break;
                case DOUBLE:
                    preparedStatement.setDouble(i, Double.parseDouble(colValue));
                    break;
                case BIGINT:
                    preparedStatement.setLong(i, Long.parseLong(colValue));
                    break;
                case BLOB:
                    preparedStatement.setBinaryStream(i, new ByteArrayInputStream(colValue.getBytes()), colValue.length());
                    break;
                case DATE:
                case DATETIME:

                    Date date = null;
                    // 3 cases -- it's a date already
                    try {
                        java.sql.Date.valueOf(colValue);
                        date = dateFormat.parse(colValue);
                    } catch (Exception e) {
                        logger.trace("couldn't parse " + colValue, e);
                    }
                    // it's an Access date
                    try {
                        date = accessDateFormat.parse(colValue);
                    } catch (Exception e) {
                        logger.trace("couldn't parse " + colValue, e);
                    }
                    // still don't know, so it came from the date analyzer
                    if (date == null) {
                        date = DateAnalyzer.convertValue(colValue);
                    }
                    if (date != null) {
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
                        preparedStatement.setTimestamp(i, sqlDate);
                    } else {
                        throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("postgresDatabase.cannot_parse_date",
                                colValue.toString(), column.getName(), column.getDataTable().getName()));
                    }
                    break;
                default:
                    preparedStatement.setString(i, colValue);
            }

        } else {
            preparedStatement.setNull(i, column.getColumnDataType().getSqlType());
        }
    }

    @Override
    public String getResultSetValueAsString(ResultSet result, int i, DataTableColumn column) throws SQLException {
        try {
            switch (column.getColumnDataType()) {
                case BOOLEAN:
                    return Boolean.toString(result.getBoolean(i));
                case DOUBLE:
                    return Double.toString(result.getDouble(i));
                case BIGINT:
                    return Long.toString(result.getLong(i));
                case DATE:
                case DATETIME:
                    return result.getDate(i).toString();
                default:
                    return result.getString(i);
            }
        } catch (Exception e) {
            // FIXME: this may cause an issue with a coded value that's numeric
            if (column.getColumnEncodingType() == DataTableColumnEncodingType.CODED_VALUE) {
                return result.getString(i);
            }
        }
        return null;
    }

    @Override
    public void closePreparedStatements(Collection<DataTable> dataTables) throws Exception {
        for (DataTable table : dataTables) {
            addOrExecuteBatch(table, true);
        }
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
    public void translateInPlace(final DataTableColumn column,
            final CodingSheet codingSheet) {
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
        // have to search for the <column-name>_original_<id> column and make a somewhat dangerous assumption that if this column exists, then we are the ones
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
        final String updateColumnSql = String.format(UPDATE_COLUMN_SET_VALUE, tableName, columnName, originalColumnName);
        logger.debug("translating column from " + tableName + " (" + columnName + ")");
        PreparedStatementCreator translateColumnPreparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                return connection.prepareStatement(updateColumnSql);
            }
        };
        PreparedStatementCallback<Object> translateColumnCallback = new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                for (CodingRule codingRule : codingSheet.getCodingRules()) {
                    String code = codingRule.getCode();
                    String term = codingRule.getTerm();
                    // 1st parameter is the translated term that we want to set
                    preparedStatement.setString(1, term);
                    logger.trace("code:" + code + " term:" + term + " " + columnDataType + " [" + updateColumnSql + "]");
                    // 2nd parameter is the where condition, the code that we want to translate.
                    boolean okToExecute = false;
                    switch (columnDataType) {
                        case BIGINT:
                            try {
                                preparedStatement.setLong(2, Long.valueOf(code));
                                okToExecute = true;
                            } catch (Exception e) {
                                logger.debug("problem casting code " + code + " to a Column's Long Mapping (ignoring)");
                            }
                            break;
                        case DOUBLE:
                            try {
                                preparedStatement.setDouble(2, Double.valueOf(code));
                                okToExecute = true;
                            } catch (Exception e) {
                                logger.debug("problem casting " + code + " to a Double");
                            }
                            break;
                        case VARCHAR:
                            preparedStatement.setString(2, code);
                            okToExecute = true;
                            break;
                        default:
                            break;
                    }
                    if (okToExecute) {
                        logger.trace("Prepared statement is: "
                                + preparedStatement.toString());
                        preparedStatement.addBatch();
                    } else {
                        logger.debug("code:" + code + " was not a valid type for " + columnDataType);
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

    public void executeUpdateOrDelete(final String createTableStatement) {
        logger.debug(createTableStatement);
        jdbcTemplate.execute(createTableStatement);
    }

    public ModernIntegrationDataResult generateModernIntegrationResult(IntegrationContext proxy) {
        ModernIntegrationDataResult result = new ModernIntegrationDataResult();
        createTable(String.format(CREATE_TEMPORARY_TABLE, proxy.getTempTableName()));
        for (IntegrationColumn column : proxy.getIntegrationColumns()) {
            String deflt = MessageHelper.getMessage("database.null_empty_integration_value");
            if (column.isDisplayColumn()) {
                deflt = MessageHelper.getMessage("database.null_empty_mapped_value");
            }
            executeUpdateOrDelete(String.format(ADD_COLUMN + " DEFAULT %s", column.getName(), deflt));
        }

        for (DataTable table : proxy.getDataTables()) {
            generateModernIntegrationResult(proxy, table);
        }

        List<Pair<DataTableColumn, List<String>>> updates = new ArrayList<Pair<DataTableColumn, List<String>>>();
        /*
         * instead of doing this, create a separate lookup table for value -> mapped value
         * then do update bound on those values
         */
        for (IntegrationColumn integrationColumn : proxy.getIntegrationColumns()) {
            for (OntologyNode node : integrationColumn.getOntologyNodesForSelect()) {
                for (DataTableColumn column : integrationColumn.getColumns()) {
                    updates.add(new Pair<DataTableColumn, List<String>>(column, column.getMappedDataValues(node)));
                }
            }
            // update each column separately
        }

        // figure out how to handle pivots
        result.setResultSet(null);
        return result;
    }

    public void generateModernIntegrationResult(final IntegrationContext proxy, final DataTable table) {
        String selectSql = generateModernOntologyEnhancedSelect(table, proxy);

        if (!selectSql.toLowerCase().contains(" where ")) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("postgresDatabase.integration_query_broken"));
        }

        executeUpdateOrDelete(selectSql);
    }

    private String quote(String term) {
        return quote(term, true);
    }

    private String quote(String term, boolean doubleQuote) {
        String chr = "\'";
        if (doubleQuote) {
            chr = "\"";
        } else {
            term = StringUtils.replace(term, "'", "''");
        }
        return " " + chr + term + chr + " ";
    }

    private String generateModernOntologyEnhancedSelect(DataTable table, IntegrationContext proxy) {
        StringBuilder selectPart = new StringBuilder("SELECT ");
        StringBuilder wherePart = new StringBuilder(" WHERE ");
        boolean firstWhere = true;
        List<String> colNames = new ArrayList<String>();

        // FOR EACH COLUMN, grab the value, for the table or use '' to keep the spacing correct
        for (IntegrationColumn integrationColumn : proxy.getIntegrationColumns()) {
            logger.info("table:" + table + " column: " + integrationColumn);
            DataTableColumn column = integrationColumn.getColumnForTable(table);
            String name = "''";
            if (column != null) {
                name = quote(column.getName());
            }
            colNames.add(name);

            // if we're an integration column, quote and grab all of the ontology nodes for the select
            // these are the "hierarchical" values
            if (!integrationColumn.isDisplayColumn() && column != null) {
                Set<String> whereVals = new HashSet<String>();
                for (OntologyNode node : integrationColumn.getOntologyNodesForSelect()) {
                    for (String val : column.getMappedDataValues(node)) {
                        whereVals.add(quote(StringEscapeUtils.escapeSql(val), false));
                    }
                }
                if (whereVals.isEmpty()) {
                    continue;
                }

                if (!firstWhere) {
                    wherePart.append(" AND ");
                } else {
                    firstWhere = false;
                }
                if (integrationColumn.isNullIncluded()) {
                    wherePart.append(" (");
                }
                wherePart.append(name).append(" IN (").append(StringUtils.join(whereVals, ",")).append(") ");
                if (integrationColumn.isNullIncluded()) {
                    wherePart.append("OR ").append(name).append(" IS NULL) ");
                }
            }
        }
        selectPart.append(StringUtils.join(colNames, ",")).append(" FROM ").append(table.getName());
        if (!firstWhere) {
            selectPart.append(wherePart);
        }
        return selectPart.toString();
    }

    @Override
    public String generateOntologyEnhancedSelect(DataTable table, List<IntegrationColumn> integrationColumns,
            final Map<List<OntologyNode>, Map<DataTable, Integer>> pivot) {
        StringBuilder selectPart = new StringBuilder("SELECT ");
        StringBuilder wherePart = new StringBuilder(" WHERE ");
        boolean firstWhere = true;
        List<String> colNames = new ArrayList<String>();

        // FOR EACH COLUMN, grab the value, for the table or use '' to keep the spacing correct
        for (IntegrationColumn integrationColumn : integrationColumns) {
            logger.info("table: {} column: {}", table, integrationColumn);
            DataTableColumn column = integrationColumn.getColumnForTable(table);
            String name = "''";
            if (column != null) {
                name = quote(column.getName());
            }
            colNames.add(name);

            // if we're an integration column, quote and grab all of the ontology nodes for the select
            // these are the "hierarchical" values
            if (!integrationColumn.isDisplayColumn() && column != null) {
                Set<String> whereVals = new HashSet<String>();
                for (OntologyNode node : integrationColumn.getOntologyNodesForSelect()) {
                    for (String val : column.getMappedDataValues(node)) {
                        whereVals.add(quote(StringEscapeUtils.escapeSql(val), false));
                    }

                    // FIXME: need to verify / replace this logic at some point
                    // for (String val : node.getMappedDataValues(column)) {
                    //
                    // }
                }
                if (whereVals.isEmpty()) {
                    continue;
                }

                if (!firstWhere) {
                    wherePart.append(" AND ");
                } else {
                    firstWhere = false;
                }
                if (integrationColumn.isNullIncluded()) {
                    wherePart.append(" (");
                }
                wherePart.append(name).append(" IN (").append(StringUtils.join(whereVals, ",")).append(") ");
                if (integrationColumn.isNullIncluded()) {
                    wherePart.append("OR ").append(name).append(" IS NULL) ");
                }
            }
        }
        selectPart.append(StringUtils.join(colNames, ",")).append(" FROM ").append(table.getName());
        if (!firstWhere) {
            selectPart.append(wherePart);
        }
        return selectPart.toString();
    }

    @Override
    public <T> T selectAllFromTable(DataTableColumn column, String key, ResultSetExtractor<T> resultSetExtractor) {
        String selectColumns = "*";
        return jdbcTemplate.query(String.format(SELECT_ALL_FROM_TABLE_WHERE, selectColumns, column.getDataTable().getName(), column.getName()),
                new String[] { key },
                resultSetExtractor);

    }

    public void renameColumn(DataTableColumn column, String newName) {
        logger.warn("RENAMING COLUMN " + column + " TO " + newName, new Exception("altering column should only be done by tests."));
        String sql = String.format(RENAME_COLUMN, column.getDataTable().getName(), column.getName(), newName);
        column.setName(newName);
        jdbcTemplate.execute(sql);
    }

    public List<String> getColumnNames(ResultSet resultSet) throws SQLException {
        List<String> columnNames = new ArrayList<String>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        for (int columnIndex = 0; columnIndex < metadata.getColumnCount(); columnIndex++) {
            String columnName = metadata.getColumnName(columnIndex + 1);
            columnNames.add(columnName);
        }
        return columnNames;
    }

    public int getRowCount(DataTable dataTable) {
        String sql = String.format(SELECT_ROW_COUNT, dataTable.getName());
        return jdbcTemplate.queryForInt(sql);
    }

    public List<String> selectAllFrom(final DataTableColumn column) {
        if (column == null) {
            return Collections.emptyList();
        }
        String sql = String.format(SELECT_ALL_FROM_COLUMN, column.getName(), column.getDataTable().getName());
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<String[]> query(String selectSql, ParameterizedRowMapper<String[]> parameterizedRowMapper) {
        return jdbcTemplate.query(selectSql, parameterizedRowMapper);
    }

    @Override
    public void editRow(DataTable dataTable, Long rowId, Map<?, ?> data) {

        String columnAssignments = "";
        final List<Object> values = new ArrayList<Object>();
        String separator = "";
        for (Object columnName : data.keySet())
        {
            if (!"id".equals(columnName))
            {
                columnAssignments += separator + columnName + "=" + "?";
                values.add(data.get(columnName));
                separator = " ";
            }
        }
        // Put id last so WHERE id = ? will work.
        values.add(data.get("id"));

        // TODO RR: should this fail with an exception?
        // Probably should log it.
        if (values.size() > 1)
        {
            String sqlTemplate = "UPDATE \"%s\" SET %s WHERE id = ?";
            String sql = String.format(sqlTemplate, dataTable.getName(), columnAssignments);

            jdbcTemplate.update(sql, values.toArray());
        }
    }

    @Override
    public Set<AbstractDataRecord> findAllRows(DataTable dataTable) {
        return null;
    }

    @Override
    public void deleteRow(DataTable dataTable, Long rowId) {
        // Do nothing.
        // Not allowed this time.
        // The use case was to allow modification of existing records.
        // I am interpreting this literally as update only.
        throw new NotImplementedException(MessageHelper.getMessage("error.not_implemented"));
    }

    @Override
    public List<List<String>> selectAllFromTable(DataTable dataTable, ResultSetExtractor<List<List<String>>> resultSetExtractor, boolean includeGenerated,
            String query) {
        List<String> coalesce = new ArrayList<String>();
        for (DataTableColumn column : dataTable.getDataTableColumns()) {
            coalesce.add(String.format("coalesce(\"%s\",'') ", column.getName()));
        }

        String selectColumns = "*";
        if (!includeGenerated) {
            selectColumns = "\"" + StringUtils.join(dataTable.getColumnNames(), "\", \"") + "\"";
        }

        String vector = StringUtils.join(coalesce, " || ' ' || ");
        String sql = String.format("SELECT %s from %s where to_tsvector('english', %s) @@ to_tsquery(%s)", selectColumns, dataTable.getName(), vector,
                quote(query, false));
        logger.debug(sql);
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    public <T> T selectRowFromTable(DataTable dataTable, ResultSetExtractor<T> resultSetExtractor, Long rowId) {
        String sql = String.format(SELECT_ROW_FROM_TABLE, dataTable.getName(), rowId);
        logger.debug(sql);
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    public String selectTableAsXml(DataTable dataTable) {
        String sql = String.format("select table_to_xml('%s',true,false,'');", dataTable.getName());
        logger.debug(sql);
        return jdbcTemplate.queryForObject(sql, String.class);
    }
}
