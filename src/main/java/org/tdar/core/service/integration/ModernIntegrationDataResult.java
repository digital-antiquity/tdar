package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.struts.data.IntegrationContext;

/**
 * $Id$
 * 
 * A result object for the integrated data. Specific to a single dataset.
 * 
 * @author <a href='mailto:Adam.Brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
public class ModernIntegrationDataResult implements Serializable {

    private static final long serialVersionUID = 3466986630097086581L;
    private IntegrationContext integrationContext;
    private ModernDataIntegrationWorkbook workbook;
    private Map<List<OntologyNode>, HashMap<String, IntContainer>> pivotData;
    private List<Object[]> previewData;
    
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

    public void setPivotData(Map<List<OntologyNode>, HashMap<String, IntContainer>> pivot) {
        this.pivotData = pivot;
    }
    
    public Map<List<OntologyNode>, HashMap<String, IntContainer>> getPivotData() {
        return pivotData;
    }

    public void setPreviewData(List<Object[]> previewData) {
        this.previewData = previewData;
    }

    public List<Object[]> getPreviewData() {
        return previewData;
    }
}
