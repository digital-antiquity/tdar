package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.OntologyNode;
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
    
    public Map<List<OntologyNode>, HashMap<Long, IntContainer>> getPivotData() {
        getData();
        return pivotData;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Map<String,HashMap<Long, Integer>> getData() {
        HashMap<String, HashMap<Long,Integer>> result = new HashMap<>();
        for (Entry<List<OntologyNode>, HashMap<Long, IntContainer>> entrySet : pivotData.entrySet()) {
            HashMap<Long, IntContainer> value = entrySet.getValue();
            HashMap<Long, Integer> val = new HashMap<Long, Integer>();
            for (Long k : value.keySet()) {
                val.put(k, value.get(k).getVal());
            }
            String key = "";
            for (OntologyNode node : entrySet.getKey()) {
                if (StringUtils.isNotEmpty(key)) {
                    key += "l ";
                }
                key += node.getDisplayName();
            }
            logger.debug("preview: {}",entrySet);
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
}
