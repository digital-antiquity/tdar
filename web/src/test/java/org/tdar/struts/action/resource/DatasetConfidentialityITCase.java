package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.dataset.ColumnMetadataController;

public class DatasetConfidentialityITCase extends AbstractControllerITCase {

    private static final String TEST_DESCRIPTION = "test description";
    private static final String TEST_DATA_SET_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + "total-number-of-bones-per-period.xlsx";
    private static final String EXCEL_FILE_NAME = "periods-modified-sm-01182011.xlsx";

    Long datasetId = null;

    @SuppressWarnings("unused")
    @Test
    @Rollback(false)
    public void testConfidentialityAfterEdit() throws Exception {

        // CodingSheet codingSheet = setupCodingSheet();

        Dataset dataset = setupAndLoadResource(TEST_DATA_SET_FILE_PATH, Dataset.class, FileAccessRestriction.CONFIDENTIAL);
        evictCache();
        datasetId = dataset.getId();
        assertNotNull(datasetId);

        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.prepare();
        CodingSheet codingSheet = codingSheetController.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription(TEST_DESCRIPTION);
        List<File> codingFiles = new ArrayList<File>();
        List<String> codingFileNames = new ArrayList<String>();
        File codingFile = TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, EXCEL_FILE_NAME);
        codingFiles.add(codingFile);
        codingSheet.setDefaultOntology(null);
        codingFileNames.add("periods-modified-sm-01182011-2.xlsx");
        codingSheetController.setUploadedFilesFileName(codingFileNames);
        codingSheetController.setUploadedFiles(codingFiles);
        codingSheetController.setServletRequest(getServletPostRequest());
        codingSheetController.save();
        Long codingId = codingSheet.getId();

        // edit column metadata
        genericService.detachFromSession(dataset);
        dataset = null;
        dataset = genericService.find(Dataset.class, datasetId);

        DataTable dataTable = dataset.getDataTables().iterator().next();
        DataTableColumn period_ = dataTable.getColumnByDisplayName("Period");
        ColumnMetadataController datasetController = generateNewInitializedController(ColumnMetadataController.class);
        datasetController.setId(datasetId);
        datasetController.prepare();
        datasetController.editColumnMetadata();
        DataTableColumn cloneBean = (DataTableColumn) BeanUtils.cloneBean(period_);
        cloneBean.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        cloneBean.setDefaultCodingSheet(codingSheet);
        logger.info("{}", cloneBean);
        List<DataTableColumn> list = new ArrayList<DataTableColumn>();
        list.add(cloneBean);
        datasetController.setTableDescription(TEST_DESCRIPTION);
        datasetController.setDataTableColumns(list);
        datasetController.saveColumnMetadata();
        setVerifyTransactionCallback(new TransactionCallback<Dataset>() {
            @Override
            public Dataset doInTransaction(TransactionStatus status) {
                Dataset mydataset = genericService.find(Dataset.class, datasetId);
                logger.info("{} {}", datasetId, mydataset);
                Set<InformationResourceFile> informationResourceFiles = mydataset.getInformationResourceFiles();
                assertNotEmpty("should have files", informationResourceFiles);
                assertEquals("should have description", TEST_DESCRIPTION,mydataset.getDataTables().iterator().next().getDescription());
                assertEquals(FileAccessRestriction.CONFIDENTIAL, informationResourceFiles.iterator().next().getRestriction());
                genericService.delete(mydataset);
                return null;
            }
        });
    }

}
