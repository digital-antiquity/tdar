package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.struts2.convention.annotation.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.PostgresIntegrationDatabase;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TestDatasetHelper;
import org.tdar.struts.action.TestFileUploadHelper;
import org.tdar.struts.action.codingSheet.CodingSheetMappingController;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.dataset.TableXMLDownloadAction;
import org.tdar.struts_base.action.TdarActionException;

/**
 * $Id$
 * 
 * Integration test over the DatasetController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class DatasetControllerITCase extends AbstractAdminControllerITCase implements TestFileUploadHelper, TestDatasetHelper {

    private static final String PUNDO_FAUNAL_REMAINS_XLS = "Pundo faunal remains.xls";
    private static final String ALEXANDRIA_EXCEL_FILENAME = "qrybonecatalogueeditedkk.xls";
    private static final String TRUNCATED_HARP_EXCEL_FILENAME = "heshfaun-truncated.xls";
    private static final String BELEMENT_COL = "belement";

    // private DatasetController controller;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTableService dataTableService;

    @Autowired
    private BillingAccountService accountService;

    @Test
    @Rollback
    public void testSaveandMerge() {
        TdarUser p = genericService.find(TdarUser.class, getUser().getId());
        Dataset dataset = genericService.findRandom(Dataset.class, 1).get(0);
        dataset.setTitle("test");
        dataset.setSubmitter(p);
        dataset.markUpdated(getUser());
        genericService.merge(dataset);
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testAccountListExistsInDatasetController() throws TdarActionException {
        // setup new dataset with authorized user (direct) and grant EDIT rights
        // associate resource with billing account
        TdarUser p = genericService.find(TdarUser.class, getUser().getId());
        Dataset dataset = createAndSaveNewDataset();
        List<BillingAccount> findAll = genericService.findAll(BillingAccount.class);
        BillingAccount account = findAll.get(0);
        dataset.setAccount(account);
        accountService.updateQuota(account, p, dataset);
        TdarUser createAndSaveNewPerson = createAndSaveNewPerson("a@bcasdasd.com", "aa");
        dataset.getAuthorizedUsers().add(new AuthorizedUser(p, createAndSaveNewPerson, Permissions.MODIFY_RECORD));
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();

        // done setup
        Long datasetId = dataset.getId();
        DatasetController c = generateNewInitializedController(DatasetController.class, createAndSaveNewPerson);
        c.setId(datasetId);
        c.prepare();
        c.edit();
        // assert that "activeAccounts" contains our billing account
        List<BillingAccount> activeAccounts = c.getActiveAccounts();
        logger.debug("active Accounts:{}", activeAccounts);
        assertTrue("account list contains one account", activeAccounts.contains(account));
    }

    @Autowired
    private PostgresIntegrationDatabase database;

    @Test
    @Rollback(true)
    public void testTranslatedGeneratedCodingSheet() throws Exception {
        // test for TDAR-5038 ;; issue is that the translated values are being pushed into coding sheets.
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTableColumn elementColumn = new DataTableColumn();
        DataTable dataTable = dataset.getDataTables().iterator().next();
        elementColumn.setTransientOntology(bElementOntology);
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        elementColumn.setName(BELEMENT_COL);
        mapColumnsToDataset(dataset, dataTable, elementColumn);
        DataTableColumn column = dataTable.getColumnByName(BELEMENT_COL);
        database.translateInPlace(column, column.getDefaultCodingSheet());
        database.executeUpdateOrDelete(String.format("update %s set %s='ABCD'", dataTable.getName(), column.getName()));
        assertNotNull(column.getDefaultCodingSheet());
        assertTrue(column.getDefaultCodingSheet().isGenerated());

        List<String> original = database.selectNonNullDistinctValues(dataTable, column, true);
        logger.debug("original:{}", original);
        assertFalse(original.contains("ABCD"));
        List<String> mapped = database.selectNonNullDistinctValues(dataTable, column, false);
        logger.debug("mapped:{}", mapped);
        assertTrue(mapped.contains("ABCD"));

    }

    @Test
    @Rollback
    public void testOntologyMappingCaseSensitivity() throws Exception {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTableColumn elementColumn = new DataTableColumn();
        DataTable dataTable = dataset.getDataTables().iterator().next();
        elementColumn.setTransientOntology(bElementOntology);
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        elementColumn.setName(BELEMENT_COL);
        mapColumnsToDataset(dataset, dataTable, elementColumn);
        CodingSheetMappingController codingSheetController = generateNewInitializedController(CodingSheetMappingController.class);
        DataTableColumn column = dataTable.getColumnByName(BELEMENT_COL);

        assertNotNull(column.getDefaultCodingSheet());
        assertTrue(column.getDefaultCodingSheet().isGenerated());

        codingSheetController.setId(column.getDefaultCodingSheet().getId());
        codingSheetController.prepare();
        codingSheetController.loadOntologyMappedColumns();
        List<String> findAllDistinctValues = dataTableService.findAllDistinctValues(dataTable, column);
        List<String> tibias = new ArrayList<String>();
        for (String distinct : findAllDistinctValues) {
            if (distinct.toLowerCase().contains("tibia")) {
                tibias.add(distinct);
            }
        }

        int tibia = -1;
        int Tibia = -1;
        List<String> suggestedTibias = new ArrayList<String>();
        int i = 0;

        for (String key : codingSheetController.getSuggestions().keySet()) {
            if (key.equals("Tibia")) {
                Tibia = i;
            }
            if (key.equals("tibia")) {
                tibia = i;
            }
            i++;
            if (key.toLowerCase().contains("tibia")) {
                suggestedTibias.add(key);
            }
        }

        assertEquals(tibias.size(), suggestedTibias.size());
        assertNotSame(tibia, -1);
        assertNotSame(Tibia, -1);
        assertTrue(String.format("%d < %d", tibia, Tibia), tibia < Tibia);

        logger.info("{}", suggestedTibias);
        Collections.sort(suggestedTibias);
        Collections.sort(tibias);
        assertEquals(tibias, suggestedTibias);
    }

    @Test
    @Rollback
    public void testNullResourceOperations() throws Exception {
        List<String> successActions = Arrays.asList("add", "list", "edit");
        // grab all methods on DatasetController annotated with a conventions plugin @Action
        for (Method method : DatasetController.class.getMethods()) {
            DatasetController controller = generateNewInitializedController(DatasetController.class);
            controller.prepare();
            if (method.isAnnotationPresent(Action.class)) {
                logger.debug("Invoking action method: " + method.getName());
                try {
                    String result = (String) method.invoke(controller);
                    if (successActions.contains(method.getName())) {
                        assertEquals("DatasetController." + method.getName() + "() should return success", com.opensymphony.xwork2.Action.SUCCESS, result);
                    } else {
                        setIgnoreActionErrors(true);
                        assertNotSame("DatasetController." + method.getName() + "() should not return SUCCESS", com.opensymphony.xwork2.Action.SUCCESS, result);
                    }
                } catch (Exception e) {
                    if (e instanceof TdarActionException) {
                        TdarActionException exception = (TdarActionException) e;
                        setIgnoreActionErrors(true);
                        logger.error("{}", exception);
                        // assertNotSame("DatasetController." + method.getName() + "() should not return SUCCESS", com.opensymphony.xwork2.Action.SUCCESS,
                        // exception.getResultName());
                    }

                }
            }
        }
    }

    @Test
    @Rollback
    public void testDatasetReplaceSame() throws TdarActionException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class, dataset.getId());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(value = false)
    /**
     * make sure that the column names are re-aligned
     * 
     * @throws TdarActionException
     */
    public void testDatasetReplaceLegacy() throws TdarActionException {
        Dataset dataset = setupAndLoadResource(PUNDO_FAUNAL_REMAINS_XLS, Dataset.class);
        DataTable table = dataset.getDataTables().iterator().next();
        final Long id = dataset.getId();
        DataTableColumn el = table.getColumnByDisplayName("Element");
        final Long elId = el.getId();
        el.setName("element");
        genericService.saveOrUpdate(el);
        logger.debug("element:{}", el);
        dataset = null;
        el = null;
        table = null;
        genericService.synchronize();
        setVerifyTransactionCallback(new TransactionCallback<Dataset>() {
            @Override
            public Dataset doInTransaction(TransactionStatus status) {
                try {
                    DataTableColumn col = genericService.find(DataTableColumn.class, elId);
                    col.setName("element");
                    genericService.saveOrUpdate(col);
                    col = null;
                    Dataset dataset = setupAndLoadResource(PUNDO_FAUNAL_REMAINS_XLS, Dataset.class, id);
                    logger.debug("cols: {}", dataset.getDataTables().iterator().next().getDataTableColumns());
                    DataTableColumn el = dataset.getDataTables().iterator().next().getColumnByDisplayName("Element");
                    Long elid2 = el.getId();
                    el = null;
                    assertEquals(elId, elid2);
                } catch (TdarActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }
        });

    }

    @Test
    @Rollback
    public void tableAsXmlReturnsErrorIfXmlExportNotEnabled() {
        TableXMLDownloadAction controller_ = generateNewInitializedController(TableXMLDownloadAction.class);
        assertSame(com.opensymphony.xwork2.Action.ERROR, controller_.getTableAsXml());
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void tableAsXml() throws Exception {
        Dataset dataset = setupAndLoadResource(TRUNCATED_HARP_EXCEL_FILENAME, Dataset.class);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        TableXMLDownloadAction controller_ = generateNewInitializedController(TableXMLDownloadAction.class);
        controller_.setId(dataset.getId());
        controller_.setDataTableId(dataTable.getId());
        controller_.prepare();
        assertEquals(com.opensymphony.xwork2.Action.SUCCESS, controller_.getTableAsXml());
        InputStream xmlStream = controller_.getXmlStream();
        String xml = IOUtils.toString(xmlStream, "UTF-8");
        assertTrue(xml.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""));
    }

    @Test
    @Rollback
    public void testDatasetReplaceWithMappings() throws Exception {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);

        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTable alexandriaTable = dataset.getDataTables().iterator().next();
        DataTableColumn elementColumn = alexandriaTable.getColumnByName(BELEMENT_COL);
        elementColumn.setTransientOntology(bElementOntology);
        Long elementColumnId = elementColumn.getId();
        mapColumnsToDataset(dataset, alexandriaTable, elementColumn);
        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getElementValueMap(), bElementOntology);
        Map<String, List<Long>> valueToOntologyNodeIdMap = elementColumn.getValueToOntologyNodeIdMap();
        elementColumn = null;
        controller.setId(dataset.getId());
        controller.prepare();
        controller.edit();
        controller.setUploadedFiles(Arrays.asList(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_EXCEL_FILENAME)));
        controller.setUploadedFilesFileName(Arrays.asList(ALEXANDRIA_EXCEL_FILENAME));
        controller.setServletRequest(getServletPostRequest());
        assertEquals(com.opensymphony.xwork2.Action.SUCCESS, controller.save());
        // FIXME: I believe this causes the NonUniqueObjectException because we're
        // still actually using the same Hibernate Session / thread of execution that we were in initially
        // (when setupAndLoadResource was invoked at the top of the method)
        // flush();
        dataset = controller.getDataset();
        alexandriaTable = dataset.getDataTables().iterator().next();
        DataTableColumn secondElementColumn = alexandriaTable.getColumnByName(BELEMENT_COL);
        assertNotNull(secondElementColumn);
        assertEquals(elementColumnId, secondElementColumn.getId());
        assertEquals(secondElementColumn.getTransientOntology(), bElementOntology);
        Map<String, List<Long>> incomingValueToOntologyNodeIdMap = secondElementColumn.getValueToOntologyNodeIdMap();
        assertEquals(valueToOntologyNodeIdMap, incomingValueToOntologyNodeIdMap);
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentExcel() throws TdarActionException, FileNotFoundException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        controller.prepare();
        controller.edit();
        String filename = "evmpp-fauna.xls";
        controller.setUploadedFiles(Arrays.asList(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR + filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        controller.setServletRequest(getServletPostRequest());
        assertEquals(com.opensymphony.xwork2.Action.SUCCESS, controller.save());
    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentColTypes() throws TdarActionException {
        Dataset dataset = setupAndLoadResource("dataset_with_floats.xls", Dataset.class);
        String filename = "dataset_with_floats_to_varchar.xls";
        assertEquals(DataTableColumnType.DOUBLE, dataset.getDataTables().iterator().next().getColumnByName("col2floats").getColumnDataType());
        Long datasetId = dataset.getId();
        dataset = null;
        dataset = replaceFile(filename, "dataset_with_floats.xls", Dataset.class, datasetId);
        assertEquals(DataTableColumnType.VARCHAR, dataset.getDataTables().iterator().next().getColumnByName("col2floats").getColumnDataType());

    }

    @Test
    @Rollback
    public void testDatasetReplaceDifferentMdb() throws TdarActionException, FileNotFoundException {
        Dataset dataset = setupAndLoadResource(ALEXANDRIA_EXCEL_FILENAME, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        controller.prepare();
        controller.edit();
        String filename = TestConstants.SPITAL_DB_NAME;
        controller.setUploadedFiles(Arrays.asList(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, filename)));
        controller.setUploadedFilesFileName(Arrays.asList(filename));
        controller.setServletRequest(getServletPostRequest());
        assertEquals(com.opensymphony.xwork2.Action.SUCCESS, controller.save());
    }

    @Test
    @Rollback(false)
    public void testReprocessDataset() throws Exception {
        Dataset dataset = setupAndLoadResource(TRUNCATED_HARP_EXCEL_FILENAME, Dataset.class);
        final Long datasetId = dataset.getId();
        final DataTable dataTable = dataset.getDataTables().iterator().next();
        final int originalNumberOfRows = tdarDataImportDatabase.getRowCount(dataTable);
        final List<List<String>> originalColumnData = new ArrayList<List<String>>();
        for (DataTableColumn column : dataTable.getSortedDataTableColumns()) {
            // munge column names and rename in tdar data database
            originalColumnData.add(tdarDataImportDatabase.selectAllFrom(column));
            tdarDataImportDatabase.renameColumn(column, column.getDisplayName());
            assertFalse("Column name should be denormalized", tdarDataImportDatabase.normalizeTableOrColumnNames(column.getName()).equals(column.getName()));
            genericService.save(column);
        }
        verifyDataTable(dataTable, originalNumberOfRows, originalColumnData);
        InformationResourceFile file = dataset.getFirstInformationResourceFile();
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        controller.prepare();
        controller.edit();
        datasetService.reprocess(dataset);
        assertEquals(file, dataset.getFirstInformationResourceFile());
        assertEquals(file.getLatestUploadedVersion(), dataset.getFirstInformationResourceFile().getLatestUploadedVersion());
        setVerifyTransactionCallback(new TransactionCallback<Dataset>() {
            @Override
            public Dataset doInTransaction(TransactionStatus status) {
                Dataset dataset = genericService.find(Dataset.class, datasetId);
                DataTable dataTable = dataset.getDataTables().iterator().next();
                verifyDataTable(dataTable, originalNumberOfRows, originalColumnData);
                for (DataTableColumn column : dataTable.getSortedDataTableColumns()) {
                    assertEquals("Column name should be normalized", tdarDataImportDatabase.normalizeTableOrColumnNames(column.getName()), column.getName());
                }
                return null;
            }
        });
    }

    private void verifyDataTable(final DataTable dataTable, final int expectedNumberOfRows, List<List<String>> expectedColumnData) {
        assertEquals(expectedNumberOfRows, tdarDataImportDatabase.getRowCount(dataTable));
        Iterator<List<String>> expectedColumnDataIterator = expectedColumnData.iterator();
        for (DataTableColumn column : dataTable.getSortedDataTableColumns()) {
            // verify column values
            logger.debug(String.format("table: %s / %s --> column:%s (%s) %s ", dataTable.getName(), dataTable.getDisplayName(),  column.getName(), column.getDisplayName(), column.getId()));
            List<String> expectedValues = expectedColumnDataIterator.next();
            List<String> actualValues = tdarDataImportDatabase.selectAllFrom(column);
            assertEquals(expectedValues, actualValues);
        }
    }

    @Override
    public String getTestFilePath() {
        return TestConstants.TEST_DATA_INTEGRATION_DIR;
    }

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

}
