package org.tdar.core.service.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.utils.MessageHelper;

public class IntegrationWorkflowValidationITCase extends AbstractIntegrationTestCase {


    @Autowired
    private GenericDao genericDao;

    @Autowired
    private SerializationService serializationService;

    private static final String DIR = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/";
    private String testJson = DIR + "test-integration.json";
    private String badTable = DIR + "test-bad-table.json";
    private String badDataTableColumn = DIR + "test-bad-data-table-column.json";
    private String badNode = DIR + "test-bad-node.json";
    private String badOntology = DIR + "test-bad-ontology.json";
    private String missmatchedNode = DIR + "test-node-to-ontology.json";
    private String missmatchedColumn = DIR + "test-col-to-table.json";
    private String missmatchedColumnToOntology = DIR + "test-column-to-ontology.json";

    @Test
    public void testValidIntegrationWorkflowData() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(testJson);
        assertFalse(workflowData.hasErrors());
    }

    @Test
    public void testBadTable() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(badTable);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.DATA_TABLE).size() > 0);
    }

    @Test
    public void testBadDataTableColumn() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(badDataTableColumn);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.DATA_TABLE_COLUMN).size() > 0);
    }

    @Test
    public void testBadNode() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(badNode);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.NODE).size() > 0);
    }

    @Test
    public void testBadOntology() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(badOntology);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.ONTOLOGY).size() > 0);
    }

    @Test
    public void testMissmatchedNode() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(missmatchedNode);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.NODE).size() > 0);
    }

    @Test
    public void testMissmatchedColumn() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(missmatchedColumn);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.DATA_TABLE_COLUMN).size() > 0);
    }

    @Test
    public void testMissmatchedColumnToOntology() throws IOException, IntegrationDeserializationException {
        IntegrationWorkflowData workflowData = loadJson(missmatchedColumnToOntology);
        assertTrue(workflowData.hasErrors());
        assertTrue(workflowData.getFieldErrors().get(IntegrationWorkflowData.DATA_TABLE_COLUMN).size() > 0);
    }

    private IntegrationWorkflowData loadJson(String json_) throws IOException, IntegrationDeserializationException {
        String json = FileUtils.readFileToString(new File(json_));
        DataIntegrationWorkflow workflow = new DataIntegrationWorkflow();
        workflow.setJsonData(json);
        IntegrationWorkflowData workflowData = serializationService.readObjectFromJson(workflow.getJsonData(), IntegrationWorkflowData.class);
        try {
            workflowData.validate(genericDao, MessageHelper.getInstance());
        } catch (IntegrationDeserializationException ie) {
            // We're eating the exception here because we're making assertions on the error objects on the JSON
            logger.error("IE: {}", ie);
        }
        return workflowData;
    }

}
