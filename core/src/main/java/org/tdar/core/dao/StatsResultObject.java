package org.tdar.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an aggregate statistics query result.
 * 
 * @author abrin
 *
 */
public class StatsResultObject implements Serializable {

    private static final long serialVersionUID = 4215287793431994744L;

    private List<String> rowLabels = new ArrayList<>();
    private List<ResourceStatWrapper> rowData = new ArrayList<>();
    private long[] totals;
    private long[] totalBots;
    private long[] totalDownloads;
    private List<String> keys = new ArrayList<>();
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean applyKeys = true;

    public void addRowData(ResourceStatWrapper row) {
        getRowData().add(row);
        List<Number> counts = row.getData();
        int third = counts.size() / 3;
        int two_thirds = third * 2;
        if (totals == null) {
            totals = new long[third];
            totalBots = new long[third];
            totalDownloads = new long[third];
        }

        if (!CollectionUtils.isEmpty(keys)) {
            applyKeys = false;
        }

        int count = 0;
        for (int i = 0; i < two_thirds - 1; i = i + 2) {
//            totalBots[count] = 0;
//            totals[count] = 0;

            int j = i + 1;
            Number iNum = counts.get(i);
            Number jNum = counts.get(j);

            if (iNum != null) {
                totalBots[count] += iNum.longValue();
            }
            if (jNum != null) {
                totals[count] += jNum.longValue();
            }
            String label = rowLabels.get(i);
            if (applyKeys) {
                keys.add(StringUtils.substringBefore(label, " "));
            }
            count++;
        }
        count = 0;
        for (int i = two_thirds; i < counts.size(); i++) {
//            totalDownloads[count] = 0;
            Number dNum = counts.get(i);
            if (dNum != null) {
                totalDownloads[count] += dNum.longValue();
            }
            count++;
        }
    }

    public List<String> getRowLabels() {
        return rowLabels;
    }

    public void setRowLabels(List<String> rowLabels) {
        this.rowLabels = rowLabels;
    }

    public List<ResourceStatWrapper> getRowData() {
        return rowData;
    }

    public void setRowData(List<ResourceStatWrapper> rowData) {
        this.rowData = rowData;
    }

    public Collection<Map<String, Object>> getObjectForJson() {
        debug();
        List<Map<String, Object>> toReturn = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            HashMap<String, Object> value = new HashMap<>();
            value.put("Views (Total)", totals[i]);
            value.put("Views", totals[i] - totalBots[i]);
            value.put("Views (Bots)", totalBots[i]);
            value.put("Downloads", totalDownloads[i]);
            value.put("date", key);
            toReturn.add(value);
        }
        return toReturn;
    }

    public void debug() {
        logger.debug("keys:{}", keys);
        logger.debug("totals:{}", totals);
        logger.debug("totalBots:{}", totalBots);
        logger.debug("totalDownloads:{}", totalDownloads);
    }

    public List<Long> getTotals() {
        ArrayList<Long> results = new ArrayList<>();
        for (long total : totals) {
            results.add(total);
        }
        return results;
    }

    public boolean empty() {
        return CollectionUtils.isEmpty(rowData);
    }

    public long[] getTotalBots() {
        return totalBots;
    }

    public void setTotalBots(long[] totalBots) {
        this.totalBots = totalBots;
    }

    public long[] getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(long[] totalDownloads) {
        this.totalDownloads = totalDownloads;
    }
}
