package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
public class DataTableViewRowITCase extends AbstractDataIntegrationTestCase {

    private static final String TEST_DATASET = "../data_integration_tests/england_woods.xlsx";
    private DataTableViewRowController controller;
    private Dataset dataset;

    // public void userCannotViewRestrictedRow() ... not sure how to test this...

    @Before
    public void setUpController() {
        controller = generateNewInitializedController(DataTableViewRowController.class);
    }

    private void prepareValidData() {
        dataset = setupAndLoadResource(TEST_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        controller.setId(dataTable.getId());
    }

    @Test
    @Rollback
    public void getDataResultsRowReturnsRow() {
        prepareValidData();
        controller.setRowId(1L);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR })
    public void getDataResultsDoesntReturnRowInTDAR() {
        prepareValidData();
        controller.setRowId(1L);
        assertEquals(TdarActionSupport.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void outOfRangeRowReturnsEmptySet() {
        prepareValidData();
        controller.setRowId(0L);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        assertTrue("No row was expected", controller.getDataTableRowAsMap().size() == 0);
    }

    @Test
    public void nonExistentTableIdReturnsError() {
        controller.setId(0L);
        controller.setRowId(1L);
        assertEquals(TdarActionSupport.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void userCanViewOwnRestrictedRow() {
        prepareValidData();
        // following forces call to getAuthenticationAndAuthorizationService().canViewConfidentialInformation...
        for (InformationResourceFile file : dataset.getInformationResourceFiles()) {
            file.setRestriction(FileAccessRestriction.CONFIDENTIAL);
        }
        controller.setRowId(1L);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    public void firstColumnIsRowIndex() {
        prepareValidData();
        controller.setRowId(1L);
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        final Iterator<DataTableColumn> iterator = controller.getDataTableRowAsMap().keySet().iterator();
        final String name = iterator.next().getName();
        assertTrue("The row id column is expected to be first in the map, but found " + name, name.equals(TargetDatabase.TDAR_ID_COLUMN));
    }

}
