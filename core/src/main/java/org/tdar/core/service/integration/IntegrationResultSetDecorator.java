package org.tdar.core.service.integration;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.model.abstracts.Database;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

/**
 * Wrapping an iterator for a ResultSet which is used by the workbook to write out the results. This wrapper does two separate things:
 * 1. it enables faceting or "pseudo" pivot table values for the result
 * 2. it extracts the previews of XX rows.
 *
 * @author abrin
 *
 */
public class IntegrationResultSetDecorator extends AbstractIteratorDecorator<Object[]> {

    private static final String OK = "OK";

    public IntegrationResultSetDecorator(Iterator<Object[]> iterator, IntegrationContext context) {
        super(iterator);
        this.context = context;
    }

    private IntegrationContext context;
    private Map<List<OntologyNode>, HashMap<Long, IntContainer>> pivot = new HashMap<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Object[] row;
    private List<Object[]> previewData = new ArrayList<>();
    private Map<Long, Integer> previewCount = new HashMap<>();

    @Override
    public Object[] next() {
        row = super.next();
        try {
            readRowToExcel();
        } catch (SQLException e) {
            logger.error("ex", e);
            throw new TdarRecoverableRuntimeException(e);
        }
        return row;
    }

    /**
     * Reads a row from the result set into the excel workbook(s)
     * 
     * @throws SQLException
     */
    private void readRowToExcel() throws SQLException {
        int resultSetPosition = 1;
        List<String> values = new ArrayList<>();
        // the list off ontology nodes, in order becomes the "key" for the pivot table
        List<OntologyNode> ontologyNodes = new ArrayList<>();
        // first column is the table name
        Long tableId = (Long) row[0];
        String tableName = "";
        DataTable table = null;
        for (DataTable dt : context.getDataTables()) {
            if (tableId.equals(dt.getId())) {
                table = dt;
                tableName = ModernDataIntegrationWorkbook.formatTableName(dt);
            }
        }
        if (StringUtils.isEmpty(tableName)) {
            logger.warn("Table Name is not defined for: " + tableId);
        }
        values.add(tableName);
        Double countVal = 1d;
        for (IntegrationColumn integrationColumn : context.getIntegrationColumns()) {
            // note SQL iterator is 1 based; java iterator is 0 based
            // DataTableColumn column = tempTable.getDataTableColumns().get(resultSetPosition);
            String value = initializeDefaultValue(resultSetPosition);
            final String rawStringValue = (String) row[resultSetPosition];
            DataTableColumn realColumn = integrationColumn.getColumnForTable(table);

            // set count value if needed
            if (integrationColumn.isCountColumn()) {
                // we go back to original version as initialized value may have been set
                value = rawStringValue;
                // if we're mapping to a "count" column that doesn't exist (e.g. a table where each row has an implict count of 1) then force to be 1.
                if (realColumn == null) {
                    countVal = 1d;
                } else {
                    try {
                        countVal = NumberFormat.getInstance().parse(rawStringValue).doubleValue();
                    } catch (ParseException nfe) {
                        logger.trace("numberParseIssue", nfe);
                        countVal = null;
                        value = MessageHelper.getMessage("database.bad_count_column");
                    }
                }
            }

            // add value to the output array for display/count columns
            if (!integrationColumn.isIntegrationColumn()) {
                values.add(value);
            }

            // if it's an integration column, add mapped value as well
            if (integrationColumn.isIntegrationColumn()) { // MAPPED VALUE if not display column
                resultSetPosition = resultSetPosition + 1;
                // HANDLE NULL
                String mappedVal = (String) row[resultSetPosition];
                String type = OK;
                if (StringUtils.isBlank(value)) {
                    value = MessageHelper.getMessage("database.null_empty_integration_value");
                    if (TdarConfiguration.getInstance().includeSpecialCodingRules()) {
                        mappedVal = CodingRule.NULL.getTerm();
                        type = CodingRule.NULL.getTerm();
                    }
                }

                if (TdarConfiguration.getInstance().includeSpecialCodingRules()) {
                    // HANDLE MISSING
                    if (StringUtils.contains(value, Database.NO_CODING_SHEET_VALUE)) {
                        mappedVal = CodingRule.MISSING.getTerm();
                        type = CodingRule.MISSING.getTerm();
                    }
                }

                values.add(value);

                OntologyNode mappedOntologyNode = integrationColumn.getMappedOntologyNode(mappedVal, realColumn);
                // if we use the special rules
                if (TdarConfiguration.getInstance().includeSpecialCodingRules() && realColumn != null) {
                    Map<String, OntologyNode> nodeMap = realColumn.getDefaultCodingSheet().getTermToOntologyNodeMap();
                    // if type "not" OK, ie. we need a special term
                    if (!StringUtils.equals(OK, type)) {
                        // if type == NULL || MISSING, try and get value:
                        mappedOntologyNode = nodeMap.get(mappedVal);
                    } else if (mappedOntologyNode == null) {
                        mappedVal = CodingRule.UNMAPPED.getTerm();
                        type = CodingRule.UNMAPPED.getTerm();
                        mappedOntologyNode = nodeMap.get(value);
                    }
                }

                if (mappedOntologyNode != null && StringUtils.isNotBlank(mappedOntologyNode.getDisplayName())) {
                    mappedVal = mappedOntologyNode.getDisplayName();
                    ontologyNodes.add(mappedOntologyNode);
                } else {
                    ontologyNodes.add(OntologyNode.NULL);
                }
                if (mappedVal == null) {
                    mappedVal = MessageHelper.getMessage("database.null_empty_mapped_value");
                }
                row[resultSetPosition] = mappedVal;
                values.add(mappedVal);

                if (PersistableUtils.isNotNullOrTransient(mappedOntologyNode)) {
                    values.add(mappedOntologyNode.getImportOrder().toString());
                } else {
                    values.add(null);
                }
                values.add(type);
            }
            resultSetPosition++;
        }
        row = values.toArray();
        logger.trace("{}", StringUtils.join(row, "|"));

        buildPivotDataForRow(ontologyNodes, tableId, countVal);
        extractPreviewContents(tableId, values);
    }

    /**
     * Take the list of ontology nodes, the table, and countValue if there is a count column and build a pivot table for the reuslts.
     */
    private void buildPivotDataForRow(List<OntologyNode> ontologyNodes, Long tableId, Double countVal) {
        HashMap<Long, IntContainer> pivotVal = getPivot().get(ontologyNodes);
        if (pivotVal == null) {
            pivotVal = new HashMap<Long, IntContainer>();
            getPivot().put(ontologyNodes, pivotVal);
        }
        IntContainer groupCount = pivotVal.get(tableId);
        if (groupCount == null) {
            groupCount = new IntContainer();
        }

        if (countVal != null) {
            if (countVal.intValue() == 1) {
                groupCount.increment();
            } else {
                groupCount.add(countVal.intValue());
            }
        }
        pivotVal.put(tableId, groupCount);
    }

    private String initializeDefaultValue(int resultSetPosition) {
        String value;
        value = (String) row[resultSetPosition];
        if (StringUtils.isBlank(value)) {
            value = "";
            row[resultSetPosition] = value;
        }
        return value;
    }

    private void extractPreviewContents(Long tableId, List<String> values) {
        int rowCount = 0;
        if (previewCount.containsKey(tableId)) {
            rowCount = previewCount.get(tableId);
        }
        if (rowCount < TdarConfiguration.getInstance().getIntegrationPreviewSizePerDataTable()) {
            rowCount++;
            logger.trace("{} {}{}", row);
            getPreviewData().add(values.toArray(new String[0]));
            previewCount.put(tableId, new Integer(rowCount));
        }
    }

    public Map<List<OntologyNode>, HashMap<Long, IntContainer>> getPivot() {
        return pivot;
    }

    public List<Object[]> getPreviewData() {
        return previewData;
    }

}
