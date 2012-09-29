package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractDataIntegrationTestCase;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.ReadUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.service.DataTableService;

/**
 * $Id$
 * 
 * Integration test over the DatasetController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class DatasetControllerITCase extends AbstractDataIntegrationTestCase {

    private static final String ALEXANDRIA_EXCEL_FILENAME = "qrybonecatalogueeditedkk.xls";
    private static final String BELEMENT_COL = "belement";

    @Autowired
    private DatasetController controller;
    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    private DataTableService dataTableService;

    @Before
    public void setUp() {
        controller.setSessionData(getSessionData());
    }

    @Test
    @Rollback
    public void testOntologyMappingCaseSensitivity() {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller.setResourceId(dataset.getId());
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setDefaultOntology(bElementOntology);
        elementColumn.setName(BELEMENT_COL);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        mapColumnsToDataset(dataset, dataTable, elementColumn);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        DataTableColumn column = dataTable.getColumnByName(BELEMENT_COL);
        loadResourceFromId(controller, dataTable.getDataset().getId());
        controller.setDataTableId(dataTable.getId());
        controller.setColumnId(column.getId());
        controller.loadOntologyMappedColumns();

        List<String> findAllDistinctValues = dataTableService.findAllDistinctValues(column);
        List<String> tibias = new ArrayList<String>();
        for (String distinct : findAllDistinctValues) {
            if (distinct.toLowerCase().contains("tibia"))
                tibias.add(distinct);
        }

        int tibia = -1;
        int Tibia = -1;
        List<String> suggestedTibias = new ArrayList<String>();
        int i = 0;
        for (String key : controller.getSuggestions().keySet()) {
            if (key.equals("Tibia")) {
                Tibia = i;
            }
            if (key.equals("tibia")) {
                tibia = i;
            }
            i++;
            if (key.toLowerCase().contains("tibia"))
                suggestedTibias.add(key);
        }

        assertEquals(tibias.size(), suggestedTibias.size());
        assertTrue(tibia != -1);
        assertTrue(Tibia != -1);
        assertTrue(tibia < Tibia);

        logger.info(suggestedTibias);
        Collections.sort(suggestedTibias);
        Collections.sort(tibias);
        assertEquals(tibias, suggestedTibias);
    }

    @Test
    public void testNullResourceOperations() throws Exception {
        List<String> successActions = Arrays.asList("add", "list");
        // grab all methods on DatasetController annotated with a conventions plugin @Action
        for (Method method : DatasetController.class.getMethods()) {
            if (method.isAnnotationPresent(Action.class)) {
                logger.debug("Invoking action method: " + method.getName());
                String result = (String) method.invoke(controller);
                if (successActions.contains(method.getName())) {
                    assertEquals("add() action should return success", DatasetController.SUCCESS, result);
                } else {
                    assertNotSame("shouldn't have any success results", DatasetController.SUCCESS, result);
                }
            }
        }
    }

    @Test
    @Rollback
    public void testFullUser() throws InstantiationException, IllegalAccessException {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        FullUser admin = new FullUser();
        admin.setResource(dataset);
        admin.setPerson(getAdminUser());
        genericService.save(admin);
        dataset.getFullUsers().add(admin);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, datasetId);
        List<Long> fullUserIds = new ArrayList<Long>();
        fullUserIds.add(getUserId());
        controller.setFullUserIds(fullUserIds);
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(1, dataset.getFullUsers().size());
        assertEquals(getUserId(), dataset.getFullUsers().iterator().next().getPerson().getId());
    }

    @Test
    @Rollback
    public void testReadUser() throws InstantiationException, IllegalAccessException {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        ReadUser admin = new ReadUser();
        admin.setResource(dataset);
        admin.setPerson(getAdminUser());
        genericService.save(admin);
        dataset.getReadUsers().add(admin);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, datasetId);
        List<Long> readUserIds = new ArrayList<Long>();
        readUserIds.add(getUserId());
        readUserIds.add(getAdminUserId());
        controller.setReadUserIds(readUserIds);
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(2, dataset.getReadUsers().size());
        Set<Long> seen = new HashSet<Long>();
        for (ReadUser r : dataset.getReadUsers()) {
            seen.add(r.getPerson().getId());
            assertTrue(r.getResource().equals(dataset));
        }
        seen.remove(getUserId());
        seen.remove(getAdminUserId());
        assertTrue("should have seen all user ids already", seen.isEmpty());
    }

    @Test
    @Rollback
    public void testReadUserEmpty() throws InstantiationException, IllegalAccessException {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class);
        Long datasetId = dataset.getId();
        ReadUser admin = new ReadUser();
        admin.setResource(dataset);
        admin.setPerson(getAdminUser());
        genericService.save(admin);
        dataset.getReadUsers().add(admin);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, datasetId);
        List<Long> readUserIds = new ArrayList<Long>();
        controller.setReadUserIds(readUserIds);
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(0, dataset.getReadUsers().size());
    }

    @Test
    public void testResourceUserCompareTo() {
        FullUser ru = new FullUser();
        ru.setPerson(getTestPerson());
        assertEquals(0, ru.compareTo(getTestPerson()));
        assertTrue(ru.compareTo(getAdminUser()) > 0);
        ru.setPerson(getAdminUser());
        assertTrue(ru.compareTo(getTestPerson()) < 0);
    }

    @Test
    @Rollback
    public void testDatasetReplaceSame() {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, dataset.getId());
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_EXCEL_FILENAME)));
        controller.setUploadedFilesFileName(Arrays.asList(ALEXANDRIA_EXCEL_FILENAME));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }

    @Test
    @Rollback
    public void testDatasetReplaceWithMappings() {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);

        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTable alexandriaTable = dataset.getDataTables().iterator().next();
        DataTableColumn elementColumn = alexandriaTable.getColumnByName(BELEMENT_COL);
        elementColumn.setDefaultOntology(bElementOntology);
        Long elementColumnId = elementColumn.getId();
        mapColumnsToDataset(dataset, alexandriaTable, elementColumn);
        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getElementValueMap(), bElementOntology);
        Map<String, Long> valueToOntologyNodeIdMap = elementColumn.getValueToOntologyNodeIdMap();
        elementColumn = null;
        loadResourceFromId(controller, dataset.getId());
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_EXCEL_FILENAME)));
        controller.setUploadedFilesFileName(Arrays.asList(ALEXANDRIA_EXCEL_FILENAME));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
        // FIXME: I believe this causes the NonUniqueObjectException because we're 
        // still actually using the same Hibernate Session / thread of execution that we were in initially 
        // (when setupAndLoadResource was invoked at the top of the method)
        // flush();
        dataset = controller.getDataset();
        alexandriaTable = dataset.getDataTables().iterator().next();
        DataTableColumn secondElementColumn = alexandriaTable.getColumnByName(BELEMENT_COL);
        assertNotNull(secondElementColumn);
        assertEquals(elementColumnId, secondElementColumn.getId());
        assertEquals(secondElementColumn.getDefaultOntology(), bElementOntology);
        Map<String, Long> incomingValueToOntologyNodeIdMap = secondElementColumn.getValueToOntologyNodeIdMap();
        assertEquals(valueToOntologyNodeIdMap, incomingValueToOntologyNodeIdMap);
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentExcel() {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, dataset.getId());
        String filename = "evmpp-fauna.xls";
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentMdb() {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, dataset.getId());
        String filename = SPITAL_DB_NAME;
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.AbstractDataIntegrationTestCase#getDatabaseList()
     */
    @Override
    public String[] getDatabaseList() {
        // TODO Auto-generated method stub
        return new String[0];
    }
}
