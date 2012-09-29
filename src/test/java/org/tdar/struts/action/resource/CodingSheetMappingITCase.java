/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.ResultMetadataWrapper;

/**
 * @author Adam Brin
 * 
 */
public class CodingSheetMappingITCase extends AbstractDataIntegrationTestCase {

    private static final String TEST_DATA_SET_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + "total-number-of-bones-per-period.xlsx";

    private static final File TEST_DATSET_FILE = new File(TEST_DATA_SET_FILE_PATH);
    private static final String EXCEL_FILE_NAME = "periods-modified-sm-01182011.xlsx";
    private static final String EXCEL_FILE_NAME2 = "periods-modified-sm-01182011-2.xlsx";
    private static final String EXCEL_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + EXCEL_FILE_NAME;
    private static final String EXCEL_FILE_PATH2 = TestConstants.TEST_DATA_INTEGRATION_DIR + EXCEL_FILE_NAME2;
    private String codingSheetFileName = "/coding sheet/csvCodingSheetText.csv";

    private static final String DOUBLE_CODING = "double_coding_key.csv";
    private static final String DOUBLE_DATASET = "double_translation_test_dataset.xlsx";
    private static final String BASIC_CSV = "csvCodingSheetText.csv";

    // TEST ME: http://dev.tdar.org/jira/browse/TDAR-587
    // TEST ME: http://dev.tdar.org/jira/browse/TDAR-581

    private static final String PATH = TestConstants.TEST_CODING_SHEET_DIR;

    @Test
    @Rollback
    public void testSimpleDelete() throws Exception {
        CodingSheet codingSheet = setupAndLoadResource(BASIC_CSV, CodingSheet.class);
        Set<CodingRule> oldCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());

        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingSheet.getId());
        controller.prepare();
        String text = (IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName))).trim();
        text = text.substring(0, text.lastIndexOf("\n"));
        controller.setFileInputMethod("text");
        assertNotNull(text);
        assertFalse(text.contains("D, Don't"));
        controller.setFileTextInput(text);
        controller.save();
        Set<CodingRule> newCodingRules = controller.getCodingSheet().getCodingRules();
        logger.info("text was: " + text);
        logger.info("{}", oldCodingRules);
        logger.info("{}", newCodingRules);
        assertFalse(newCodingRules.equals(oldCodingRules));
        for (CodingRule rule : newCodingRules) {
            if (rule.getCode().equals("D")) {
                fail("Should not have found a 'D'");
            }
        }
    }

    @Test
    @Rollback
    public void testDoubleCoding() {
        CodingSheet codingSheet = setupAndLoadResource(DOUBLE_CODING, CodingSheet.class);
        Dataset dataset = setupAndLoadResource(DOUBLE_DATASET, Dataset.class);
        DataTable firstTable = dataset.getDataTables().iterator().next();
        assertNotNull(firstTable);
        DataTableColumn column = firstTable.getColumnByDisplayName("double");
        assertFalse(column.getName().equals(column.getDisplayName()));
        assertEquals(DataTableColumnEncodingType.UNCODED_VALUE, column.getColumnEncodingType());
        assertEquals(DataTableColumnType.DOUBLE, column.getColumnDataType());
        column.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        column.setDefaultCodingSheet(codingSheet);
        datasetService.save(column);
        datasetService.translate(column, codingSheet);

        ResultMetadataWrapper resultsWrapper = datasetService.selectAllFromDataTable(firstTable, 0, 100, true);
        List<List<String>> selectAllFromDataTable = resultsWrapper.getResults();
        assertEquals(6, selectAllFromDataTable.size());
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        int idRow = -1;
        int colRow = -1;
        for (int i = 0; i < resultsWrapper.getFields().size(); i++) {
            if (resultsWrapper.getFields().get(i).equals(DataTableColumn.TDAR_ROW_ID)) {
                idRow = i;
            }
            if (resultsWrapper.getFields().get(i).equals(column)) {
                colRow = i;
            }
        }
        assertFalse(colRow == -1);
        assertTrue("id row is hidden and should be -1", idRow == -1);

        idRow = 1;
        for (List<String> row : selectAllFromDataTable) {
            map.put(idRow, row.get(colRow));
            logger.info("{}: {}", idRow, row.get(colRow));
            idRow++;
        }
        assertEquals("elephant", map.get(1));
        assertEquals("elephant", map.get(2));
        assertEquals("elephant", map.get(3));
        assertEquals("marble", map.get(4));
        assertEquals("watercolor", map.get(5));
        assertEquals("watercolor", map.get(6));
    }

    @Test
    @Rollback
    public void testTabDelimitedCodingSheetUpload() throws Exception {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(0 == codingSheet.getCodingRules().size());
        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        assertTrue(ruleMap.get("a") != null);
        assertTrue(ruleMap.get("b") != null);
        assertTrue(ruleMap.get("1") != null);
        assertTrue(ruleMap.get("D") != null);
        assertEquals("test description", ruleMap.get("a").getDescription());
        assertEquals("Aardvark", ruleMap.get("a").getTerm());
        assertEquals("Bird", ruleMap.get("b").getTerm());
        assertEquals("numeric", ruleMap.get("1").getTerm());
        assertEquals("Don't", ruleMap.get("D").getTerm());

        Collection<InformationResourceFileVersion> latestVersions = codingSheet.getLatestVersions();

        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingId);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        assertNotNull(controller.getFileTextInput());
        assertEquals(codingText, controller.getFileTextInput());

        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        // FIXME: brittle, use
        assertEquals(latestVersions, codingSheet.getLatestVersions());

        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingId);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        controller.setFileTextInput(codingText + "abd ");
        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        genericService.merge(codingSheet);
        assertFalse(latestVersions.equals(codingSheet.getLatestVersions()));
    }

    @Test
    @Rollback
    public void testFakeCodingSheetWithDataTable() throws Exception {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();
        Dataset ds = new Dataset();
        ds.setTitle("test");
        ds.markUpdated(getUser());
        genericService.save(ds);
        DataTable dt = new DataTable();
        dt.setName("test");
        dt.setDataset(ds);
        genericService.save(dt);
        DataTableColumn dtc = new DataTableColumn();
        dtc.setName("test");
        dtc.setDisplayName("test");
        dtc.setDataTable(dt);
        dtc.setColumnDataType(DataTableColumnType.VARCHAR);
        dtc.setDefaultCodingSheet(codingSheet);
        genericService.save(dtc);
        Set<CodingRule> exitingCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());
        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingSheet.getId());
        controller.prepare();
        codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();

        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(0 == codingSheet.getCodingRules().size());
        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        logger.debug("existing:" + exitingCodingRules);
        logger.debug("new:" + codingSheet.getCodingRules());
        for (CodingRule existing : exitingCodingRules) {
            assertEquals("expecting matching ids for key:" + existing.getCode(), existing.getId(), ruleMap.get(existing.getCode()).getId());
        }
        assertTrue(ruleMap.get("a") != null);
        assertTrue(ruleMap.get("b") != null);
        assertTrue(ruleMap.get("1") != null);
        assertTrue(ruleMap.get("D") != null);
        assertEquals("test description", ruleMap.get("a").getDescription());
        assertEquals("Aardvark", ruleMap.get("a").getTerm());
        assertEquals("Bird", ruleMap.get("b").getTerm());
        assertEquals("numeric", ruleMap.get("1").getTerm());
        assertEquals("Don't", ruleMap.get("D").getTerm());

    }

    @Test
    @Rollback
    public void testXLSCodingSheetUpload() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();

        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        assertTrue(ruleMap.get("12") != null);
        assertTrue(ruleMap.get("6") != null);
        assertEquals("g", ruleMap.get("12").getTerm());
        assertEquals("m", ruleMap.get("6").getTerm());
    }

    @Test
    @Rollback
    public void testCodingSheetMappingReplace() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.setId(codingSheet.getId());
        codingSheetController.prepare();
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFiles.add(new File(EXCEL_FILE_PATH2));
        uploadedFileNames.add(EXCEL_FILE_NAME2);
        codingSheetController.setUploadedFilesFileName(uploadedFileNames);
        codingSheetController.setUploadedFiles(uploadedFiles);
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        Set<CodingRule> rules = codingSheet.getCodingRules();
        assertFalse(rules.isEmpty());
        boolean found = false;
        for (CodingRule rule : rules) {
            if (rule.getCode().equals("0")) {
                assertEquals("aaaa", rule.getTerm());
                assertEquals("1234", rule.getDescription());
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Rollback
    public void testCodingSheetMapping() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();

        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        datasetController.prepare();
        Dataset dataset = datasetController.getDataset();
        dataset.setTitle("test dataset");
        dataset.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFileNames.add(TEST_DATSET_FILE.getName());
        uploadedFiles.add(TEST_DATSET_FILE);
        datasetController.setUploadedFiles(uploadedFiles);
        datasetController.setUploadedFilesFileName(uploadedFileNames);
        datasetController.save();
        Long datasetId = dataset.getId();
        assertNotNull(datasetId);
        DataTableColumn period_ = dataset.getDataTables().iterator().next().getColumnByDisplayName("Period");
        datasetController = generateNewInitializedController(DatasetController.class);
        datasetController.setId(datasetId);
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        datasetController.saveColumnMetadata();
        dataset = null;
        dataset = genericService.find(Dataset.class, datasetId);
        for (DataTable table : dataset.getDataTables()) {
            for (DataTableColumn dtc : table.getDataTableColumns()) {
                logger.debug(dtc.getName());
                if (dtc.getName().equals("Period")) {
                    assertEquals(dtc.getDefaultCodingSheet().getId(), codingSheet.getId());
                }
            }
        }
        datasetService.createTranslatedFile(dataset);
    }

    @Test
    @Rollback
    public void testDatasetMappingPreservation() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();
        Dataset dataset = setupIntegrationDataset(TEST_DATSET_FILE, "Test Dataset");
        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        Long datasetId = dataset.getId();
        DataTable table = dataset.getDataTables().iterator().next();
        datasetController.setId(datasetId);
        DataTableColumn period_ = table.getColumnByDisplayName("Period");
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        datasetController.setDataTableColumns(Arrays.asList(period_));
        datasetController.saveColumnMetadata();
        DataTableColumn periodColumn = null;
        DataTableColumn period = genericService.find(DataTableColumn.class, period_.getId());
        logger.info("{}", period.getDefaultCodingSheet());
        logger.info("{}", codingSheet.getId());
        assertNull("period should not be set because column type is wrong", period.getDefaultCodingSheet());

        datasetController = generateNewInitializedController(DatasetController.class);
        table = dataset.getDataTables().iterator().next();
        datasetController.setId(datasetId);
        period_ = table.getColumnByDisplayName("Period");
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        period_.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        datasetController.setDataTableColumns(Arrays.asList(period_));
        datasetController.saveColumnMetadata();
        periodColumn = null;
        period = genericService.find(DataTableColumn.class, period_.getId());
        assertEquals(period.getDefaultCodingSheet().getId(), codingSheet.getId());
        datasetService.createTranslatedFile(dataset);

        Dataset newDataset = setupIntegrationDataset(TEST_DATSET_FILE, "Test Dataset", datasetId);

        for (DataTable incomingDataTable : newDataset.getDataTables()) {
            for (DataTableColumn incomingColumn : incomingDataTable.getDataTableColumns()) {
                if (incomingColumn.getName().equals("Period")) {
                    // default id equality
                    assertEquals(incomingColumn, periodColumn);
                    assertEquals(incomingColumn.getDefaultCodingSheet().getId(), codingSheet.getId());
                }
            }
        }
    }

    public Dataset setupIntegrationDataset(File file, String datasetTitle) throws TdarActionException {
        return setupIntegrationDataset(file, datasetTitle, null);
    }

    public Dataset setupIntegrationDataset(File file, String datasetTitle, Long datasetId) throws TdarActionException {
        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        logger.info("setting resource id: " + datasetId);
        if (datasetId != null) {
            datasetController.setId(datasetId);
        }
        datasetController.prepare();

        Dataset dataset = datasetController.getDataset();
        dataset.setTitle(datasetTitle);
        dataset.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFileNames.add(file.getName());
        uploadedFiles.add(file);
        datasetController.setUploadedFiles(uploadedFiles);
        datasetController.setUploadedFilesFileName(uploadedFileNames);
        datasetController.save();
        assertNotNull(dataset.getId());
        assertEquals("controller shouldn't have action errors", 0, datasetController.getActionErrors().size());
        return dataset;
    }

    /**
     * @return
     * @throws TdarActionException 
     */
    private CodingSheet setupCodingSheet() throws TdarActionException {
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.prepare();
        CodingSheet codingSheet = codingSheetController.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFiles.add(new File(EXCEL_FILE_PATH));
        uploadedFileNames.add(EXCEL_FILE_NAME);
        codingSheetController.setUploadedFilesFileName(uploadedFileNames);
        codingSheetController.setUploadedFiles(uploadedFiles);
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(codingSheet.getCodingRules().isEmpty());
        return codingSheet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getDatabaseList() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    @Override
    protected String getTestFilePath() {
        return PATH;
    }

}
