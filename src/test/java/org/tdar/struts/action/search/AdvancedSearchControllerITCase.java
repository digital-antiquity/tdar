package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.SortOption;

@Transactional
public class AdvancedSearchControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    ResourceService resourceService;

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
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known site name kwd", resultsContainId(2420L));
    }

    @Test
    @Rollback
    public void testApprovedSiteTypeKeywords() {
        controller.setApprovedSiteTypeKeywordIds(Arrays.asList(256l));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known site type kwd", resultsContainId(262L));
    }

    @Rollback
    public void testSiteTypeKeywords() {
        // FIXME: add a test for this
        // controller.setUncontrolledSiteTypeKeywords();
        // doSearch("");
        // assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        // assertTrue("expected to find document that uses known site type kwd", resultsContainId(262L));
    }

    @Test
    @Rollback
    public void testMaterialKeywords() {
        controller.setMaterialKeywordIds(Arrays.asList(2L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        assertTrue("expected to find document that uses known material kwd", resultsContainId(3805L));
    }

    @Test
    @Rollback
    public void testCulture() {
        controller.setUncontrolledCultureKeywords(Arrays.asList("Sinagua"));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
        // looking for project that has document that has known culture
        assertTrue("expected to find project that uses known culture", resultsContainId(4279L));
    }

    @Test
    @Rollback
    public void testApprovedCulture() {
        controller.setApprovedCultureKeywordIds(Arrays.asList(19L));
        doSearch("");
        assertTrue("we should get back at least one hit", !controller.getResults().isEmpty());
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
        assertFalse("we should get back at least one hit", !controller.getResults().isEmpty());
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
    public void testResultCounts() {
        for (ResourceType type : ResourceType.values()) {
            for (Status status : Status.values()) {
                assertResultCount(type, status);
            }
        }
    }

    // compare the counts returned from searchController against the counts we get from the database
    private void assertResultCount(ResourceType resourceType, Status status) {
        long expectedCount = resourceService.getResourceCount(resourceType, status);
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        setResourceTypes(resourceType);
        setStatuses(status);
        doSearch("");
        String msg = String.format("ResourceType:%s  Status:%s  Count-expected:%s", resourceType, status, expectedCount);
        Assert.assertEquals(msg, expectedCount, controller.getTotalRecords());
    }

    private void setSortThenCheckFirstResult(String message, SortOption sortField, Long projectId, Long expectedId) {
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setSortField(sortField);
        controller.setProjectIds(Arrays.asList(projectId));
        doSearch("");
        logger.info("{}", controller.getResults());
        Resource found = (Resource) controller.getResults().iterator().next();
        logger.info("{}", found);
        Assert.assertEquals(message, expectedId, found.getId());
    }

    // note: relevance sort broken out into SearchRelevancyITCase
    @Test
    @Rollback
    public void testSortFieldTitle() {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.setStatus(Status.ACTIVE);
        p.markUpdated(getUser());
        List<String> titleList = Arrays.asList(new String[] { "a", "b", "c", "d" });
        genericService.save(p);
        for (String title : titleList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setTitle(title);
            doc.setProject(p);
            doc.setStatus(Status.ACTIVE);
            genericService.save(doc);
            if (alphaId == -1) {
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();
        setSortThenCheckFirstResult("sorting by title asc", SortOption.TITLE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by title desc", SortOption.TITLE_REVERSE, p.getId(), omegaId);
    }

    @Test
    @Rollback
    public void testSortFieldDateCreated() {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.markUpdated(getUser());
        List<Integer> dateList = Arrays.asList(new Integer[] { null, 1, 2, 3, 4, 5, 19, 39 });
        genericService.save(p);
        for (Integer date : dateList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setDateCreated(date);
            doc.setTitle("hello" + date);
            doc.setProject(p);
            genericService.save(doc);
            if (alphaId == -1) {
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();

        setSortThenCheckFirstResult("sorting by datecreated asc", SortOption.DATE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by datecreated desc", SortOption.DATE_REVERSE, p.getId(), omegaId);
    }

    @Test
    @Rollback
    public void testResourceCount() {
        // fixme: remove this query. it's only temporary to ensure that my named query is working
        long count = resourceService.getResourceCount(ResourceType.PROJECT, Status.ACTIVE);
        assertTrue(count > 0);
    }
}
