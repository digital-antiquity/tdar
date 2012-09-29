package org.tdar.db.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.IntegrationRowData;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * Used to interact with the data import postgresql database.
 * 
 * FIXME: switch to SimpleJdbcTemplate eventually
 * 
 * @version $Revision$
 */
public class PostgresDatabase implements TargetDatabase {

    private static final String SELECT_FROM_WHERE_PARAM1 = "SELECT \"%s\" FROM %s WHERE ";
    private static final String IN_CLAUSE = " \"%s\" IN ( '%s' ) ";
    private static final String IN_CLAUSE_WITH_NULLS = " ( \"%s\" IN ( '%s' ) OR \"%s\" is NULL)";
    private static final String SELECT_ALL_FROM_TABLE = "SELECT %s FROM %s";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private static final String SELECT_DISTINCT = "SELECT DISTINCT \"%s\" FROM %s ORDER BY \"%s\"";
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
    private static final String SQL_ALTER_TABLE = "ALTER TABLE \"%1$s\" ALTER \"%2$s\" TYPE %3$s USING \"%2$s\"::%3$s";
    private static final String[] reservedNames = { "all", "analyse", "analyze", "and", "any", "array", "as", "asc", "asymmetric", "both", "case", "cast",
            "check", "collate", "column", "constraint", "create", "current_date", "current_role", "current_time", "current_timestamp", "current_user",
            "default", "deferrable", "desc", "distinct", "do", "double", "else", "end", "except", "for", "foreign", "from", "grant", "group", "having", "in",
            "initially", "intersect", "into", "leading", "limit", "localtime", "localtimestamp", "new", "not", "null", "off", "offset", "old", "on", "only",
            "or", "order", "placing", "primary", "references", "select", "session_user", "some", "symmetric", "table", "then", "to", "trailing", "union",
            "unique", "user", "using", "when", "where", "false", "true", "authorization", "between", "binary", "cross", "freeze", "full", "ilike", "inner",
            "is", "isnull", "join", "left", "like", "natural", "notnull", "outer", "overlaps", "right", "similar", "verbose" };
    /**
     * 
     */
    private static final String DEFAULT_TYPE = "text";

    private final static String SCHEMA_NAME = "public";
    private final static int BATCH_SIZE = 5000;

    private JdbcTemplate jdbcTemplate;

    private final Logger logger = Logger.getLogger(getClass());

    public static final int MAX_VARCHAR_LENGTH = 500;
    public static final int MAX_NAME_SIZE = 60;

    DateFormat dateFormat = new SimpleDateFormat();
    DateFormat accessDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");

    private Map<DataTable, Pair<PreparedStatement, Integer>> preparedStatementMap = new HashMap<DataTable, Pair<PreparedStatement, Integer>>();

    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRES;
    }

    public String getFullyQualifiedTableName(String tableName) {
        return SCHEMA_NAME + '.' + tableName;
    }

    public void dropTable(final DataTable dataTable) {
        dropTable(dataTable.getName());
    }

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
            e.getNextException().printStackTrace();
            throw new TdarRecoverableRuntimeException("an error ocurred while processing a prepared statement ", e);
        } finally {
            try {
                statement.clearBatch();
                statement.getConnection().close();
                preparedStatementMap.remove(dataTable);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("could not close and clear statement", e);
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

    /**
	 */
    // FIXME: THERE HAS TO BE A BETTER WAY TO DO THIS
    private String generateOntologyEnhancedSelect(DataTable table,
            List<DataTableColumn> columns, List<DataTableColumn> displayColumnNames,
            Collection<OntologyNode> ontologyNodes) {
        if (columns == null)
            columns = Collections.emptyList();
        if (displayColumnNames == null)
            displayColumnNames = Collections.emptyList();
        if (ontologyNodes == null)
            ontologyNodes = Collections.emptyList();

        // map quote to column names
        ArrayList<String> columnNames = new ArrayList<String>();
        for (DataTableColumn column : columns) {
            columnNames.add(column.getName());
        }
        for (DataTableColumn column : displayColumnNames) {
            columnNames.add(column.getName());
        }

        String columnNames_ = StringUtils.join(columnNames, "\",\"");

        ArrayList<String> whereParts = new ArrayList<String>();

        // now generate the where condition based on the ontology nodes
        logger.debug("ontology nodes: " + ontologyNodes);
        logger.debug("data table columns: " + columns);
        HashMap<Long, Set<String>> columnIdToIncludedValuesMap = generateIncludedValuesForIntegration(columns, ontologyNodes);
        // at this point columnIdToIncludedValuesMap should have all the values
        // that need to be selected.
        logger.debug("column id to included values map: " + columnIdToIncludedValuesMap);
        // finally, go through all of the integration columns and generate the
        // where condition for each column
        for (DataTableColumn column : columns) {
            String columnName = column.getName();
            Set<String> includedValues = columnIdToIncludedValuesMap.get(column.getId());
            if (CollectionUtils.isEmpty(includedValues)) {
                // no values to include
                logger.warn("No included values found while integrating column: " + columnName);
                continue;
            }
            whereParts.add(constructInClause(columnName, includedValues, true));
        }

        String sql = SELECT_FROM_WHERE_PARAM1 + StringUtils.join(whereParts, " AND ");
        sql = String.format(sql, columnNames_, table.getName());
        logger.debug(sql);
        return sql;
    }

    /**
     * @param columnName
     * @param includedValues
     * @return
     */
    private String constructInClause(String columnName, Set<String> includedValues, boolean includeNulls) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> iter = includedValues.iterator();
        while (iter.hasNext()) {
            String val = iter.next();
            sb.append(StringUtils.replace(val, "'", "''"));
            if (iter.hasNext())
                sb.append("','");
        }
        String format = IN_CLAUSE;
        if (includeNulls)
            format = IN_CLAUSE_WITH_NULLS;
        return String.format(format, columnName, sb.toString(), columnName);
    }

    @SuppressWarnings("unchecked")
    public IntegrationDataResult generateIntegrationResult(final DataTable table, final List<DataTableColumn> integrationColumns,
            List<DataTableColumn> columnsToDisplay, final Map<OntologyNode, OntologyNode> aggregatedOntologyNodeMap, Set<OntologyNode> allOntologyNodes) {
        String selectSql = generateOntologyEnhancedSelect(table, integrationColumns, columnsToDisplay, allOntologyNodes);

        IntegrationDataResult integrationDataResult = new IntegrationDataResult();
        integrationDataResult.setDataTable(table);
        integrationDataResult.setIntegrationColumns(integrationColumns);
        integrationDataResult.setColumnsToDisplay(columnsToDisplay);
        logger.debug("integration columns: " + integrationColumns);
        logger.debug("columns to display: " + columnsToDisplay);

        final int numberOfColumns = integrationColumns.size() + columnsToDisplay.size();
        logger.debug("number of columns to display: " + numberOfColumns);
        // based on the selected ontology nodes, generate a mapping between all selected ontology nodes (including implicitly selected child nodes)
        // and the ontology node that they should be aggregated towards (always upwards).

        final HashMap<String, OntologyNode> valuesToOntologyNodeMap = new HashMap<String, OntologyNode>();
        for (DataTableColumn column : integrationColumns) {
            valuesToOntologyNodeMap.putAll(column.getValueToOntologyNodeMap());
        }
        List<IntegrationRowData> rowDataList = new ArrayList<IntegrationRowData>();
        if (!selectSql.trim().toLowerCase().endsWith("where")) {
            rowDataList = query(selectSql, new ParameterizedRowMapper<IntegrationRowData>() {
                @Override
                public IntegrationRowData mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    // grab the data from this result set, populate the IntegrationDataResult
                    IntegrationRowData rowData = new IntegrationRowData();
                    ArrayList<String> values = new ArrayList<String>();
                    for (int i = 1; i <= numberOfColumns; i++) {
                        // note SQL iterator is 1 based; java iterator is 0 based
                        DataTableColumn column = table.getColumnByName(resultSet.getMetaData().getColumnName(i));
                        String value = getResultSetValueAsString(resultSet, i, column);
                        if (integrationColumns.contains(column) && StringUtils.isEmpty(value)) {
                            value = NULL_EMPTY_INTEGRATION_VALUE;
                        }
                        values.add(value);
                    }
                    ArrayList<OntologyNode> ontologyValues = new ArrayList<OntologyNode>();
                    // add the ontology value mapping for this integration value
                    for (int index = 0; index < integrationColumns.size(); index++) {
                        // the integration column values are the first values selected in the table
                        // so we iterate over the integration columns and grab the values at indices 0 .. integrationColumns.size
                        String integrationValue = values.get(index);
                        logger.trace("integration value: " + integrationValue);
                        // then find their associated ontology value.
                        OntologyNode node = valuesToOntologyNodeMap.get(integrationValue);
                        // now check to see if this node should be aggregated
                        ontologyValues.add(aggregatedOntologyNodeMap.get(node));
                        logger.trace("associated ontology node: " + valuesToOntologyNodeMap.get(integrationValue));
                    }
                    rowData.setDataValues(values);
                    rowData.setOntologyValues(ontologyValues);
                    return rowData;
                }
            });
        }
        integrationDataResult.setRowData(rowDataList);
        return integrationDataResult;
    }

    private HashMap<Long, Set<String>> generateIncludedValuesForIntegration(
            List<DataTableColumn> columns,
            Collection<OntologyNode> ontologyNodes) {
        HashMap<Long, Set<String>> columnIdToIncludedValuesMap = new HashMap<Long, Set<String>>();

        // FIXME: SIMPLIFY
        for (OntologyNode node : ontologyNodes) {
            logger.trace(node.getIri());
            // for each data table column, find the values that have been mapped to this node. All of those values should be included in the WHERE condition for
            // this column.
            for (DataTableColumn column : columns) {
                List<String> mappedDataValues = node.getMappedDataValues(column);
                // all of these Strings should be included in the where condition where <column-name> in ("foo", "bar", "baz", "quux");
                logger.trace(mappedDataValues);
                Set<String> includedValues = columnIdToIncludedValuesMap.get(column.getId());
                if (includedValues == null) {
                    includedValues = new HashSet<String>();
                    columnIdToIncludedValuesMap.put(column.getId(), includedValues);
                }
                includedValues.addAll(mappedDataValues);
            }
        }
        return columnIdToIncludedValuesMap;
    }

    @SuppressWarnings("all")
    public List query(String sql, RowMapper rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) {
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    public <T> T selectAllFromTable(DataTable table,
            ResultSetExtractor<T> resultSetExtractor, boolean includeGeneratedValues) {
        String selectColumns = "*";
        if (!includeGeneratedValues) {
            selectColumns = "\"" + StringUtils.join(table.getColumnNames(), "\", \"") + "\"";
        }

        return jdbcTemplate.query(String.format(SELECT_ALL_FROM_TABLE, selectColumns, table.getName()), resultSetExtractor);
    }

    public List<String> selectDistinctValues(DataTableColumn dataTableColumn) {
        if (dataTableColumn == null) {
            return Collections.emptyList();
        }
        String distinctSql = String.format(SELECT_DISTINCT, dataTableColumn.getName(), dataTableColumn.getDataTable().getName(), dataTableColumn.getName());
        return jdbcTemplate.queryForList(distinctSql, String.class);
    }

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

    public String normalizeTableOrColumnNames(String name) {
        String result = name.trim().replaceAll("[^\\w]", "_").toLowerCase();
        if (result.length() > MAX_NAME_SIZE)
            result = result.substring(0, MAX_NAME_SIZE);

        if (ArrayUtils.contains(reservedNames, result)) {
            result = "col_" + result;
        }
        if (result.equals("")) {
            result = "col_blank";
        }

        if (StringUtils.isNumeric(result.substring(0, 1))) {
            result = "c" + result;
        }

        return result;
    }

    // FIXME: allows for totally free form queries, refine this later?
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
            e.printStackTrace();
            logger.error("Couldn't access tdar data import database metadata",
                    e);
        }
        return false;
    }

    public void alterTableColumnType(String tableName, DataTableColumn column,
            DataTableColumnType type) {
        alterTableColumnType(tableName, column, type, -1);
    }

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
        logger.trace("creating definition: " + str);
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
        logger.info("creating table:" + dataTable.getName());
        logger.trace(sql);
        getJdbcTemplate().execute(sql);
    }

    public void createPreparedStatement(DataTable dataTable,
            List<DataTableColumn> list) throws SQLException {
        String insertSQL = INSERT_STATEMENT;

        List<String> columnNameList = dataTable.getColumnNames();
        String columnNames = StringUtils.join(columnNameList, ", ");
        String valuePlaceHolder = StringUtils.repeat("?", ", ", columnNameList.size());
        String sql = String.format(insertSQL, dataTable.getName(), columnNames, valuePlaceHolder);
        logger.trace(sql);

        PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
        preparedStatementMap.put(dataTable, new Pair<PreparedStatement, Integer>(preparedStatement, 0));
    }

    @Override
    public void addTableRow(DataTable dataTable,
            Map<DataTableColumn, String> valueColumnMap) throws Exception {
        if (MapUtils.isEmpty(valueColumnMap))
            return;

        if (preparedStatementMap.get(dataTable) == null) {
            createPreparedStatement(dataTable, dataTable.getDataTableColumns());
        }

        PreparedStatement preparedStatement = preparedStatementMap.get(
                dataTable).getFirst();
        logger.trace(valueColumnMap);

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
                case DATE:
                case DATETIME:
                    Date date = null;
                    try {
                        java.sql.Date.valueOf(colValue);
                        date = dateFormat.parse(colValue);
                    } catch (Exception e) {
                        logger.trace(e);
                    }
                    try {
                        date = accessDateFormat.parse(colValue);
                    } catch (Exception e) {
                        logger.trace(e);
                    }
                    if (date != null) {
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
                        preparedStatement.setTimestamp(i, sqlDate);
                    } else {
                        throw new TdarRecoverableRuntimeException("don't know how to parse date: " + colValue);
                    }
                    break;
                default:
                    preparedStatement.setString(i, colValue);
            }

        } else {
            preparedStatement.setNull(i, column.getColumnDataType().getSqlType());
        }
    }

    private String getResultSetValueAsString(ResultSet result, int i, DataTableColumn column) throws SQLException {
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
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                return connection.prepareStatement(updateColumnSql);
            }
        };
        PreparedStatementCallback<Object> translateColumnCallback = new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(
                    PreparedStatement preparedStatement) throws SQLException,
                    DataAccessException {
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
}
