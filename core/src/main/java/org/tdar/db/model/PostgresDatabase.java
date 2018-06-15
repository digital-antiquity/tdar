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
import java.util.Comparator;
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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
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
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.db.builder.AbstractSqlTools;
import org.tdar.db.builder.SqlSelectBuilder;
import org.tdar.db.builder.WhereCondition;
import org.tdar.db.builder.WhereCondition.Condition;
import org.tdar.db.builder.WhereCondition.ValueCondition;
import org.tdar.db.conversion.analyzers.DateAnalyzer;
import org.tdar.db.model.abstracts.AbstractDataRecord;
import org.tdar.db.model.abstracts.RowOperations;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
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
@Component(value = "target")
public class PostgresDatabase extends AbstractSqlTools implements TargetDatabase, RowOperations, PostgresConstants {

    private static final String SELECT_ROW_COUNT = "SELECT COUNT(0) FROM %s";
    private static final String SELECT_ALL_FROM_TABLE_WHERE = "SELECT * FROM \"%s\" WHERE \"%s\"=?";
    private static final String SELECT_ALL_FROM_TABLE_WHERE_LOWER = "SELECT * FROM \"%s\" WHERE lower(\"%s\")=lower(?)";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private static final String ALTER_DROP_COLUMN = "ALTER TABLE \"%s\" DROP COLUMN \"%s\"";
    private static final String UPDATE_UNMAPPED_CODING_SHEET = "UPDATE \"%s\" SET \"%s\"='" + NO_CODING_SHEET_VALUE + " ' || \"%s\" WHERE \"%s\" IS NULL";
    private static final String UPDATE_COLUMN_SET_VALUE_TRIM = "UPDATE \"%s\" SET \"%s\"=? WHERE trim(\"%s\")=?";
    private static final String UPDATE_COLUMN_SET_VALUE = "UPDATE \"%s\" SET \"%s\"=? WHERE \"%s\"=?";
    public static final String ADD_COLUMN = "ALTER TABLE \"%s\" ADD COLUMN \"%s\" character varying";
    public static final String ADD_NUMERIC_COLUMN = "ALTER TABLE \"%s\" ADD COLUMN \"%s\" bigint";
    private static final String RENAME_COLUMN = "ALTER TABLE \"%s\" RENAME COLUMN \"%s\" TO \"%s\"";
    private static final String UPDATE_COLUMN_TO_NULL = "UPDATE \"%s\" SET \"%s\"=NULL";
    private static final String ORIGINAL_KEY = "__o";
    private static final String INSERT_STATEMENT = "INSERT INTO %1$s (%2$s) VALUES(%3$s)";
    private static final String CREATE_TABLE = "CREATE TABLE %1$s (" + DataTableColumn.TDAR_ID_COLUMN + " bigserial, %2$s)";
    private static final String SQL_ALTER_TABLE = "ALTER TABLE \"%1$s\" ALTER \"%2$s\" TYPE %3$s USING \"%2$s\"::%3$s";
    public static final String DEFAULT_TYPE = "text";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // FIXME: replace with LoadingCache for simpler usage (and better concurrent performance)
    private ConcurrentMap<DataTable, Pair<PreparedStatement, Integer>> preparedStatementMap = new ConcurrentHashMap<DataTable, Pair<PreparedStatement, Integer>>();

    private JdbcTemplate jdbcTemplate;

    @Override
    public int getMaxTableLength() {
        return MAX_NAME_SIZE;
    }

    @Override
    public int getMaxColumnNameLength() {
        return MAX_COLUMN_NAME_SIZE;
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
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void dropTable(final DataTable dataTable) {
        dropTable(dataTable.getName());
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void dropTable(final String tableName) {
        try {
            jdbcTemplate.execute(String.format(DROP_TABLE, getFullyQualifiedTableName(tableName)));
        } catch (BadSqlGrammarException exception) {
            if (isAcceptableException(exception.getSQLException())) {
                return;
            }
            throw new TdarRecoverableRuntimeException("postgresDatabase.cannot_delete_table", exception, Arrays.asList(tableName));
        }
    }

    @Transactional(value = "tdarDataTx", readOnly = false)
    public void addOrExecuteBatch(DataTable dataTable, boolean force) {
        Pair<PreparedStatement, Integer> statementPair = preparedStatementMap.get(dataTable);
        logger.trace("adding or executing batch for {} with statement pair {}", dataTable, statementPair);
        if (statementPair == null) {
            return;
        }

        PreparedStatement statement = statementPair.getFirst();
        int batchNum = statementPair.getSecond().intValue() + 1;

        statementPair.setSecond(batchNum);
        if ((batchNum < TdarConfiguration.getInstance().getTdarDataBatchSize()) && !force) {
            return;
        }
        try {
            String success = "all";
            int[] numUpdates = statement.executeBatch();
            for (int i = 0; i < numUpdates.length; i++) {
                if (numUpdates[i] == -2) {
                    logger.error("Execution {} : unknown number of rows updated", i);
                    success = "some";
                } else {
                    logger.trace("Execution {} successful: {} rows updated", i, numUpdates[i]);
                }
            }
            logger.debug("{} inserts/updates committed, {} successful", numUpdates.length, success);
            // cleanup
        } catch (SQLException e) {
            logger.warn("sql exception", e.getNextException());
            throw new TdarRecoverableRuntimeException("postgresDatabase.prepared_statement_fail", e);
        } finally {
            try {
                statement.clearBatch();
                statement.getConnection().close();
                preparedStatementMap.remove(dataTable);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("postgresDatabase.could_not_close", e);
            }
        }
    }

    @Transactional(value = "tdarDataTx", readOnly = false)
    public void createTable(final String createTableStatement) {
        logger.debug(createTableStatement);
        jdbcTemplate.execute(createTableStatement);
    }

    private boolean isAcceptableException(SQLException exception) {
        return exception.getSQLState().equals(getSqlDropStateError());
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
    public <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = getSelectAll(table, includeGeneratedValues);
        return jdbcTemplate.query(builder.toSql(), resultSetExtractor);
    }

    @Override
    @Deprecated
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTable(DataTable table, ResultSetExtractor<T> resultSetExtractor, String... orderBy) {
        SqlSelectBuilder builder = getSelectAll(table, false);
        builder.getOrderBy().addAll(Arrays.asList(orderBy));
        return jdbcTemplate.query(new LowMemoryStatementCreator(builder.toSql()), resultSetExtractor);
    }

    private SqlSelectBuilder getSelectAll(DataTable table, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = new SqlSelectBuilder();
        if (includeGeneratedValues) {
            builder.getColumns().add(DataTableColumn.TDAR_ROW_ID.getName());
        }
        List<DataTableColumn> columns = table.getDataTableColumns();
        columns.sort(new Comparator<DataTableColumn>() {

            @Override
            public int compare(DataTableColumn o1, DataTableColumn o2) {
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
        table.getDataTableColumns().forEach(dtc -> {
            builder.getColumns().add(dtc.getName());
            if (dtc.getDefaultCodingSheet() != null && includeGeneratedValues) {
                builder.getColumns().add(generateOriginalColumnName(dtc));
            }
        });
        builder.getTableNames().add(table.getName());
        return builder;
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectAllFromTableInImportOrder(DataTable table, ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        SqlSelectBuilder builder = getSelectAll(table, includeGeneratedValues);
        builder.getOrderBy().add(DataTableColumn.TDAR_ROW_ID.getName());
        LowMemoryStatementCreator lmsc = new LowMemoryStatementCreator(builder.toSql());
        return jdbcTemplate.query(lmsc, resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public List<String> selectDistinctValues(DataTableColumn dataTableColumn, boolean sort) {
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
        builder.getTableNames().add(dataTableColumn.getDataTable().getName());
        if (sort) {
            builder.getOrderBy().add(dataTableColumn.getName());
        }
        logger.trace(builder.toSql());
        return jdbcTemplate.queryForList(builder.toSql(), String.class);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public Map<String, Long> selectDistinctValuesWithCounts(DataTableColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyMap();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.setDistinct(true);
        builder.setCountColumn(dataTableColumn.getName());
        builder.getGroupBy().add(dataTableColumn.getName());
        builder.getColumns().add(dataTableColumn.getName());
        builder.getTableNames().add(dataTableColumn.getDataTable().getName());
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
    public List<String> selectNonNullDistinctValues(DataTableColumn dataTableColumn, boolean useUntranslatedValues) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.getTableNames().add(dataTableColumn.getDataTable().getName());
        builder.setDistinct(true);
        String name = dataTableColumn.getName();
        if (useUntranslatedValues) {
            String original = generateOriginalColumnName(dataTableColumn);
            if (hasColumn(dataTableColumn.getDataTable().getName(), original)) {
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
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return jdbcTemplate.query(psc, pss, rse);
    }

    public String getSqlDropStateError() {
        return DROP_STATE_ERROR_CODE;
    }

    @Qualifier("tdarDataImportDataSource")
    @Autowired(required = false)
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

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType type) {
        alterTableColumnType(tableName, column, type, -1);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType columnType, int length) {
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
    public String toImplementedTypeDeclaration(DataTableColumnType dataType, int length_) {
        int length = length_;
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
    @Transactional(value = "tdarDataTx", readOnly = false)
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
            throw new TdarRecoverableRuntimeException("postgresDatabase.datatable_to_long");
        }

        while (iterator.hasNext()) {
            DataTableColumn column = iterator.next();
            String name = column.getName();
            if (columnNames.contains(name)) {
                name = name + i;
                column.setName(name);
                column.setDisplayName(column.getDisplayName() + i);
            }
            columnNames.add(name);
            i++;
            tableColumnBuilder.append(String.format(COL_DEF, name,
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
        if (MapUtils.isEmpty(valueColumnMap)) {
            return;
        }

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
        addToBatch(preparedStatement);
        addOrExecuteBatch(dataTable, false);
    }

    private void addToBatch(PreparedStatement preparedStatement) {
        try {
            preparedStatement.addBatch();
        } catch (SQLException e) {
            logger.error("an error ocurred while processing a prepared statement ", e);
        }
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
                        throw new TdarRecoverableRuntimeException("postgresDatabase.cannot_parse_date",
                                Arrays.asList(colValue.toString(), column.getName(), column.getDataTable().getName()));
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
    @Transactional(value = "tdarDataTx", readOnly = false)
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
    public int getRowCount(DataTable dataTable) {
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
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void editRow(DataTable dataTable, Long rowId, Map<?, ?> data) {

        String columnAssignments = "";
        final List<Object> values = new ArrayList<Object>();
        String separator = "";
        for (Object columnName : data.keySet()) {
            if (!"id".equals(columnName)) {
                columnAssignments += separator + columnName + "=" + "?";
                values.add(data.get(columnName));
                separator = " ";
            }
        }
        // Put id last so WHERE id = ? will work.
        values.add(data.get("id"));

        // TODO RR: should this fail with an exception?
        // Probably should log it.
        if (values.size() > 1) {
            String sqlTemplate = "UPDATE \"%s\" SET %s WHERE id = ?";
            String sql = String.format(sqlTemplate, dataTable.getName(), columnAssignments);

            jdbcTemplate.update(sql, values.toArray());
        }
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public Set<AbstractDataRecord> findAllRows(DataTable dataTable) {
        return null;
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void deleteRow(DataTable dataTable, Long rowId) {
        // Do nothing.
        // Not allowed this time.
        // The use case was to allow modification of existing records.
        // I am interpreting this literally as update only.
        throw new NotImplementedException(MessageHelper.getMessage("error.not_implemented"));
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
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
        String sql = String.format("SELECT %s from \"%s\" where to_tsvector('english', %s) @@ to_tsquery(%s)", selectColumns, dataTable.getName(), vector,
                quote(query, false));
        logger.debug(sql);
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public <T> T selectRowFromTable(DataTable dataTable, ResultSetExtractor<T> resultSetExtractor, Long rowId) {
        SqlSelectBuilder builder = new SqlSelectBuilder();
        builder.getTableNames().add(dataTable.getName());
        WhereCondition where = new WhereCondition(DataTableColumn.TDAR_ROW_ID.getName());
        where.setValue(rowId);
        builder.getWhere().add(where);
        return jdbcTemplate.query(builder.toSql(), resultSetExtractor);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public String selectTableAsXml(DataTable dataTable) {
        String sql = String.format("select table_to_xml('%s',true,false,'');", dataTable.getName());
        logger.debug(sql);
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = true)
    public boolean checkTableExists(DataTable dataTable) {
        String sql = String.format("select * from \"%s\" limit 1", dataTable.getName());
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
