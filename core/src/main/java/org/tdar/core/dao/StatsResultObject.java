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
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void addRowData(ResourceStatWrapper row) {
        getRowData().add(row);
        List<Number> counts = row.getData();
        if (totals == null) {
            totals = new long[counts.size()];
        }
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) != null) {
                totals[i] += counts.get(i).longValue();
            }
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
    
    public Collection<Map<String,Object>> getObjectForJson() {
        Map<String, Map<String,Object>> map = new LinkedHashMap<>();
        
        LinkedHashSet<String> order = new LinkedHashSet<>();
        for (int i =0; i< rowLabels.size(); i++) {
            String[] v = rowLabels.get(i).split(" ");
            String key = v[0];
            if (!map.containsKey(key)) {
                map.put(key, new HashMap<>());
                order.add(key);
            }
            map.get(key).put("date", v[0]);
            if (v[1] ==null) {
                logger.error("v[1] is null {} {}",rowLabels.get(i), v.toString());
            } else {
                map.get(key).put(v[1], totals[i]);
            }
        }
        List<Map<String,Object>> toReturn = new ArrayList<>();
        for (String key : order) {
            toReturn.add(map.get(key));
        }
        return toReturn;
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
}
