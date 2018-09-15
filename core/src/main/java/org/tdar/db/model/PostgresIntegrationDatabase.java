package org.tdar.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.ModernDataIntegrationWorkbook;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.db.builder.SqlSelectBuilder;
import org.tdar.db.builder.WhereCondition;
import org.tdar.db.model.abstracts.Database;
import org.tdar.db.model.abstracts.IntegrationDatabase;

import com.opensymphony.xwork2.TextProvider;

@Component
public class PostgresIntegrationDatabase extends PostgresDatabase implements IntegrationDatabase {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String INTEGRATION_TABLE_NAME_COL = "tableName";
    public static final String INTEGRATION_SUFFIX = "_int";

    @Override
    /**
     * Takes the IntegrationContext and produces a ModernIntegrationResult that contains the Excel Workbook and proxy information such as pivot data
     * and preview data.
     */
    @Transactional(value = "tdarDataTx", readOnly = false)
    public ModernIntegrationDataResult generateIntegrationResult(IntegrationContext proxy, String rawIntegration, TextProvider provider) {
        logger.debug("Context: {}", proxy);
        ModernIntegrationDataResult result = new ModernIntegrationDataResult(proxy);
        @SuppressWarnings("unused")
        ModernDataIntegrationWorkbook workbook = new ModernDataIntegrationWorkbook(provider, result, rawIntegration);
        createIntegrationTempTable(proxy, provider);
        populateTempInterationTable(proxy);
        applyOntologyMappings(proxy);
        extractIntegationResults(result);
        return result;
    }

    /**
     * Runs the "final" select for the integration result that allows us to sort and extract records from the temporary table
     * 
     * @param result
     */
    @SuppressWarnings("deprecation")
    private void extractIntegationResults(final ModernIntegrationDataResult result) {
        selectAllFromTable(result.getIntegrationContext().getTempTable(), new ResultSetExtractor<Object>() {

            @Override
            public Object extractData(ResultSet arg0) throws SQLException, DataAccessException {
                ModernDataIntegrationWorkbook workbook = result.getWorkbook();
                workbook.setResultSet(arg0);
                workbook.setContext(result.getIntegrationContext());
                workbook.generate();
                return null;
            }
        });
    }

    /**
     * For each integration column in the context, iterate through each and find the values that are actually mapped in the data as opposed to unmapped columns
     * (i.e. no data). We then take those mappings and translate them into "update" statements that go into one of the extra columns in the temp table. We also
     * use the moment to set the import sort order that we have specified in the ontology and put it into the "third" extra column.
     * 
     * @param proxy
     */
    private void applyOntologyMappings(final IntegrationContext proxy) {
        /*
         * instead of doing this, consider creating a separate lookup table for value -> mapped value
         * then do update bound on those values
         */
        for (IntegrationColumn integrationColumn : proxy.getIntegrationColumns()) {
            if (!integrationColumn.isIntegrationColumn()) {
                continue;
            }

            DataTableColumn column = integrationColumn.getTempTableDataTableColumn();
            Map<OntologyNode, Map<Set<String>, List<Long>>> map = new HashMap<>();
            List<OntologyNode> filteredOntologyNodes = integrationColumn.getFilteredOntologyNodes();
            sortLeafToRoot(filteredOntologyNodes);

            // don't reuse nodes
            Set<OntologyNode> ignoreNodes = new HashSet<>();
            for (OntologyNode userChosenNode : filteredOntologyNodes) {
                logger.debug(" -- {} {}", userChosenNode, userChosenNode.getNumberOfParents());
                // only add to the "seen nodes" when we're done with a node
                Set<OntologyNode> seenNodes = new HashSet<>();
                for (DataTableColumn actualColumn : integrationColumn.getColumns()) {
                    // Map<DataTable,Set<String>> tableNodeSetMap = new HashMap();
                    // do these need to be per-table-updates?
                    Set<String> nodeSet = new HashSet<>(actualColumn.getMappedDataValues(userChosenNode));
                    seenNodes.add(userChosenNode);
                    // tableNodeSetMap.put(actualColumn.getDataTable(), nodeSet);
                    // check parent mapping logic to make sure that we don't apply to the grantparent if multiple nodes in tree are selected
                    findAllChildNodesThatHaventBeenSeen(integrationColumn, filteredOntologyNodes, ignoreNodes, userChosenNode, seenNodes, actualColumn,
                            nodeSet);

                    if (CollectionUtils.isEmpty(nodeSet)) {
                        continue;
                    }
                    // keep track of the nodes in a map Node --> Mapping --> [tableIds]
                    // only re-use when the node + mappings are 100% the same
                    map.putIfAbsent(userChosenNode, new HashMap<>());
                    map.get(userChosenNode).putIfAbsent(nodeSet, new ArrayList<>());
                    map.get(userChosenNode).get(nodeSet).add(actualColumn.getDataTable().getId());
                }
                ignoreNodes.addAll(seenNodes);
            }

            for (Entry<OntologyNode, Map<Set<String>, List<Long>>> entry : map.entrySet()) {
                OntologyNode node = entry.getKey();
                for (Entry<Set<String>, List<Long>> vals : entry.getValue().entrySet()) {
                    List tableIds = vals.getValue();
                    Set<String> nodeSet = vals.getKey();
                    WhereCondition whereCond = new WhereCondition(column.getName());
                    WhereCondition tableCond = new WhereCondition(INTEGRATION_TABLE_NAME_COL);

                    tableCond.setInValues(tableIds);

                    whereCond.getInValues().addAll(nodeSet);
                    whereCond.setIncludeNulls(false);
                    StringBuilder sb = new StringBuilder("UPDATE ");
                    sb.append(proxy.getTempTableName());
                    sb.append(" SET ").append(quote(column.getName() + INTEGRATION_SUFFIX)).append("=").append(quote(node.getDisplayName(), false));
                    sb.append(" WHERE ");
                    sb.append(tableCond.toSql());
                    sb.append(" AND ");
                    sb.append(whereCond.toSql());
                    executeUpdateOrDelete(sb.toString());

                }
            }
        }
    }

    private void sortLeafToRoot(List<OntologyNode> filteredOntologyNodes) {
        // sort by the most parents first
        filteredOntologyNodes.sort(new Comparator<OntologyNode>() {
            @Override
            public int compare(OntologyNode o1, OntologyNode o2) {
                if (o1.getNumberOfParents() > o2.getNumberOfParents()) {
                    return -1;
                }

                if (o1.getNumberOfParents() < o2.getNumberOfParents()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void findAllChildNodesThatHaventBeenSeen(IntegrationColumn integrationColumn, List<OntologyNode> filteredOntologyNodes,
            Set<OntologyNode> ignoreNodes,
            OntologyNode userChosenNode, Set<OntologyNode> seenNodes, DataTableColumn actualColumn, Set<String> nodeSet) {
        for (OntologyNode node_ : integrationColumn.getOntologyNodesForSelect()) {
            if (node_.isChildOf(userChosenNode) && !filteredOntologyNodes.contains(node_) && !ignoreNodes.contains(node_)) {
                seenNodes.add(node_);
                nodeSet.addAll(actualColumn.getMappedDataValues(node_));
            }
        }
    }

    /**
     * Dump data into the Temp Table for Integration
     * 
     * @param proxy
     */
    private void populateTempInterationTable(final IntegrationContext proxy) {
        for (DataTable table : proxy.getDataTables()) {
            generateModernIntegrationResult(proxy, table);
        }
    }

    /**
     * Creates a temporary table for the integration with extra (internal) columns for the Mapped Columns and Sort Columns
     * 
     * @param proxy
     * @return
     */
    private DataTable createIntegrationTempTable(final IntegrationContext proxy, TextProvider provider) {
        final DataTable tempTable = new DataTable();
        tempTable.setName(proxy.getTempTableName());
        createTable(String.format("CREATE TEMPORARY TABLE %1$s (" + DataTableColumn.TDAR_ID_COLUMN + " bigserial)", tempTable.getName()));
        DataTableColumn tableColumn = new DataTableColumn();
        tableColumn.setName(INTEGRATION_TABLE_NAME_COL);
        executeUpdateOrDelete(String.format(ADD_NUMERIC_COLUMN, tempTable.getName(), tableColumn.getName()));
        executeUpdateOrDelete(String.format("create index on \"%s\" (\"%s\")", tempTable.getName(), tableColumn.getName()));
        tempTable.getDataTableColumns().add(tableColumn);
        tableColumn.setDisplayName(provider.getText("dataIntegrationWorkbook.data_table"));

        proxy.setTempTable(tempTable);
        Set<String> seen = new HashSet<>();
        for (IntegrationColumn column : proxy.getIntegrationColumns()) {
            logger.debug("column: {}", column);
            if (StringUtils.isNotBlank(column.getName())) {
                DataTableColumn dtc = new DataTableColumn();
                dtc.setDisplayName(column.getName());
                int i = 0;
                String name = column.getName();
                String name_ = column.getName();
                while (seen.contains(name_)) {
                    i++;
                    name_ = name + Integer.toString(i);
                }
                seen.add(name_);
                name = name_;
                dtc.setName(normalizeTableOrColumnNames(name));
                name = dtc.getName();
                tempTable.getDataTableColumns().add(dtc);
                column.setTempTableDataTableColumn(dtc);
                executeUpdateOrDelete(String.format(ADD_COLUMN, tempTable.getName(), dtc.getName()));
                String pretty = dtc.getPrettyDisplayName();
                if (column.isIntegrationColumn()) {
                    pretty += "(" + column.getSharedOntology().getId() + ")";
                    dtc.setDisplayName(provider.getText("dataIntegrationWorkbook.data_original_value", Arrays.asList(pretty)));

                    DataTableColumn integrationColumn = new DataTableColumn();
                    integrationColumn.setDisplayName(dtc.getDisplayName() + " " + i);
                    integrationColumn.setName(name + INTEGRATION_SUFFIX);

                    integrationColumn.setDisplayName(provider.getText("dataIntegrationWorkbook.data_mapped_value", Arrays.asList(pretty)));
                    tempTable.getDataTableColumns().add(integrationColumn);
                    executeUpdateOrDelete(String.format(ADD_COLUMN, tempTable.getName(), integrationColumn.getName()));
                    // dtc.setDisplayName(integrationColumn.getDisplayName());
                }
            }
        }
        return tempTable;
    }

    /**
     * Populate the Temporary Integration Table for the specified DataTable and integration Context. This expects that the temp table has been built and just
     * handles the "insert"
     * 
     * @param proxy
     * @param table
     */
    private void generateModernIntegrationResult(final IntegrationContext proxy, final DataTable table) {
        StringBuilder sb = new StringBuilder();
        joinListWithCommas(sb, proxy.getTempTable().getColumnNames(), true);
        String selectSql = "INSERT INTO " + proxy.getTempTableName() + " ( " + sb.toString() + ") " + generateOntologyEnhancedSelect(table, proxy);

        executeUpdateOrDelete(selectSql);
    }

    /**
     * Builds the complex select statement to get the Integration contents from the specified DataTable. It needs to pull out integration columns
     * multiple times so that we handle sorting and mapping properly.
     * 
     * @param table
     * @param proxy
     * @return
     */
    private String generateOntologyEnhancedSelect(DataTable table, IntegrationContext proxy) {
        SqlSelectBuilder builder = new SqlSelectBuilder();
        // FOR EACH COLUMN, grab the value, for the table or use '' to keep the spacing correct
        builder.setStringSelectValue(table.getId().toString());
        for (IntegrationColumn integrationColumn : proxy.getIntegrationColumns()) {
            boolean isIntColumn = integrationColumn.isIntegrationColumn();
            DataTableColumn column = integrationColumn.getColumnForTable(table);
            logger.trace("table: {} column: {} type: {} ", table, column, isIntColumn);
            if (column == null) {
                builder.getColumns().add(null);
                if (isIntColumn) {
                    builder.getColumns().add(null);
                }
            } else {
                builder.getColumns().add(column.getName());
                // pull the column name twice if an integration column so we have mapped and unmapped values, and sort
                if (isIntColumn) {
                    logger.debug("Is integration column");
                    builder.getColumns().add(column.getName());

                    WhereCondition cond = new WhereCondition(column.getName());
                    cond.setInComment("mapped values");
                    for (OntologyNode node : integrationColumn.getOntologyNodesForSelect()) {
                        cond.getInValues().addAll(column.getMappedDataValues(node));
                    }
                    boolean includeUnmapepdValues = true;
                    boolean includeUncodedValues = true;

                    if (includeUnmapepdValues) {
                        cond.setMoreInComment("unmapped values");
                        cond.getMoreInValues().addAll(column.getUnmappedDataValues());
                    }

                    if (includeUncodedValues) {
                        cond.addOrLikeValue(Database.NO_CODING_SHEET_VALUE + "%");
                    }

                    boolean nullIncluded = integrationColumn.isNullIncluded();
                    // if we don't include the null, then we're not limiting when a column has no integration values. This is important if you have
                    // 3 integration columns and two have mapped values, and the third doesn't.

                    // toggle for TDAR-5161
                    boolean oldWay = false;

                    if (oldWay) {
                        if (cond.getInValues().isEmpty()) {
                            continue;
                        }
                    } else {
                        if (cond.getInValues().isEmpty() && !nullIncluded) {
                            continue;
                        }
                    }

                    if (integrationColumn.isNullIncluded()) {
                        cond.setIncludeNulls(true);
                    }

                    builder.getWhere().add(cond);
                }
            }
        }
        builder.getTableNames().add(table.getName());

        builder.evaluateWhereForEmpty();
        return builder.toSql();
    }

}
