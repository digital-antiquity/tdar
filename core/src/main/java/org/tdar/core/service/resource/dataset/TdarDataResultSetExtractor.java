package org.tdar.core.service.resource.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

/*
 * Handles the extraction of a ResultSet from the tdardata database and converts it into a ResultsMetadataWrapper backed object that can be converted to JSON or XML
 */
public class TdarDataResultSetExtractor implements ResultSetExtractor<List<List<String>>> {
    private final ResultMetadataWrapper wrapper;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // start at record
    private final int start;
    // end at record
    private final int page;
    private final DataTable dataTable;
    /*
     * Should the tdar_id row be included or not
     */
    private boolean returnRowId = true;

    public TdarDataResultSetExtractor(ResultMetadataWrapper wrapper, int start, int page, DataTable dataTable, boolean returnRowId) {
        this.wrapper = wrapper;
        this.start = start;
        this.page = page;
        this.dataTable = dataTable;
        this.returnRowId = returnRowId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public List<List<String>> extractData(ResultSet rs) throws SQLException {
        List<List<String>> results = new ArrayList<List<String>>();
        int rowNum = 1;
        while (rs.next()) {
            Map<DataTableColumn, String> result = DatasetUtils.convertResultSetRowToDataTableColumnMap(dataTable, rs, returnRowId);
            if (rs.isFirst()) {
                wrapper.setFields(new ArrayList<DataTableColumn>(result.keySet()));

                // if we're returning the tDAR row id, then we add one to the list of fields
                List<DataTableColumn> columns = new ArrayList<>(dataTable.getDataTableColumns());

                // remove hidden columns
                Iterator<DataTableColumn> colIterator = columns.iterator();
                while (colIterator.hasNext()) {
                    if (!colIterator.next().isVisible()) {
                        colIterator.remove();
                    }
                }
                if (returnRowId) {
                    columns.add(DataTableColumn.TDAR_ROW_ID);
                }
                columns.removeAll(result.keySet());
                if (CollectionUtils.isNotEmpty(columns)) {
                    logger.error("mismatch column entries: {} [columns not referenced in DT: {}] ", dataTable, columns);
                }
            }

            if ((rowNum > start) && (rowNum <= (start + page))) {
                ArrayList<String> values = new ArrayList<String>();
                for (DataTableColumn col : wrapper.getFields()) {
                    if (col.isVisible() || (returnRowId && DataTableColumn.TDAR_ID_COLUMN.equals(col.getName()))) {
                        values.add(result.get(col));
                    }
                }
                results.add(values);
            }
            rowNum++;

            if (rs.isLast()) {
                wrapper.setTotalRecords(rs.getRow());
            }
        }
        return results;
    }

    /*
     * Should the tdar_id row be included or not
     */
    public boolean isReturnRowId() {
        return returnRowId;
    }

    /*
     * Should the tdar_id row be included or not
     */
    public void setReturnRowId(boolean returnRowId) {
        this.returnRowId = returnRowId;
    }
}
