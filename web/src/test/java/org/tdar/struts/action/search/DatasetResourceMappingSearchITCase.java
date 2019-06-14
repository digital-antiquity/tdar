package org.tdar.struts.action.search;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TestFileUploadHelper;
import org.tdar.struts.action.dataset.DatasetController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.tdar.TestConstants.TEST_ROOT_DIR;


public class DatasetResourceMappingSearchITCase extends AbstractAdminControllerITCase implements TestFileUploadHelper {

    @Override
    public String getTestFilePath() {
        return Paths.get(TEST_ROOT_DIR, "dataset/dataset-search").toString();
    }


    Dataset setupAndLoadDataset(String path) {
        Dataset dataset = null;
        try {
            dataset = setupAndLoadResource(path, Dataset.class);
            Long datasetId = dataset.getId();
            dataset = null;
            DatasetController controller = generateNewInitializedController(DatasetController.class);
            controller.prepare();
            controller.setId(datasetId);
            controller.prepare();
            dataset = controller.getDataset();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return dataset;
    }

    ResourceCollection newResourceCollection(String path, String name, String desc) {
        ResourceCollection rc = new ResourceCollection();
        rc.setName(name);
        rc.setDescription(desc);
        rc.markUpdated(getSessionUser());
        genericService.save(rc);
        return rc;
    }

    /**
     * Ensure test resources are available.
     */
    @Test
    public void testSanity() {
        getLogger().debug("Test Path is {}", getTestFilePath());
        File testDir = Paths.get(getTestFilePath()).toFile();
        Collection<File> files = FileUtils.listFiles(testDir, new String[]{"png"}, false);
        assertNotEmpty("dataset-search directory should have image files", files);
        assertThat(files, hasSize(15));
    }

    DataTableColumn lookupColumn(Dataset ds, String displayName){
        DataTableColumn col = ds.getDataTables().stream()
                .flatMap( (dt) -> dt.getDataTableColumns().stream() )
                .filter(dtc -> dtc.getDisplayName().toLowerCase().equals(displayName.toLowerCase()))
                .findFirst().get();
        return col;
    }

    long save(Persistable p) {
        genericService.save(p);
        return p.getId();
    }

    @Test
    public void testDatasetSearch() throws Exception {
        // upload dataset
        Path datasetPath  = Paths.get(getTestFilePath(), "public_images_descriptions.xlsx");
        Dataset dataset = setupAndLoadDataset(datasetPath.toString());
        assertNotNull(dataset);

        // set columns
        DataTableColumn dtc = lookupColumn(dataset, "color_image_filename");
        assertNotNull(dtc);
        dtc.setMappingColumn(true);
        long id = save(dtc);
        assertThat(id, not(equalTo(-1)));


        // create resource collection and enable mapping


        // upload images and assign to collection

        // create dataset
    }





}
