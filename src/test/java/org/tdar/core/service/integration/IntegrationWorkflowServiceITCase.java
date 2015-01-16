package org.tdar.core.service.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;

public class IntegrationWorkflowServiceITCase extends AbstractIntegrationTestCase {
    
    @Autowired  
    private IntegrationWorkflowService service;

    @Autowired  
    private SerializationService serializationService;

    private String testJson = "src/test/resources/data_integration_tests/test-integration.json";
    
    @Test
    public void testIntegrationWorkflowData() throws IOException, IntegrationDeserializationException {
        String json = FileUtils.readFileToString(new File(testJson));
        getLogger().debug(json);

        DataIntegrationWorkflow workflow = new DataIntegrationWorkflow();
        workflow.setJsonData(json);
        IntegrationWorkflowData workflowData = serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class);
        IntegrationContext context = service.toIntegrationContext(workflow);
//        getLogger().debug("{}", context.getDataTables());
//        getLogger().debug("{}", context.getIntegrationColumns());
        assertTrue(context.getDataTables().size() > 0);
        assertTrue(context.getIntegrationColumns().size() > 0);
        logger.debug("done convrsion");
        String json2 = serializationService.convertToJson(workflowData);
        logger.debug(json2);
    }

}
