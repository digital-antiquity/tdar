package org.tdar.struts.action.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationColumnPartProxy;
import org.tdar.core.dao.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.search.DatasetSearchFilter;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.dto.IntegrationDeserializationException;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.api.integration.IntegrationAction;
import org.tdar.struts.action.api.integration.IntegrationColumnDetailsAction;
import org.tdar.struts.action.api.integration.NodeParticipationByColumnAction;
import org.tdar.struts.action.api.integration.TableDetailsAction;
import org.tdar.struts.action.workspace.AngularIntegrationAction;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class DataIntegrationAjaxITCase extends AbstractControllerITCase {

    private static final String TEST_PATH = TestConstants.TEST_ROOT_DIR + "/data_integration_tests/json/";

    @Autowired
    DataTableService dataTableService;

    @Autowired
    OntologyService ontologyService;

    @Autowired
    private SerializationService serializationService;

    public static final Long SPITAL_MAIN_ID = 3091L;
    public static final Long TAXON_ID = 42940L;

    @Test
    public void testDatasetService() throws IOException {
        DatasetSearchFilter filter = new DatasetSearchFilter();
        filter.setAuthorizedUser(getUser());
        IntegrationDataTableSearchResult findDataTables = dataTableService.findDataTables(filter);
        logger.debug(serializationService.convertToFilteredJson(findDataTables, JsonIntegrationFilter.class));
    }

    @Test
    public void testOntologyService() throws IOException {
        OntologySearchFilter filter = new OntologySearchFilter();
        filter.setAuthorizedUser(getUser());
        IntegrationOntologySearchResult findOntologies = ontologyService.findOntologies(filter);
        logger.debug(serializationService.convertToFilteredJson(findOntologies, JsonIntegrationFilter.class));
    }

    @Test
    public void testDataTableDetailsAction() throws IOException {
        TableDetailsAction detailsAction = generateNewInitializedController(TableDetailsAction.class, getAdminUser());
        detailsAction.setDataTableIds(Arrays.asList(SPITAL_MAIN_ID));
        detailsAction.dataTableDetails();
        logger.debug(IOUtils.toString(detailsAction.getJsonInputStream()));
    }

    @Test
    public void testIntegrationColumnDetailsAction() throws IOException {
        IntegrationColumnDetailsAction detailsAction = generateNewInitializedController(IntegrationColumnDetailsAction.class, getAdminUser());
        IntegrationColumn column = new IntegrationColumn(ColumnType.INTEGRATION);
        column.getColumns().add(dataTableService.find(SPITAL_MAIN_ID).getColumnByName("species_common_name"));
        column.setSharedOntology(ontologyService.find(TAXON_ID));
        detailsAction.setIntegrationColumn(column);
        detailsAction.integrationColumnDetails();
        logger.debug(IOUtils.toString(detailsAction.getJsonInputStream()));
    }

    @Autowired
    GenericService genericService;

    @Test
    public void testCatVariableJson() throws TdarActionException {
        AngularIntegrationAction ia = generateNewInitializedController(AngularIntegrationAction.class, getAdminUser());
        ia.prepare();
        ia.execute();
        logger.debug(ia.getCategoriesJson());
    }

    @Test
    public void testNodeParticipation() throws IOException {
        NodeParticipationByColumnAction action;
        // get all the mapped dataTableColumns
        action = generateNewController(NodeParticipationByColumnAction.class);
        List<DataTableColumn> dataTableColumns = new ArrayList<>(genericService.findAll(DataTableColumn.class));
        List<Long> dtcIds = new ArrayList<>();
        for (DataTableColumn dtc : dataTableColumns) {
            if (dtc.getMappedOntology() != null) {
                dtcIds.add(dtc.getId());
            }
        }

        action.getDataTableColumnIds().addAll(dtcIds);
        action.prepare();
        action.execute();
        logger.debug("results:{}", action.getIntegrationColumnPartProxies());
        logger.debug(IOUtils.toString(action.getJsonInputStream()));
        // we expect to have at least one node value present
        int nodesPresent = 0;
        int proxiesPresent = 0;
        for (IntegrationColumnPartProxy proxy : action.getIntegrationColumnPartProxies()) {
            nodesPresent += proxy.getFlattenedNodes().size();
            proxiesPresent++;
        }
        logger.debug("nodesPresent:{}", nodesPresent);
        // NOTE: this was 184 prior to the change to the filter node logic (reversion to older logic) in r6881.
        // 20-04-15 -- changed nodes in database, so, changing logic to match
        Assert.assertEquals(177, nodesPresent);
        Assert.assertEquals(4, proxiesPresent);

    }

    @Test
    public void testIntegrationAction() throws IOException, IntegrationDeserializationException {
        StringWriter writer = runJson(TEST_PATH + "sample-valid-integration.json");
        logger.debug(writer.toString());
    }

    private String testDupJson = TEST_PATH + "test-integration-duplicate-display.json";

    @Test
    public void testIntegrationActionDupColumns() throws IOException, IntegrationDeserializationException {
        StringWriter writer = runJson(testDupJson);
        logger.debug(writer.toString());
    }

    
    @Test
    public void testEmptyCountColumn() throws IOException, IntegrationDeserializationException {
        StringWriter writer = runJson(TEST_PATH + "test-empty-count-column.json");
        logger.debug(writer.toString());
    }

    
    @Test
    @Rollback(true)
    public void testRelaxedIntegration() throws IOException, IntegrationDeserializationException {
        Dataset dataset = genericService.find(Dataset.class, 42990L);
        DataTable table = dataset.getDataTables().iterator().next();
        DataTableColumn column = table.getColumnByName("taxon");
        CodingSheet codingsheet = column.getDefaultCodingSheet();
        codingsheet.setDefaultOntology(null);
        genericService.saveOrUpdate(dataset, table, column, codingsheet);
        StringWriter writer = runJson(TEST_PATH + "test-relaxed-intgration.json");
        logger.debug(writer.toString());
    }

    private StringWriter runJson(String jsonFilename) throws IOException, JsonParseException, JsonMappingException, IntegrationDeserializationException {
        IntegrationAction action = generateNewInitializedController(IntegrationAction.class, getAdminUser());
        String integration = FileUtils.readFileToString(new File(jsonFilename));
        action.setIntegration(integration);
        action.integrate();
        StringWriter writer = new StringWriter();
        IOUtils.copyLarge(new InputStreamReader(action.getJsonInputStream()), writer);
        return writer;
    }

    private String testJson = TEST_PATH + "selenium-integrate.json";

    @Test
    public void testIntegrationParllelToSelenium() throws JsonParseException, JsonMappingException, IOException, IntegrationDeserializationException {
        StringWriter writer = runJson(testJson);
        logger.debug(writer.toString());
        assertTrue(writer.toString().contains("rabbit"));
    }
}
