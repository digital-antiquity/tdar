/**
x * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import java.io.IOException;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.struts.data.ResultMetadataWrapper;

import static org.junit.Assert.*;

/**
 * @author Adam Brin
 * 
 */
public class DataSetBrowseITCase extends AbstractDataIntegrationTestCase {

    private static final int RESULTS_PER_PAGE = 2;

    private static final String DOUBLE_DATASET = "../coding sheet/double_translation_test_dataset.xlsx";

    @Test
    @Rollback
    public void testBrowse() throws IOException {
        // load datasets
        Dataset dataset = setupAndLoadResource(DOUBLE_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        DataTableBrowseController controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResults());
        ResultMetadataWrapper resultsWrapper = controller.getResultsWrapper();
        // DEFAULT CASE -- START @ 0
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(0), resultsWrapper.getStartRecord());
        assertFalse(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());
        logger.debug("{}", controller.getResultsWrapper().toJSON());

        // PAGED CASE -- START @ 5
        controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        controller.setStartRecord(5);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResults());
        resultsWrapper = controller.getResultsWrapper();
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(5), resultsWrapper.getStartRecord());
        assertFalse(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());
        logger.debug("{}", controller.getResultsWrapper().toJSON());

        // OVER-EXTENDED CASE -- START @ 500
        controller = generateNewInitializedController(DataTableBrowseController.class);
        controller.setId(dataTable.getId());
        controller.setRecordsPerPage(RESULTS_PER_PAGE);
        controller.setStartRecord(500);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResults());
        resultsWrapper = controller.getResultsWrapper();
        assertEquals(new Integer(RESULTS_PER_PAGE), resultsWrapper.getRecordsPerPage());
        assertEquals(new Integer(6), resultsWrapper.getTotalRecords());
        assertEquals(new Integer(500), resultsWrapper.getStartRecord());
        assertTrue(resultsWrapper.getResults().isEmpty());
        assertFalse(resultsWrapper.getFields().isEmpty());

        logger.debug("{}", controller.getResultsWrapper().toJSON());
    }
}
