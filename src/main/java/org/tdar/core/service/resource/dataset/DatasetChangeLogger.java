package org.tdar.core.service.resource.dataset;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;

public class DatasetChangeLogger implements Serializable {

    private static final long serialVersionUID = 3875860740738869060L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    Map<Long, String> colkeys = new HashMap<>();
    Map<Long, String> tabkeys = new HashMap<>();
    boolean noOp = false;

    public DatasetChangeLogger(Dataset dataset) {
        if (datasetIsNullOrEmpty(dataset)) {
            noOp = true;
            return;
        }
        extractKeys(dataset, colkeys, tabkeys);
    }

    private boolean datasetIsNullOrEmpty(Dataset dataset) {
        return dataset == null || CollectionUtils.isEmpty(dataset.getDataTables());
    }

    public void compare(Dataset dataset) {
        if (noOp) {
            logger.debug("no-op; inital dataset was null or empty");
            return;
        }
        if (datasetIsNullOrEmpty(dataset)) {
            logger.debug("second dataset was null or empty");
            return;
        }

        Map<Long, String> colkeys_ = new HashMap<>();
        Map<Long, String> tabkeys_ = new HashMap<>();
        extractKeys(dataset, colkeys_, tabkeys_);
        logReimportChanges(dataset, colkeys, tabkeys, colkeys_, tabkeys_);
    }

    private void logReimportChanges(Dataset dataset, Map<Long, String> colkeys, Map<Long, String> tabkeys, Map<Long, String> colkeys_,
            Map<Long, String> tabkeys_) {
        Collection<Long> disjunctTab = CollectionUtils.disjunction(tabkeys.keySet(), tabkeys_.keySet());
        Collection<Long> disjunctCol = CollectionUtils.disjunction(colkeys.keySet(), colkeys_.keySet());
        if (CollectionUtils.isNotEmpty(disjunctTab) || CollectionUtils.isNotEmpty(disjunctCol)) {
            logger.warn("|======= TABLES/COLS CHANGED FOR {} ===========|", dataset.getId());
            logger.warn("| {}", dataset.getTitle());
            if (CollectionUtils.isNotEmpty(disjunctTab)) {
                logger.warn("| DROPPED/ADDED TABLES:");
                logChange(tabkeys, tabkeys_, disjunctTab);
            }
            if (CollectionUtils.isNotEmpty(disjunctCol)) {
                logger.warn("| DROPPED/ADDED COLUMNS:");
                logChange(colkeys, colkeys_, disjunctCol);
            }
            logger.warn("|==============================================|");
        }
    }

    // col1text - VARCHAR -1, col2ints - BIGINT -1, col3ints - BIGINT -1, col4text - VARCHAR -1, col_mapping - VARCHAR -1
    // col1text - VARCHAR 32818, col2ints - BIGINT 32819, col3ints - BIGINT 32820, col4text - VARCHAR 32821, mapping2 - VARCHAR 32823
    private void logChange(Map<Long, String> tabkeys, Map<Long, String> tabkeys_, Collection<Long> disjunctTab) {
        for (Long dis : disjunctTab) {
            String chg = tabkeys.get(dis);
            if (chg == null) {
                chg = tabkeys_.get(dis);
            }
            logger.warn("| id: {} [{}]", dis, chg);
        }
    }

    private void extractKeys(Dataset dataset, Map<Long, String> dtkeys, Map<Long, String> dskeys) {
        for (DataTable table : dataset.getDataTables()) {
            dskeys.put(table.getId(), table.getName() + " | " + table.getDisplayName());
            for (DataTableColumn col : table.getDataTableColumns()) {
                String value = col.getName() + " | " + col.getDisplayName() + "[" + col.getColumnEncodingType() + "]";
                if (col.getColumnEncodingType() == DataTableColumnEncodingType.CODED_VALUE) {
                    value += " : " + col.getDefaultCodingSheet();
                }
                dtkeys.put(col.getId(), value);
            }
        }
    }
}
