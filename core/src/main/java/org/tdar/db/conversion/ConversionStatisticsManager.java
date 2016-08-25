package org.tdar.db.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.db.conversion.analyzers.CharAnalyzer;
import org.tdar.db.conversion.analyzers.ColumnAnalyzer;
import org.tdar.db.conversion.analyzers.DateAnalyzer;
import org.tdar.db.conversion.analyzers.DoubleAnalyzer;
import org.tdar.db.conversion.analyzers.LongAnalyzer;

public class ConversionStatisticsManager {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<DataTableColumn, List<ColumnAnalyzer>> statistics = new HashMap<DataTableColumn, List<ColumnAnalyzer>>();

    public ConversionStatisticsManager(List<DataTableColumn> columns) {
        for (DataTableColumn column : columns) {
            // for each column, add the analyzers in order from most-to-least favored.
            List<ColumnAnalyzer> analyzers = new ArrayList<ColumnAnalyzer>();
            analyzers.add(new LongAnalyzer());
            analyzers.add(new DoubleAnalyzer());
            analyzers.add(new DateAnalyzer());
            analyzers.add(new CharAnalyzer());
            statistics.put(column, analyzers);
            logger.trace("adding " + analyzers + "to statistics for column " + column);
        }
    }

    // update the provided most-desired-datatype statistics based on the provided column and value. This method
    // modifies the provided map in that in removes desired-datatypelist items for the provided column if the provided value
    // cannot be converted to that datatype. Null values have no effect on the statistics
    public void updateStatistics(DataTableColumn column, String value, int rowNumber) {
        if (value == null) {
            return;
        }
        List<ColumnAnalyzer> analyzers = statistics.get(column);
        Iterator<ColumnAnalyzer> iter = analyzers.iterator();
        while (iter.hasNext()) {
            ColumnAnalyzer ca = iter.next();
            if (!ca.analyze(value, column, rowNumber)) {
                logger.trace("removing " + ca.getType() + " from list of potential conversion types for column " + column + "(value was: '" + value + "'");
                iter.remove();
            }
        }
    }

    public Map<DataTableColumn, List<ColumnAnalyzer>> getStatistics() {
        return statistics;
    }
}
