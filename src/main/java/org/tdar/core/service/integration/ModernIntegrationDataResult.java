package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * A result object for the integrated data. Specific to a single dataset.
 * 
 * @author <a href='mailto:Adam.Brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
@JsonAutoDetect
public class ModernIntegrationDataResult implements Serializable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 3466986630097086581L;
    private IntegrationContext integrationContext;
    private ModernDataIntegrationWorkbook workbook;
    private Map<List<OntologyNode>, HashMap<Long, IntContainer>> pivotData;
    private List<Object[]> previewData;
    private PersonalFilestoreTicket ticket;

    public ModernIntegrationDataResult(IntegrationContext proxy) {
        this.setIntegrationContext(proxy);
    }

    public ModernDataIntegrationWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(ModernDataIntegrationWorkbook workbook) {
        this.workbook = workbook;
    }

    public IntegrationContext getIntegrationContext() {
        return integrationContext;
    }

    public void setIntegrationContext(IntegrationContext integrationContext) {
        this.integrationContext = integrationContext;
    }

    public void setPivotData(Map<List<OntologyNode>, HashMap<Long, IntContainer>> pivot) {
        this.pivotData = pivot;
    }

    public Map<List<OntologyNode>, HashMap<Long, IntContainer>> getRawPivotData() {
        return pivotData;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Map<String, HashMap<String, Integer>> getRawData() {
        HashMap<String, HashMap<String, Integer>> result = new HashMap<>();
        Map<Long, DataTable> idMap = PersistableUtils.createIdMap(integrationContext.getDataTables());
        for (Entry<List<OntologyNode>, HashMap<Long, IntContainer>> entrySet : pivotData.entrySet()) {
            HashMap<Long, IntContainer> value = entrySet.getValue();
            HashMap<String, Integer> val = new HashMap<String, Integer>();
            for (Long k : value.keySet()) {
                val.put(ModernDataIntegrationWorkbook.formatTableName(idMap.get(k)), value.get(k).getVal());
            }
            String key = "";
            for (OntologyNode node : entrySet.getKey()) {
                if (StringUtils.isNotEmpty(key)) {
                    key += "l ";
                }
                key += node.getDisplayName();
            }
            logger.debug("preview: {}", entrySet);
            result.put(key, val);
        }

        return result;
    }

    public void setPreviewData(List<Object[]> previewData) {
        this.previewData = previewData;
    }

    @JsonView(JsonIntegrationFilter.class)
    public List<Object[]> getPreviewData() {
        return previewData;
    }

    @JsonView(JsonIntegrationFilter.class)
    public PersonalFilestoreTicket getTicket() {
        return ticket;
    }

    public void setTicket(PersonalFilestoreTicket ticket) {
        this.ticket = ticket;
    }

    @JsonView(JsonIntegrationFilter.class)
    public List<String> getPreviewColumnLabels() {
        List<String> labels = new ArrayList<String>();
        for (DataTableColumn dtc : integrationContext.getTempTable().getDataTableColumns()) {
            labels.add(dtc.getDisplayName());
        }
        return labels;
    }

    @JsonView(JsonIntegrationFilter.class)
    public List<String> getPivotColumnLabels() {
        List<String> labels = new ArrayList<String>();
        for (IntegrationColumn col : integrationContext.getIntegrationColumns()) {
            if (col.isIntegrationColumn()) {
                labels.add(col.getName());
            }
        }
        for (DataTable table : integrationContext.getDataTables()) {
            labels.add(ModernDataIntegrationWorkbook.formatTableName(table));
        }
        return labels;
    }

}
