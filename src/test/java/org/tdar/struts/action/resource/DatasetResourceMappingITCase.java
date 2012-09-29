package org.tdar.struts.action.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.TdarActionException;

import static org.junit.Assert.*;

public class DatasetResourceMappingITCase extends AbstractDataIntegrationTestCase {

    @Autowired
    DatasetService datasetService;

    @Autowired
    GenericService genericService;

    Dataset sharedDataset = null;
    List<Long> sharedImageIds;
    @Test
    @Rollback
    public void testDatasetMapping() throws Exception {

        Project project = new Project();
        project.setTitle("test project");
        project.setDescription("mapping test");
        project.markUpdated(getSessionUser());
        genericService.save(project);

        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.prepare();
        Dataset dataset = controller.getDataset();
        dataset.setTitle("test title");
        dataset.setDescription("test title");
        controller.setProjectId(project.getId());
        dataset.markUpdated(getSessionUser());
        File file = new File(TestConstants.TEST_DATA_INTEGRATION_DIR + "/tab_mapping_dataset.tab");
        controller.setUploadedFiles(Arrays.asList(file));
        controller.setUploadedFilesFileName(Arrays.asList(file.getName()));
        controller.save();
        Long image1_id = uploadImage("5127663428_42ef7f4463_b.jpg", project).getId();
        Long image2_id = uploadImage("handbook_of_archaeology.jpg", project).getId();
        sharedDataset = dataset;

        genericService.synchronize();
        genericService.refresh(project);
        assertEquals(3, project.getInformationResources().size());

        controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        dataset = null;
        controller.prepare();
        controller.editColumnMetadata();
        boolean seenMappingColumn = false;

        List<DataTableColumn> dataTableColumns = controller.getDataset().getDataTables().iterator().next().getDataTableColumns();
        List<DataTableColumn> dataTableColumns_ = new ArrayList<DataTableColumn>();
        for (DataTableColumn column_ : dataTableColumns) {
            DataTableColumn column = (DataTableColumn) BeanUtils.cloneBean(column_);
            dataTableColumns_.add(column);
            if (column.getName().equals("mapping")) {
                seenMappingColumn = true;
                column.setMappingColumn(true);
                column.setIgnoreFileExtension(false);
            }
        }
        controller.setDataTableColumns(dataTableColumns_);
        assertTrue(seenMappingColumn);
        controller.saveColumnMetadata();
        // FIXME: replace with verifyTransactionCallback
        genericService.synchronize();

        Image image1 = genericService.find(Image.class, image1_id);
        Image image2 = genericService.find(Image.class, image2_id);

        logger.info(String.format("image mapping: %s - %s", image1.getMappedDataKeyColumn(), image1.getMappedDataKeyValue()));
        logger.info(String.format("image mapping: %s - %s", image2.getMappedDataKeyColumn(), image2.getMappedDataKeyValue()));

        assertFalse(image1.getMappedDataKeyColumn() == null);
        assertFalse(image1.getMappedDataKeyValue() == null);
        assertFalse(image2.getMappedDataKeyColumn() == null);
        assertFalse(image2.getMappedDataKeyValue() == null);
        // do search for something in another column
        sharedImageIds = Arrays.asList(image1.getId(), image2.getId());
    }

    
    @Test
    @Rollback(false)
    public void testDatasetMappingWithReplace() throws Exception {
        testDatasetMapping();
        Dataset dataset = sharedDataset;

        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setId(dataset.getId());
        controller.prepare();
        File file = new File(TestConstants.TEST_DATA_INTEGRATION_DIR + "/tab_mapping_dataset2.tab");
        controller.setUploadedFiles(Arrays.asList(file));
        controller.setUploadedFilesFileName(Arrays.asList(file.getName()));

        controller.save();
        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                for (Long imageId: sharedImageIds) {
                    Image image = genericService.find(Image.class, imageId);
                    assertNull(image.getMappedDataKeyColumn());
                    assertNull(image.getMappedDataKeyValue());
                }
                return null;
            }
        });
    }

    public Image uploadImage(String filename, Project p) throws TdarActionException {
        ImageController controller = generateNewInitializedController(ImageController.class);
        controller.prepare();
        Image image = controller.getImage();
        image.setTitle(filename);
        image.setDescription(filename);
        controller.setProjectId(p.getId());
        image.markUpdated(getSessionUser());
        File file = new File(TestConstants.TEST_IMAGE_DIR + "/" + filename);
        addFileToResource(image, file);
        // controller.setUploadedFiles(Arrays.asList(file));
        // controller.setUploadedFilesFileName(Arrays.asList(filename));

        controller.save();
        return image;
    }

    @Override
    public String[] getDatabaseList() {
        // TODO Auto-generated method stub
        return null;
    }
}
