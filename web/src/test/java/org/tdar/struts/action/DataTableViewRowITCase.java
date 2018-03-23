package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.struts.action.dataset.RowViewAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;

public class DataTableViewRowITCase extends AbstractAdminControllerITCase {

    private static final String TEST_DATASET = "/data_integration_tests/england_woods.xlsx";
    private RowViewAction controller;
    private Dataset dataset;

    @Before
    public void setUpController() {
        controller = generateNewInitializedController(RowViewAction.class);
    }

    private void prepareValidData() throws TdarActionException {
        dataset = setupAndLoadResource(TEST_DATASET, Dataset.class);
        assertNotNull(dataset);
        DataTable dataTable = dataset.getDataTables().iterator().next();
        assertNotNull(dataTable);
        controller.setId(dataset.getId());
        controller.setDataTableId(dataTable.getId());
    }

    @Test
    @Rollback
    public void getDataResultsRowReturnsRow() throws Exception {
        prepareValidData();
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    public void getDataResultsDoesntReturnInvalidRowInTDAR() throws Exception {
        prepareValidData();
        controller.setRowId(100000000L);
        controller.prepare();
        assertEquals(Action.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void outOfRangeRowReturnsEmptySet() throws Exception {
        prepareValidData();
        controller.setRowId(0L);
        controller.prepare();
        assertEquals(Action.ERROR, controller.getDataResultsRow());
        assertTrue("No row was expected", controller.getDataTableRowAsMap().size() == 0);
    }

    @Test
    public void nonExistentTableIdReturnsError() throws Exception {
        controller.setDataTableId(0L);
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(Action.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void userCanViewOwnRestrictedRow() throws Exception {
        prepareValidData();
        // following forces call to getAuthenticationAndAuthorizationService().canViewConfidentialInformation...
        for (InformationResourceFile file : dataset.getInformationResourceFiles()) {
            file.setRestriction(FileAccessRestriction.CONFIDENTIAL);
        }
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.getDataResultsRow());
        assertTrue("A row was expected", controller.getDataTableRowAsMap().size() > 0);
    }

    @Test
    @Rollback
    public void userCannotViewRestrictedRow() throws Exception {
        prepareValidData();

        for (InformationResourceFile file : dataset.getInformationResourceFiles()) {
            file.setRestriction(FileAccessRestriction.CONFIDENTIAL);
        }
        genericService.save(dataset);
        TdarUser user = createAndSaveNewUser();
        init(controller, user);
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(Action.ERROR, controller.getDataResultsRow());
    }

    @Test
    @Rollback
    public void firstColumnIsRowIndex() throws Exception {
        prepareValidData();
        controller.setRowId(1L);
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.getDataResultsRow());
        final Iterator<DataTableColumn> iterator = controller.getDataTableRowAsMap().keySet().iterator();
        final String name = iterator.next().getName();
        assertTrue("The row id column is expected to be first in the map, but found " + name, name.equals(DataTableColumn.TDAR_ID_COLUMN));
    }

}
