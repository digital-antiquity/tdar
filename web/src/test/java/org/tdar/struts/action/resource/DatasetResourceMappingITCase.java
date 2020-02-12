package org.tdar.struts.action.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.ColumnVisibility;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TestFileUploadHelper;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.dataset.ResourceMappingMetadataController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts_base.action.TdarActionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DatasetResourceMappingITCase extends AbstractAdminControllerITCase implements TestFileUploadHelper {

    private static final String TAB_MAPPING_DATASET_TAB = "tab_mapping_dataset.tab";

    @Autowired
    DatasetService datasetService;

    @Autowired
    AuthorizationService authorizationService;


    Dataset sharedDataset = null;
    List<Long> sharedImageIds;


    private ResourceCollection newResourceCollection(String name, String desc) {
        ResourceCollection rc = new ResourceCollection();
        rc.setName("test project");
        rc.setDescription("mapping test");
        rc.markUpdated(getSessionUser());
        genericService.save(rc);
        return rc;
    }

    private <T extends AbstractPersistableController & Preparable> T generateNewPreparedController(Class<T> controllerClass, Long id) throws TdarActionException{
        T controller = generateNewInitializedController(controllerClass, null);
        controller.prepare();
        if(id != null) {
            controller.setId(id);
            controller.prepare();
        }
        return controller;
    }


    @Test
    @Rollback(false)
    public void testDatasetMapping() throws Exception {

        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName("test project");
        resourceCollection.setDescription("mapping test");
        resourceCollection.markUpdated(getSessionUser());
        genericService.save(resourceCollection);
        Long rcid = resourceCollection.getId();

        Dataset dataset = setupAndLoadResource(TAB_MAPPING_DATASET_TAB, Dataset.class);
        Long datasetId = dataset.getId();
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.prepare();
        controller.setId(datasetId);
        controller.prepare();
        dataset = controller.getDataset();
        dataset.setTitle("test title");
        dataset.setDescription("test title");
        controller.getShares().add(resourceCollection);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        resourceCollection.setDataset(dataset);
        genericService.save(resourceCollection);

        Long image1_id = uploadImage("5127663428_42ef7f4463_b.jpg", resourceCollection).getId();
        Long image2_id = uploadImage("handbook_of_archaeology.jpg", resourceCollection).getId();
        sharedDataset = dataset;
        genericService.detachFromSession(dataset);
        // do search for something in another column
        sharedImageIds = Arrays.asList(image1_id, image2_id);
        genericService.synchronize();

        resourceCollection = resourceCollectionService.find(rcid);

        assertNotNull("expecting collection with mapped dataset", resourceCollection.getDataset());
        //assertEquals(3, projectService.findAllResourcesInProject(project).size());
        assertEquals(3, resourceCollection.getManagedResources().size());

        ResourceMappingMetadataController columnController = generateNewInitializedController(ResourceMappingMetadataController.class);
        columnController.setId(dataset.getId());
        dataset = null;
        columnController.prepare();
        columnController.editColumnMetadata();
        assertEmpty("expecting zero action errors for editColumnMetadata", columnController.getActionErrors());
        boolean seenMappingColumn = false;

        List<DataTableColumn> dataTableColumns = columnController.getPersistable().getDataTables().iterator().next().getDataTableColumns();
        List<DataTableColumn> dataTableColumns_ = new ArrayList<DataTableColumn>();
        for (DataTableColumn column_ : dataTableColumns) {
            DataTableColumn column = (DataTableColumn) BeanUtils.cloneBean(column_);
            dataTableColumns_.add(column);
            if (column.getDisplayName().equals("mapping")) {
                logger.debug("col: {}", column);
                seenMappingColumn = true;
                column.setMappingColumn(true);
                column.setIgnoreFileExtension(false);
            }
        }
        columnController.setDataTableColumns(dataTableColumns_);
        assertTrue(seenMappingColumn);
        columnController.setAsync(false);
        columnController.saveColumnMetadata();

        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                for (Long imageId : sharedImageIds) {
                    Image image = genericService.find(Image.class, imageId);
                    logger.info(String.format("image mapping: %s - %s", image.getMappedDataKeyColumn(), image.getMappedDataKeyValue()));
                    String assertmsg = String.format("image should have mapped data key/value: %s", image);
                    assertNotNull(assertmsg, image.getMappedDataKeyColumn());
                    assertNotNull(assertmsg, image.getMappedDataKeyValue());
                }
                return null;
            }
        });
    }


    private DataTableColumn getColumnByName(List<DataTableColumn> columns, String name) {
        return columns.stream().filter(col ->
                name.equals(col.getName())
                || name.equals(col.getDisplayName())
        ).collect(Collectors.toList()).get(0);
    }


    // FIXME: This test is virtually identical to testDatasetMapping
    @Ignore("only run this test in mimbres hotfix branch for now")
    @Test
    @Rollback(false)
    public void testDatasetMappingWithConfidentialColumns() throws Exception {

        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName("test project");
        resourceCollection.setDescription("mapping test");
        resourceCollection.markUpdated(getSessionUser());
        genericService.save(resourceCollection);
        Long rcid = resourceCollection.getId();

        Dataset dataset = setupAndLoadResource(TAB_MAPPING_DATASET_TAB, Dataset.class);
        Long datasetId = dataset.getId();
        dataset = null;
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.prepare();
        controller.setId(datasetId);
        controller.prepare();
        dataset = controller.getDataset();
        dataset.setTitle("test title");
        dataset.setDescription("test title");
        controller.getShares().add(resourceCollection);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        resourceCollection.setDataset(dataset);
        genericService.save(resourceCollection);

        Long image1_id = uploadImage("5127663428_42ef7f4463_b.jpg", resourceCollection).getId();
        Long image2_id = uploadImage("handbook_of_archaeology.jpg", resourceCollection).getId();
        sharedDataset = dataset;
        genericService.detachFromSession(dataset);
        // do search for something in another column
        sharedImageIds = Arrays.asList(image1_id, image2_id);
        genericService.synchronize();

        resourceCollection = resourceCollectionService.find(rcid);

        assertNotNull("expecting collection with mapped dataset", resourceCollection.getDataset());
        //assertEquals(3, projectService.findAllResourcesInProject(project).size());
        assertEquals(3, resourceCollection.getManagedResources().size());

        ResourceMappingMetadataController columnController = generateNewInitializedController(ResourceMappingMetadataController.class);
        columnController.setId(dataset.getId());
        dataset = null;
        columnController.prepare();
        columnController.editColumnMetadata();
        assertEmpty("expecting zero action errors for editColumnMetadata", columnController.getActionErrors());
        boolean seenMappingColumn = false;

        List<DataTableColumn> dataTableColumns = columnController.getPersistable().getDataTables().iterator().next().getDataTableColumns();
        List<DataTableColumn> dataTableColumns_ = new ArrayList<DataTableColumn>();
        for (DataTableColumn column_ : dataTableColumns) {
            DataTableColumn column = (DataTableColumn) BeanUtils.cloneBean(column_);
            dataTableColumns_.add(column);
            if (column.getDisplayName().equals("mapping")) {
                logger.debug("col: {}", column);
                seenMappingColumn = true;
                column.setMappingColumn(true);
                column.setIgnoreFileExtension(false);
            }
        }

        getColumnByName(dataTableColumns_, "col1text").setVisible(ColumnVisibility.CONFIDENTIAL);


        columnController.setDataTableColumns(dataTableColumns_);
        assertTrue(seenMappingColumn);
        columnController.setAsync(false);
        columnController.saveColumnMetadata();

        setVerifyTransactionCallback((TransactionCallback<Image>) status -> {
            for (Long imageId : sharedImageIds) {
                Image image = genericService.find(Image.class, imageId);
                logger.info(String.format("image mapping: %s - %s", image.getMappedDataKeyColumn(), image.getMappedDataKeyValue()));
                String assertmsg = String.format("image should have mapped data key/value: %s", image);
                assertNotNull(assertmsg, image.getMappedDataKeyColumn());
                assertNotNull(assertmsg, image.getMappedDataKeyValue());
                assertThat(authorizationService.canViewConfidentialInformation(getUser(), image), is(false));
            }
            return null;
        });
    }



    @Test
    @Rollback(false)
    public void testDatasetMappingWithReplace() throws Exception {
        testDatasetMapping();
        Dataset dataset = sharedDataset;

        replaceFile("tab_mapping_dataset2.tab", TAB_MAPPING_DATASET_TAB, Dataset.class, dataset.getId());

        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                for (Long imageId : sharedImageIds) {
                    Image image = genericService.find(Image.class, imageId);
                    assertNull(image.getMappedDataKeyColumn());
                    assertNull(image.getMappedDataKeyValue());
                }
                return null;
            }
        });
    }

    public Image uploadImage(String filename, Project p) throws TdarActionException, FileNotFoundException {
        ImageController controller = generateNewInitializedController(ImageController.class);
        controller.prepare();
        Image image = controller.getImage();
        image.setTitle(filename);
        image.setDescription(filename);
        controller.setProjectId(p.getId());
        image.markUpdated(getSessionUser());
        File file = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, filename);
        addFileToResource(image, file);
        // controller.setUploadedFiles(Arrays.asList(file));
        // controller.setUploadedFilesFileName(Arrays.asList(filename));

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        return image;
    }

    public Image uploadImage(String filename, ResourceCollection rc) throws TdarActionException, FileNotFoundException{
        ImageController controller = generateNewPreparedController(ImageController.class, null);
        Image image = controller.getImage();
        image.setTitle(filename);
        image.setDescription(filename);
        controller.getShares().add(rc);
        image.setTitle(filename);
        image.setDescription(filename);
        image.markUpdated(getSessionUser());
        File file = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, filename);
        addFileToResource(image, file);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        return image;
    }

    @Override
    public String getTestFilePath() {
        return TestConstants.TEST_DATA_INTEGRATION_DIR;
    }
}
