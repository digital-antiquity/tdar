package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.struts.action.resource.DatasetController;

public class DataTableViewRowITCase extends AbstractDataIntegrationTestCase {

    private static final String TEST_DATASET = "src/test/resources/data_integration_tests/england_woods.xlsx";
    private DatasetController controller;
    private Dataset dataset;

    @Before
    public void setUpController() {
        controller = generateNewInitializedController(DatasetController.class);
    }

    private void prepareValidData() {
        dataset = setupAndLoadResource(TEST_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        controller.setId(dataset.getId());
        controller.setDataTableId(dataTable.getId());
    }

    @Test
    @Rollback
    public void getDataResultsRowReturnsRow() {
        prepareValidData();
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    public void getDataResultsDoesntReturnInvalidRowInTDAR() {
        prepareValidData();
        controller.setRowId(100000000L);
        controller.prepare();
        assertEquals(TdarActionSupport.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void outOfRangeRowReturnsEmptySet() {
        prepareValidData();
        controller.setRowId(0L);
        controller.prepare();
        assertEquals(TdarActionSupport.ERROR, controller.getDataResultsRow());
        assertTrue("No row was expected", controller.getDataTableRowAsMap().size() == 0);
    }

    @Test
    public void nonExistentTableIdReturnsError() {
        controller.setDataTableId(0L);
        controller.setRowId(1L);
        controller.prepare();
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
        controller.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    public void userCannotViewRestrictedRow() {
        prepareValidData();

        for (InformationResourceFile file : dataset.getInformationResourceFiles()) {
            file.setRestriction(FileAccessRestriction.CONFIDENTIAL);
        }
        genericService.save(dataset);
        Person user = createAndSaveNewPerson();
        init(controller, user);
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(TdarActionSupport.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void firstColumnIsRowIndex() {
        prepareValidData();
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(TdarActionSupport.SUCCESS, controller.getDataResultsRow());
        final Iterator<DataTableColumn> iterator = controller.getDataTableRowAsMap().keySet().iterator();
        final String name = iterator.next().getName();
        assertTrue("The row id column is expected to be first in the map, but found " + name, name.equals(TargetDatabase.TDAR_ID_COLUMN));
    }

}
