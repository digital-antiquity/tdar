package org.tdar.core.service.integration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;

public class IntegrationWorkflowServiceITCase extends AbstractIntegrationTestCase {
    
    @Autowired  
    private IntegrationWorkflowService service;
    
    private String testJson = "src/test/resources/data_integration_tests/test-integration.json";
    
    @Test
    public void testIntegrationWorkflowData() throws IOException {
        String json = FileUtils.readFileToString(new File(testJson));
        getLogger().debug(json);
        DataIntegrationWorkflow workflow = new DataIntegrationWorkflow();
        workflow.setJsonData(json);
        IntegrationContext context = service.toIntegrationContext(workflow);
        getLogger().debug("{}", context.getDataTables());
        getLogger().debug("{}", context.getIntegrationColumns());
    }

}
