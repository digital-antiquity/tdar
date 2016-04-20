/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.dataset.ResultMetadataWrapper;
import org.tdar.struts.action.dataset.DatasetController;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
public class DataSetBrowseITCase extends AbstractDataIntegrationTestCase {

    private static final int RESULTS_PER_PAGE = 2;

    private static final String SRC_TEST_EMPTY_ACCDB = "data_integration_tests/empty.accdb";
    private static final String DOUBLE_DATASET = "coding sheet/double_translation_test_dataset.xlsx";
    private static final String TEXT_DATASET = "coding sheet/csvCodingSheetText.csv";
    
    @Autowired
    private DatasetService datasetService;

    @Test
    @Rollback
    public void testBrowse() throws IOException, TdarActionException {
        // load datasets
        Dataset dataset = setupAndLoadResource(DOUBLE_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        DataTableBrowseController controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        assertEquals(Action.SUCCESS, controller.getDataResults());
        ResultMetadataWrapper resultsWrapper = controller.getResultsWrapper();
        // DEFAULT CASE -- START @ 0
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(0), resultsWrapper.getStartRecord());
        assertFalse(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());
        logger.debug("{}", controller.getJsonResult());

        // PAGED CASE -- START @ 5
        controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        controller.setStartRecord(5);
        assertEquals(Action.SUCCESS, controller.getDataResults());
        resultsWrapper = controller.getResultsWrapper();
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(5), resultsWrapper.getStartRecord());
        assertFalse(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());

        logger.debug("{}", controller.getJsonResult());

        // OVER-EXTENDED CASE -- START @ 500
        controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        controller.setStartRecord(500);
        assertEquals(Action.SUCCESS, controller.getDataResults());
        resultsWrapper = controller.getResultsWrapper();
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(500), resultsWrapper.getStartRecord());
        assertTrue(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());

        logger.debug("{}", controller.getJsonResult());
    }

    @Test
    @Rollback
    public void testSearch() throws IOException, TdarActionException {
        // load datasets
        Dataset dataset = setupAndLoadResource(TEXT_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        String term = "Bird";
        ResultMetadataWrapper selectFromDataTable = datasetService.findRowsFromDataTable(dataTable, 0, 1, true, term);
        assertNotEmpty(selectFromDataTable.getResults());
        for (List<String> result : selectFromDataTable.getResults()) {
            String row = StringUtils.join(result.toArray());
            assertTrue(row.contains(term));
        }

        term = "D";
        selectFromDataTable = datasetService.findRowsFromDataTable(dataTable, 0, 1, true, term);
        assertNotEmpty(selectFromDataTable.getResults());
        for (List<String> result : selectFromDataTable.getResults()) {
            String row = StringUtils.join(result.toArray());
            assertTrue(row.contains(term));
        }
    }

    @Test
    @Rollback
    public void testTranslate() throws IOException, TdarActionException, IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException {
        // load datasets
        Dataset dataset = setupAndLoadResource(SRC_TEST_EMPTY_ACCDB, Dataset.class);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        datasetService.createTranslatedFile(dataset);
        assertNotNull(dataset);
    }

}
