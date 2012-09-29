package org.tdar.struts.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.tdar.core.bean.resource.ResourceType.DOCUMENT;
import static org.tdar.core.bean.resource.ResourceType.IMAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.CultureKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SiteTypeKeywordService;

@Transactional
public abstract class AbstractSearchControllerITCase extends AbstractControllerITCase {

    @Autowired
    protected LuceneSearchController controller;
    
    //FIXME:these counts will change often  - need to figure a better way to keep it in sync
    /*
     * execute the following sql against the test database to generate the count constants:
     * 
     * select 'protected static final int RESOURCE_COUNT_' || resource_type|| ' = ' || count(resource_type) || ';'  jabba from resource where status = 'ACTIVE' group by resource_type;
     */
    
    protected static final int RESOURCE_COUNT_DOCUMENT = 5;
    protected static final int RESOURCE_COUNT_ONTOLOGY = 1;
    protected static final int RESOURCE_COUNT_PROJECT = 11;
    protected static final int RESOURCE_COUNT_DATASET = 2;
    protected static final int RESOURCE_COUNT_CODING_SHEET = 4;    
    protected static final int RESOURCE_COUNT_IMAGE = 0;
    protected static final int RESOURCE_COUNT_SENSORY_DATA = 0;
    
    protected static final int RESOURCE_COUNT_ACTIVE = 23;
    protected static final int RESOURCE_COUNT_DRAFT = 0;
    protected static final int RESOURCE_COUNT_FLAGGED = 0;
    protected static final int RESOURCE_COUNT_DELETED = 0;

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;

    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    CultureKeywordService cultureKeywordService;
    @Autowired
    SiteTypeKeywordService siteTypeKeywordService;

    public TdarActionSupport getController() {
        return controller;
    }

    @Before
    public void reset() {
        searchIndexService.purgeAll();
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    protected Long setupDataset() {
        Dataset dataset = new Dataset();
        dataset.setTitle("precambrian dataset");
        dataset.setDescription("dataset description");
        dataset.markUpdated(getTestPerson());
        SiteTypeKeyword siteType = siteTypeKeywordService.findByLabel("Shell midden");
        dataset.getSiteTypeKeywords().add(siteType);
        assertFalse(siteType.getLabel().trim().startsWith(":"));
        assertFalse(siteType.getLabel().trim().endsWith(":"));
        TreeSet<ResourceCreator> rcs = new TreeSet<ResourceCreator>();
        ResourceCreator rc = new ResourceCreator();
        rc.setRole(ResourceCreatorRole.AUTHOR);
        rc.setResource(dataset);
        rc.setCreator(createAndSaveNewPerson("atest@Test.com", "abc"));
        rcs.add(rc);
        ResourceCreator rc2 = new ResourceCreator();
        rc2.setRole(ResourceCreatorRole.PREPARER);
        rc2.setResource(dataset);
        rc2.setCreator(getTestPerson().getInstitution());
        rcs.add(rc2);
        dataset.setResourceCreators(rcs);
        dataset.setStatus(Status.DELETED);
        genericService.save(dataset);

        Long datasetId = dataset.getId();
        return datasetId;
    }

    protected Long setupCodingSheet() {
        CodingSheet coding = new CodingSheet();
        coding.setTitle("precambrian codingsheet");
        coding.setDescription("codingsheet description");
        coding.markUpdated(getTestPerson());
        coding.setStatus(Status.ACTIVE);
        genericService.save(coding);

        Long codingId = coding.getId();

        return codingId;
    }

    protected Long setupImage() {
        Image img = new Image();
        img.setTitle("precambrian Test");
        img.setDescription("image description");
        img.markUpdated(getTestPerson());
        CultureKeyword label = cultureKeywordService.findByLabel("Folsom");
        CultureKeyword label2 = cultureKeywordService.findByLabel("Early Archaic");
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setMaximumLatitude(35.791);
        latLong.setMaximumLongitude(-117.124);
        latLong.setMinimumLatitude(33.354);
        latLong.setMinimumLongitude(-117.101);
        img.setLatitudeLongitudeBox(latLong);
        assertNotNull(label.getId());
        img.getCultureKeywords().add(label);
        img.getCultureKeywords().add(label2);
        img.setStatus(Status.DRAFT);
        genericService.save(img);
        genericService.save(latLong);
        Long imgId = img.getId();
        return imgId;
    }

    protected Long setupDatedDocument() {
        Document doc = new Document();
        doc.setTitle("Calendar Date Test");
        doc.setDescription("Ensure we can find a resource given temporal limits.");
        doc.markUpdated(getTestPerson());
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000);
        doc.getCoverageDates().add(cd);
        genericService.save(doc);
        Long docId = doc.getId();
        return docId;
    }

    protected boolean resultsContainId(Long id) {
        boolean found = false;
        for (Resource r : controller.getResources()) {
            logger.trace(r.getId() + " " + r.getResourceType());
            if (id.equals(r.getId()))
                found = true;
        }
        return found;
    }

    protected List<ResourceType> getInheritingTypes() {
        List<ResourceType> list = new ArrayList<ResourceType>();
        list.add(IMAGE);
        list.add(DOCUMENT);
        return list;
    }

    protected void doSearch(String query) {
        controller.setQuery(query);
        controller.performSearch();
        logger.info("search (" + controller.getQuery() + ") found: " + controller.getTotalRecords());
    }

    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(Resource.class);
    }

    protected void setStatuses(Status ... status) {
        List<Status> includedStatuses = Arrays.asList(status);
        controller.setIncludedStatuses(includedStatuses);
    }

    protected void setStatusAll() {
        controller.setIncludedStatuses(Collections.<Status> emptyList());
    }
    
    protected void logResults() {
        for(Resource r : controller.getResources()) {
            logger.debug("Search Result:" + r);
        }
    }
    
    protected void setResourceTypes(ResourceType ... resourceTypes) {
        controller.setResourceTypes(Arrays.asList(resourceTypes));
    }

}
