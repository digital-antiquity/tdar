package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;

public class DatasetITCase extends AbstractIntegrationTestCase {

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


  @Test
  @Rollback(true)
  public void testEqualsHashCode() {
    List<Dataset> datasets = datasetService.findAll();
    for (Dataset dataset : datasets) {
      Dataset freshDataset = createAndSaveNewDataset();
      assertFalse(dataset.equals(freshDataset));
      assertFalse(dataset.hashCode() == freshDataset.hashCode());
      freshDataset = new Dataset();
      freshDataset.setTitle("fresh dataset");
      assertFalse(dataset.equals(freshDataset));
      assertFalse(dataset.hashCode() == freshDataset.hashCode());
      freshDataset.setId(dataset.getId());
      assertEquals(dataset, freshDataset);
      assertEquals(dataset.hashCode(), freshDataset.hashCode());
      // sanity check on other subtypes
      for (Class<? extends Resource> resourceSubtype : new Class[] { Ontology.class, Document.class, Image.class, CodingSheet.class, Project.class }) {
        for (Resource r : genericService.findAll(resourceSubtype)) {
          assertFalse(dataset.equals(r));
          assertFalse(dataset.hashCode() == r.hashCode());
        }
      }
    }
  }

}
