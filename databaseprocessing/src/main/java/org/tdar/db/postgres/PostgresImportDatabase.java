package org.tdar.db.postgres;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.db.Database;
import org.tdar.db.ImportDatabase;
import org.tdar.db.conversion.analyzers.DateAnalyzer;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.db.datatable.ImportTable;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.Pair;

@Component
public class PostgresImportDatabase implements ImportDatabase, Database, PostgresConstants {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private JdbcTemplate jdbcTemplate;
    // FIXME: replace with LoadingCache for simpler usage (and better concurrent performance)
    private ConcurrentMap<ImportTable, Pair<PreparedStatement, Integer>> preparedStatementMap = new ConcurrentHashMap<>();

    private static final String INSERT_STATEMENT = "INSERT INTO %1$s (%2$s) VALUES(%3$s)";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private static final String CREATE_TABLE = "CREATE TABLE %1$s (" + ImportColumn.TDAR_ID_COLUMN + " bigserial, %2$s)";
    private static final String SQL_ALTER_TABLE = "ALTER TABLE \"%1$s\" ALTER \"%2$s\" TYPE %3$s USING \"%2$s\"::%3$s";

    public static final String DEFAULT_TYPE = "text";

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
    public void dropTable(final ImportTable dataTable) {
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

    private boolean isAcceptableException(SQLException exception) {
        return exception.getSQLState().equals(getSqlDropStateError());
    }

    public String getSqlDropStateError() {
        return DROP_STATE_ERROR_CODE;
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void createTable(ImportTable dataTable) {
        dropTable(dataTable);
        String createTable = CREATE_TABLE;
        String COL_DEF = "\"%1$s\" %2$s";

        // take all of the columns that we're dealing with and
        StringBuilder tableColumnBuilder = new StringBuilder();
        Set<String> columnNames = new HashSet<String>();
        Iterator<? extends ImportColumn> iterator = dataTable.getDataTableColumns().iterator();
        int i = 1;
        if (dataTable.getDataTableColumns().size() > MAX_ALLOWED_COLUMNS) {
            throw new TdarRecoverableRuntimeException("postgresDatabase.datatable_to_long");
        }

        while (iterator.hasNext()) {
            ImportColumn column = iterator.next();
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

    
    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void alterTableColumnType(String tableName, ImportColumn column, DataTableColumnType type) {
        alterTableColumnType(tableName, column, type, -1);
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
    
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void addOrExecuteBatch(ImportTable dataTable, boolean force) {
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


    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void closePreparedStatements(Collection<ImportTable> dataTables) throws Exception {
        for (ImportTable table : dataTables) {
            addOrExecuteBatch(table, true);
        }
    }

    @Override
    @Transactional(value = "tdarDataTx", readOnly = false)
    public void alterTableColumnType(String tableName, ImportColumn column, DataTableColumnType columnType, int length) {
        String type = toImplementedTypeDeclaration(columnType, length);
        String sqlAlterTable = SQL_ALTER_TABLE;
        String sql = String.format(sqlAlterTable, tableName, column.getName(), type);
        logger.trace(sql);
        getJdbcTemplate().execute(sql);
    }


    public PreparedStatement createPreparedStatement(ImportTable dataTable, List<? extends ImportColumn> list) throws SQLException {
        String insertSQL = INSERT_STATEMENT;

        List<String> columnNameList = dataTable.getColumnNames();
        String columnNames = StringUtils.join(columnNameList, ", ");
        String valuePlaceHolder = StringUtils.repeat("?", ", ", columnNameList.size());
        String sql = String.format(insertSQL, dataTable.getName(), columnNames, valuePlaceHolder);
        logger.trace(sql);

        return getConnection().prepareStatement(sql);
    }


    private Callable<Pair<PreparedStatement, Integer>> createPreparedStatementPairCallable(final ImportTable dataTable) {
        return new Callable<Pair<PreparedStatement, Integer>>() {
            @Override
            public Pair<PreparedStatement, Integer> call() throws Exception {
                return Pair.create(createPreparedStatement(dataTable, dataTable.getDataTableColumns()), 0);
            }
        };
    }


    private void addToBatch(PreparedStatement preparedStatement) {
        try {
            preparedStatement.addBatch();
        } catch (SQLException e) {
            logger.error("an error ocurred while processing a prepared statement ", e);
        }
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int i, ImportColumn column, String dataTableName, String colValue)
            throws SQLException {
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
                                Arrays.asList(colValue.toString(), column.getName(), dataTableName));
                    }
                    break;
                default:
                    preparedStatement.setString(i, colValue);
            }

        } else {
            preparedStatement.setNull(i, column.getColumnDataType().getSqlType());
        }
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

    
    @Override
    public <T extends ImportColumn> void addTableRow(ImportTable<T> dataTable, Map<? extends ImportColumn, String> valueColumnMap) throws Exception {
        if (MapUtils.isEmpty(valueColumnMap)) {
            return;
        }

        Pair<PreparedStatement, Integer> statementPair = getOrCreate(dataTable, preparedStatementMap, createPreparedStatementPairCallable(dataTable));
        PreparedStatement preparedStatement = statementPair.getFirst();
        logger.trace("{}", valueColumnMap);

        int i = 1;

        for (ImportColumn column : dataTable.getDataTableColumns()) {
            String colValue = valueColumnMap.get(column);
            setPreparedStatementValue(preparedStatement, i, column, dataTable.getName(), colValue);
            i++;
        }
        // push everything into batches
        addToBatch(preparedStatement);
        addOrExecuteBatch(dataTable, false);
    }

    @Qualifier("tdarDataImportDataSource")
    @Autowired(required = false)
    @Override
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


}
