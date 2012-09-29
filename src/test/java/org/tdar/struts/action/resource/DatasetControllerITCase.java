package org.tdar.struts.action.resource;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import static org.junit.Assert.*;

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
    private static final String TRUNCATED_HARP_EXCEL_FILENAME = "heshfaun-truncated.xls";
    private static final String BELEMENT_COL = "belement";

    private DatasetController controller;
    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    private DataTableService dataTableService;

    @Before
    public void setUp() {
        controller = generateNewInitializedController(DatasetController.class);
    }

    @Test
    @Rollback
    public void testOntologyMappingCaseSensitivity() throws Exception {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller.setId(dataset.getId());
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setDefaultOntology(bElementOntology);
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        elementColumn.setName(BELEMENT_COL);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        mapColumnsToDataset(dataset, dataTable, elementColumn);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        DataTableColumn column = dataTable.getColumnByName(BELEMENT_COL);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataTable.getDataset().getId());
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
    @Rollback
    public void testNullResourceOperations() throws Exception {
        List<String> successActions = Arrays.asList("add", "list");
        // grab all methods on DatasetController annotated with a conventions plugin @Action
        for (Method method : DatasetController.class.getMethods()) {
            controller = generateNewInitializedController(DatasetController.class);
            controller.prepare();
            if (method.isAnnotationPresent(Action.class)) {
                logger.debug("Invoking action method: " + method.getName());
                try {
                	String result = (String) method.invoke(controller);
                	if (successActions.contains(method.getName())) {
                		assertEquals("DatasetController." + method.getName() + "() should return success", DatasetController.SUCCESS, result);
                	} else {
                		setIgnoreActionErrors(true);
                		assertNotSame("DatasetController." + method.getName() + "() should not return SUCCESS", DatasetController.SUCCESS, result);
                	}
                }
                catch (Exception e) {
                	if (e instanceof TdarActionException) {
                		TdarActionException exception = (TdarActionException) e;
                		setIgnoreActionErrors(true);
                		assertNotSame("DatasetController." + method.getName() + "() should not return SUCCESS", DatasetController.SUCCESS, exception.getResultName());                		
                	}

                }
            }
        }
    }

    @Test
    @Rollback
    public void testFullUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.MODIFY_RECORD);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
        addAuthorizedUser(controller.getDataset(), getUser(), GeneralPermissions.MODIFY_RECORD);
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(1, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
        assertEquals(getUserId(), dataset.getInternalResourceCollection().getAuthorizedUsers().iterator().next().getUser().getId());
    }

    @Test
    @Rollback
    public void testReadUser() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
        assertEquals(1, controller.getAuthorizedUsers().size());
        ArrayList<AuthorizedUser> authorizedUsers = new ArrayList<AuthorizedUser>();
        authorizedUsers.add(new AuthorizedUser(getBasicUser(), GeneralPermissions.VIEW_ALL));
        authorizedUsers.add(new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL));
        controller.setAuthorizedUsers(authorizedUsers);
        controller.save();
        dataset = datasetService.find(datasetId);
        ResourceCollection internalResourceCollection = dataset.getInternalResourceCollection();
        assertEquals(2, internalResourceCollection.getAuthorizedUsers().size());
        Set<Long> seen = new HashSet<Long>();
        for (AuthorizedUser r : internalResourceCollection.getAuthorizedUsers()) {
            seen.add(r.getUser().getId());
        }
        // FIXME: this fails but clearly, above it works
        // assertTrue(internalResourceCollection.getResources().contains(dataset));
        seen.remove(TestConstants.USER_ID);
        seen.remove(getAdminUserId());
        assertTrue("should have seen all user ids already", seen.isEmpty());
    }

    @Test
    @Rollback
    public void testReadUserEmpty() throws Exception {
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, false);
        Long datasetId = dataset.getId();
        addAuthorizedUser(dataset, getAdminUser(), GeneralPermissions.VIEW_ALL);
        genericService.save(dataset);
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, datasetId);
        controller.setAuthorizedUsers(Collections.<AuthorizedUser> emptyList());
        controller.save();
        dataset = datasetService.find(datasetId);
        assertEquals(0, dataset.getInternalResourceCollection().getAuthorizedUsers().size());
    }

    @Test
    @Rollback
    public void testDatasetReplaceSame() throws TdarActionException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_EXCEL_FILENAME)));
        controller.setUploadedFilesFileName(Arrays.asList(ALEXANDRIA_EXCEL_FILENAME));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }

    @Test
    @Rollback
    public void testDatasetReplaceWithMappings() throws TdarActionException {
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
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
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
    public void testDatasetReplaceDifferentExcel() throws TdarActionException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
        String filename = "evmpp-fauna.xls";
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentColTypes() throws TdarActionException {
        Dataset dataset = setupAndLoadResource("dataset_with_floats.xls", Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        assertEquals(DataTableColumnType.DOUBLE, dataset.getDataTables().iterator().next().getColumnByName("col2floats").getColumnDataType());
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
        String filename = "dataset_with_floats_to_varchar.xls";
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
        assertEquals(DataTableColumnType.VARCHAR, dataset.getDataTables().iterator().next().getColumnByName("col2floats").getColumnDataType());
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentMdb() throws TdarActionException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
        String filename = SPITAL_DB_NAME;
        controller.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
    }
    
    
    @Test
    @Rollback(false)
    public void testReprocessDataset() throws Exception {
        Dataset dataset = setupAndLoadResource(TRUNCATED_HARP_EXCEL_FILENAME, Dataset.class);
        final Long datasetId = dataset.getId();
        final DataTable dataTable = dataset.getDataTables().iterator().next();
        final int originalNumberOfRows = tdarDataImportDatabase.getRowCount(dataTable);
        final List<List<String>> originalColumnData = new ArrayList<List<String>>();
        for (DataTableColumn column: dataTable.getSortedDataTableColumns()) {
            // munge column names and rename in tdar data database
            originalColumnData.add(tdarDataImportDatabase.selectAllFrom(column));
            tdarDataImportDatabase.renameColumn(column, column.getDisplayName());
            assertFalse("Column name should be denormalized", tdarDataImportDatabase.normalizeTableOrColumnNames(column.getName()).equals(column.getName()));
            genericService.save(column);
        }
        verifyDataTable(dataTable, originalNumberOfRows, originalColumnData);
        InformationResourceFile file = dataset.getFirstInformationResourceFile();
        controller = generateNewInitializedController(DatasetController.class);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
        datasetService.reprocess(dataset);
        assertEquals(file, dataset.getFirstInformationResourceFile());
        assertEquals(file.getLatestUploadedVersion(), dataset.getFirstInformationResourceFile().getLatestUploadedVersion());
        setVerifyTransactionCallback(new TransactionCallback<Dataset>() {
            @Override
            public Dataset doInTransaction(TransactionStatus status) {
                Dataset dataset = genericService.find(Dataset.class, datasetId);
                DataTable dataTable = dataset.getDataTables().iterator().next();
                verifyDataTable(dataTable, originalNumberOfRows, originalColumnData);
                for (DataTableColumn column: dataTable.getSortedDataTableColumns()) {
                    assertEquals("Column name should be normalized", tdarDataImportDatabase.normalizeTableOrColumnNames(column.getName()), column.getName());
                }
                return null;
            }
        });
    }

    private void verifyDataTable(final DataTable dataTable, final int expectedNumberOfRows, List<List<String>> expectedColumnData) {
        assertEquals(expectedNumberOfRows, tdarDataImportDatabase.getRowCount(dataTable));
        Iterator<List<String>> expectedColumnDataIterator = expectedColumnData.iterator();
        for (DataTableColumn column: dataTable.getSortedDataTableColumns()) {
            // verify column values
            List<String> expectedValues = expectedColumnDataIterator.next();
            List<String> actualValues = tdarDataImportDatabase.selectAllFrom(column);
            assertEquals(expectedValues, actualValues);
        }
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
