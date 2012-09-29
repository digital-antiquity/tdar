package org.tdar.struts.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SearchIndexService;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Before
    public void reset() {
        reindex();
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback
    public void testSiteNameKeywords() {
        controller.setSiteNameKeywords(Arrays.asList("Atsinna"));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        assertTrue("expected to find document that uses known site name kwd", resultsContainId(2420L));
    }

    @Test
    @Rollback
    public void testApprovedSiteTypeKeywords() {
        controller.setApprovedSiteTypeKeywordIds(Arrays.asList(256l));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        assertTrue("expected to find document that uses known site type kwd", resultsContainId(262L));
    }

    @Rollback
    public void testSiteTypeKeywords() {
        // FIXME: add a test for this
        // controller.setUncontrolledSiteTypeKeywords();
        // doSearch("");
        // assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        // assertTrue("expected to find document that uses known site type kwd", resultsContainId(262L));
    }

    @Test
    @Rollback
    public void testMaterialKeywords() {
        controller.setMaterialKeywordIds(Arrays.asList(2L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        assertTrue("expected to find document that uses known material kwd", resultsContainId(3805L));
    }

    @Test
    @Rollback
    public void testCulture() {
        controller.setUncontrolledCultureKeywords(Arrays.asList("Sinagua"));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        // looking for project that has document that has known culture
        assertTrue("expected to find project that uses known culture", resultsContainId(4279L));
    }

    @Test
    @Rollback
    public void testApprovedCulture() {
        controller.setApprovedCultureKeywordIds(Arrays.asList(19L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResources().isEmpty());
        assertTrue("expected to find document that uses known material kwd", resultsContainId(2420L));
    }

    @Test
    @Rollback
    public void testLatLong() {
        Long imgId = setupImage();
        controller.setMaxx(-112.0330810546875);
        controller.setMaxy(33.465816745730024);
        controller.setMinx(-112.11273193359375);
        controller.setMiny(33.42571077612917);
        reindex();
        setStatuses(Status.DRAFT);
        doSearch("");
        assertFalse("we should get back at least one hit", !controller.getResources().isEmpty());
        assertFalse("expected to find document that uses known material kwd", resultsContainId(imgId));
    }

    @Test
    @Rollback
    public void testProjectIds() {
        Long projectId = 139L;
        controller.setProjectIds(Arrays.asList(projectId));
        controller.setResourceTypes(Arrays.asList(ResourceType.PROJECT));
        doSearch("");
        resultsContainId(projectId);
    }

    @Test
    @Rollback
    public void testTitle() {
        Long projectId = 139L;
        String projectTitle = "Rudd Creek Archaeological Project";
        controller.setTitle(projectTitle);
        doSearch("");
        resultsContainId(projectId);
    }

    // @Test
    // TODO: currently covered in SearchITCase webtest, may want to add as a controller test later
    @Rollback
    public void testUncontrolledSiteTypeKeywords() {
        fail("not implemented");
    }

    @Test
    @Rollback
    public void testSearchSubmitterIds() {
        Long submitterId = 6L;
        String resourceTitle = "Durrington Walls Humerus Dataset";
        Long resourceId = 3074L;
        setResourceTypes(ResourceType.DATASET);
        controller.setSearchSubmitterIds(Arrays.asList(submitterId));
        doSearch("");
        resultsContainId(resourceId);
    }

    @Test
    @Rollback
    public void testSearchContributorIds() {
        Long contributorId = 100L;
        // FIXME: pull these out into constants
        Long projectId = 139L;
        String projectTitle = "Rudd Creek Archaeological Project";
        setResourceTypes(ResourceType.PROJECT);
        controller.setSearchContributorIds(Arrays.asList(contributorId));
        doSearch("");
        resultsContainId(projectId);
    }

    @Test
    @Rollback
    public void testIncludedStatuses() {
        assertResultCount(Status.ACTIVE, RESOURCE_COUNT_ACTIVE);
        assertResultCount(Status.DRAFT, RESOURCE_COUNT_DRAFT);
        assertResultCount(Status.FLAGGED, RESOURCE_COUNT_FLAGGED);
        assertResultCount(Status.DELETED, RESOURCE_COUNT_DELETED);

    }

    @Test
    @Rollback
    public void testFilterByResourceTypes() {
        assertResultCount(ResourceType.CODING_SHEET, RESOURCE_COUNT_CODING_SHEET);
        assertResultCount(ResourceType.DATASET, RESOURCE_COUNT_DATASET);
        assertResultCount(ResourceType.DOCUMENT, RESOURCE_COUNT_DOCUMENT);
        assertResultCount(ResourceType.IMAGE, RESOURCE_COUNT_IMAGE);
        assertResultCount(ResourceType.SENSORY_DATA, RESOURCE_COUNT_SENSORY_DATA);
        assertResultCount(ResourceType.ONTOLOGY, RESOURCE_COUNT_ONTOLOGY);
        assertResultCount(ResourceType.PROJECT, RESOURCE_COUNT_PROJECT);
    }

    private void assertResultCount(ResourceType type, long count) {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        setResourceTypes(type);
        doSearch("");
        Assert.assertEquals("record count expected for " + type + ":" + count, count, controller.getTotalRecords());
    }

    private void assertResultCount(Status status, long expected) {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        setStatuses(status);
        doSearch("");
        Assert.assertEquals("record count expected for " + status + ":" + expected, expected, controller.getTotalRecords());
    }

    private void setSortThenCheckFirstResult(String message, List<ResourceType> resourceTypes, String sortField, Long expectedId) {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setSortField(sortField);
        controller.setResourceTypes(resourceTypes);
        doSearch("");
        Assert.assertEquals(message, expectedId, controller.getResources().iterator().next().getId());
    }

    // note: relevance sort broken out into SearchRelevancyITCase
    @Test
    @Rollback
    public void testSortFieldTitle() {
        Long alphaProjectId = 3479L;
        Long omegaProejectId = 4279L;
        setSortThenCheckFirstResult("sorting by title asc", allResourceTypes, "title_sort", alphaProjectId);
        setSortThenCheckFirstResult("sorting by title desc", allResourceTypes, "-title_sort", omegaProejectId);
    }

//    @Test
    @Rollback
    // FIXME: currently breaks because test dataset date_created contain nulls(can't check earliest) and dupes(cant check most recent) .
    public void testSortFieldDateCreated() {
        List<ResourceType> informationResourceTypes = new ArrayList<ResourceType>(allResourceTypes);
        informationResourceTypes.remove(ResourceType.PROJECT);
        Long oldestProjectId = 3088L;
        Long newestProjectId = 4287L;
        setSortThenCheckFirstResult("sorting by datecreated asc", informationResourceTypes, "dateCreated", oldestProjectId);
        setSortThenCheckFirstResult("sorting by datecreated desc", informationResourceTypes, "-dateCreated", newestProjectId);
    }

}
