package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.service.resource.DatasetService;

public class DatasetITCase extends AbstractIntegrationTestCase {

    @Autowired
    private DatasetService datasetService;

    @Test
    @Rollback(true)
    public void testPersistence() {
        List<Dataset> existingDatasets = datasetService.findAll();
        Dataset dataset = createAndSaveNewDataset();
        assertNotSame(dataset.getId(), -1L);
        assertEquals("there should be one additional dataset now", existingDatasets.size() + 1, datasetService.count().intValue());
    }

    @Test
    @Rollback(true)
    public void testFindAllSorted() {
        List<Dataset> allSortedDatasets = datasetService.findAllSorted();
        assertEquals(allSortedDatasets.size(), datasetService.count().intValue());
        assertEquals("default sort order for datasets is title asc", allSortedDatasets, datasetService.findAllSorted("title asc"));
        List<Dataset> ascendingIdDatasets = datasetService.findAllSorted("id asc");
        List<Dataset> descendingIdDatasets = datasetService.findAllSorted("id desc");
        assertEquals(allSortedDatasets.size(), ascendingIdDatasets.size());
        assertEquals(allSortedDatasets.size(), descendingIdDatasets.size());
        // test ordering
        for (int i = 0; i < ascendingIdDatasets.size(); i++) {
            int descendingIdDatasetIndex = descendingIdDatasets.size() - (i + 1);
            assertEquals(ascendingIdDatasets.get(i), descendingIdDatasets.get(descendingIdDatasetIndex));
        }
    }

    // @Test
    // @Rollback
    // public void testDatasetDeleteDoesntCascade() throws InstantiationException, IllegalAccessException {
    // //create a dataset, save a source citation, make sure that deleting it doesn't delete (or try to delete) the documents in the citation.
    // Dataset dataset = createAndSaveNewDataset() ;
    // Document document = createAndSaveNewInformationResource(Document.class);
    // Long documentId = document.getId();
    // dataset.getSourceCitations().add(document);
    // logger.debug("about to delete a dataset that cites:{}", document);
    // datasetService.saveOrUpdate(dataset);
    // datasetService.delete(dataset);
    // Document document2 = resourceService.find(Document.class, documentId);
    // Assert.assertNotNull("formerly related document should not have been deleted", document2);
    // Assert.assertEquals(document, document2);
    //
    // }

}
