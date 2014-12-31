package org.tdar.core.service.integration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

/**
 * Wrapping an iterator for a ResultSet which is used by the workbook to write out the results. This wrapper does two separate things:
 * 1. it enables faceting or "pseudo" pivot table values for the result
 * 2. it extracts the previews of XX rows.
 *
 * @author abrin
 *
 */
public class IntegrationResultSetDecorator extends AbstractIteratorDecorator<Object[]> {

    public IntegrationResultSetDecorator(Iterator<Object[]> iterator, IntegrationContext context) {
        super(iterator);
        this.context = context;
    }

    private IntegrationContext context;
    private Map<List<OntologyNode>, HashMap<String, IntContainer>> pivot = new HashMap<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Object[] row;
    private List<Object[]> previewData = new ArrayList<>();
    private Map<String, Integer> previewCount = new HashMap<>();

    @Override
    public Object[] next() {
        row = super.next();
        try {
            readRowToExcel();
        } catch (Exception e) {
            logger.error("ex", e);
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
        String tableName = (String) row[0];
        values.add(tableName);
        int countVal = -1;
        for (IntegrationColumn integrationColumn : context.getIntegrationColumns()) {
            // note SQL iterator is 1 based; java iterator is 0 based
            // DataTableColumn column = tempTable.getDataTableColumns().get(resultSetPosition);
            String value = initializeDefaultValue(resultSetPosition);

            // add value to the output array
            values.add(value);

            // set count value if needed
            if (integrationColumn.isCountColumn()) {
                countVal = Integer.parseInt(value);
            }

            // if it's an integration column, add mapped value as well
            if (integrationColumn.isIntegrationColumn()) { // MAPPED VALUE if not display column
                resultSetPosition = resultSetPosition + 1;
                String mappedVal = (String) row[resultSetPosition];

                DataTableColumn realColumn = integrationColumn.getColumns().get(0);
                OntologyNode mappedOntologyNode = integrationColumn.getMappedOntologyNode(mappedVal, realColumn);
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
                // increment for "sort" column
                resultSetPosition++;
            }
            resultSetPosition++;
        }
        row = values.toArray();
        logger.trace("{}", StringUtils.join(row, "|"));

        buildPivotDataForRow(ontologyNodes, tableName, countVal);
        extractPreviewContents(tableName, values);
    }

    /**
     * Take the list of ontology nodes, the table, and countValue if there is a count column and build a pivot table for the reuslts.
     */
    private void buildPivotDataForRow(List<OntologyNode> ontologyNodes, String tableName, int countVal) {
        HashMap<String, IntContainer> pivotVal = getPivot().get(ontologyNodes);
        if (pivotVal == null) {
            pivotVal = new HashMap<String, IntContainer>();
            getPivot().put(ontologyNodes, pivotVal);
        }
        IntContainer groupCount = pivotVal.get(tableName);
        if (groupCount == null) {
            groupCount = new IntContainer();
        }

        if (countVal == -1) {
            groupCount.increment();
        } else {
            groupCount.add(countVal);
        }
        pivotVal.put(tableName, groupCount);
    }

    private String initializeDefaultValue(int resultSetPosition) {
        String value;
        value = (String) row[resultSetPosition];
        if (StringUtils.isBlank(value)) {
            value = MessageHelper.getMessage("database.null_empty_integration_value");
            row[resultSetPosition] = value;
        }
        return value;
    }

    private void extractPreviewContents(String tableName, List<String> values) {
        int rowCount = 0;
        if (previewCount.containsKey(tableName)) {
            rowCount = previewCount.get(tableName);
        }
        if (rowCount < TdarConfiguration.getIntegrationPreviewSizePerDataTable()) {
            rowCount++;
            logger.trace("{} {}{}", row);
            getPreviewData().add(values.toArray(new String[0]));
            previewCount.put(tableName, new Integer(rowCount));
        }
    }

    public Map<List<OntologyNode>, HashMap<String, IntContainer>> getPivot() {
        return pivot;
    }

    public void setPivot(Map<List<OntologyNode>, HashMap<String, IntContainer>> pivot) {
        this.pivot = pivot;
    }

    public List<Object[]> getPreviewData() {
        return previewData;
    }

    public void setPreviewData(List<Object[]> previewData) {
        this.previewData = previewData;
    }

}
