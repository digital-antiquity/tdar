package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.PostgresIntegrationDatabase;
import org.tdar.filestore.VersionType;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TestDatasetHelper;
import org.tdar.struts.action.TestFileUploadHelper;
import org.tdar.struts.action.codingSheet.CodingSheetMappingController;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.dataset.TableXMLDownloadAction;
import org.tdar.struts.action.geospatial.GeospatialController;
import org.tdar.struts_base.action.TdarActionException;

/**
 * $Id$
 * 
 * Integration test over the DatasetController's action methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class ShapefileControllerITCase extends AbstractAdminControllerITCase implements TestFileUploadHelper, TestDatasetHelper {

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
    public void testShapeFile() throws TdarActionException, FileNotFoundException {
        GeospatialController controller = generateNewInitializedController(GeospatialController.class);
        controller.prepare();
        controller.add();
        controller.getGeospatial().setTitle("test");
        controller.getGeospatial().setDescription("test");
        File dir = TestConstants.getFile(TestConstants.TEST_SHAPEFILE_DIR);
        PersonalFilestoreTicket ticket = grabTicket();
        uploadFilesAsync(Arrays.asList(dir.listFiles()), ticket);
        for (File f : dir.listFiles()) {
            FileProxy fp = new FileProxy();
            fp.setAction(FileAction.ADD);
            fp.setFilename(f.getName());
            controller.getFileProxies().add(fp);
        }
        controller.setTicketId(ticket.getId());
        controller.setServletRequest(getServletPostRequest());
        assertEquals(com.opensymphony.xwork2.Action.SUCCESS, controller.save());
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
