package org.tdar.struts.action.search;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;

public class AuthenticatedLuceneSearchControllerITCase extends AbstractSearchControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;

    @Before
    public void reset() {
        reindex();
        controller = generateNewInitializedController(LuceneSearchController.class, getAdminUser());
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject1() {
        Long imgId = setupImage();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(getInheritingTypes());
        List<Long> approvedCultureKeywordIds = new ArrayList<Long>();
        approvedCultureKeywordIds.add(9l);
        setStatuses(Status.DRAFT);
        controller.setApprovedCultureKeywordIds(approvedCultureKeywordIds);
        doSearch("");
        assertTrue("'Archaic' defined in parent project should be found in information resource", resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexed() {
        Long datasetId = setupDataset();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian");
        assertTrue(resultsContainId(datasetId));
    }

    @Test
    @Rollback(true)
    public void testDeletedMaterialsAreIndexedButYouCantSee() {
        controller = generateNewInitializedController(LuceneSearchController.class, getBasicUser());
        setIgnoreActionErrors(true);
        controller.setRecordsPerPage(50);
        Long datasetId = setupDataset();
        searchIndexService.index(genericService.find(Dataset.class, datasetId));
        controller.setResourceTypes(allResourceTypes);
        setStatuses(Status.DELETED);
        doSearch("precambrian");
        assertTrue(controller.getActionErrors().size() > 0);
    }

    @Test
    @Rollback(true)
    public void testDraftMaterialsAreIndexed() {
        Long imgId = setupImage();
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatuses(Status.DRAFT);
        doSearch("description");
        assertTrue(resultsContainId(imgId));
    }

    @Test
    @Rollback(true)
    public void testHierarchicalCultureKeywordsAreIndexed() {
        Long imgId = setupImage();
        logger.info("Created new image: " + imgId);
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(allResourceTypes);
        setStatusAll();
        doSearch("PaleoIndian");
        assertTrue(resultsContainId(imgId));
    }

}
