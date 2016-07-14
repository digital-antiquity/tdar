package org.tdar.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractResourceSearchITCase extends AbstractWithIndexIntegrationTestCase {


    public static final String REASON = "because";
    public static final String _33_CU_314 = "33-Cu-314";
    public static final String CONSTANTINOPLE = "Constantinople";
    public static final String ISTANBUL = "Istanbul";
    public static final String L_BL_AW = "l[]bl aw\\";

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ResourceSearchService resourceSearchService;

    @Autowired
    EntityService entityService;

    protected AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();

    protected Long setupDataset() {
        return setupDataset(Status.DELETED);
    }

    protected Long setupDataset(Status status) {
        Dataset dataset = new Dataset();
        dataset.setTitle("precambrian dataset");
        dataset.setDescription("dataset description");
        dataset.markUpdated(getUser());
        SiteTypeKeyword siteType = genericKeywordService.findByLabel(SiteTypeKeyword.class, "Shell midden");
        dataset.getSiteTypeKeywords().add(siteType);
        assertFalse(siteType.getLabel().trim().startsWith(":"));
        assertFalse(siteType.getLabel().trim().endsWith(":"));
        genericService.saveOrUpdate(dataset);
        ResourceCreator rc = new ResourceCreator(createAndSaveNewPerson("atest@Test.com", "abc"), ResourceCreatorRole.CREATOR);
        ResourceCreator rc2 = new ResourceCreator(getUser().getInstitution(), ResourceCreatorRole.PREPARER);
        dataset.getResourceCreators().add(rc);
        dataset.getResourceCreators().add(rc2);
        dataset.setStatus(status);
        genericService.saveOrUpdate(dataset);

        Long datasetId = dataset.getId();
        return datasetId;
    }

    protected Long setupCodingSheet() {
        CodingSheet coding = new CodingSheet();
        coding.setTitle("precambrian codingsheet");
        coding.setDescription("codingsheet description");
        coding.markUpdated(getUser());
        coding.setStatus(Status.ACTIVE);
        genericService.save(coding);

        Long codingId = coding.getId();

        return codingId;
    }

    protected Long setupImage() {
        return setupImage(getUser());
    }

    protected Long setupImage(TdarUser user) {
        Image img = new Image();
        img.setTitle("precambrian Test");
        img.setDescription("image description");
        img.markUpdated(user);
        CultureKeyword label = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword label2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setWest(-117.124);
        latLong.setEast(-117.101);
        latLong.setNorth(35.791);
        latLong.setSouth(33.354);
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
        doc.setDate(1000);
        doc.setProject(Project.NULL);
        doc.setDescription("Ensure we can find a resource given temporal limits.");
        doc.markUpdated(getUser());
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000);
        doc.getCoverageDates().add(cd);
        genericService.save(doc);
        Long docId = doc.getId();
        return docId;
    }

    public SearchResult<Resource> doSearch(String text, TdarUser user, SearchParameters params_, ReservedSearchParameters reservedParams,
            SortOption option) throws ParseException, SolrServerException, IOException {
        asqo = new AdvancedSearchQueryObject();
        SearchParameters params = params_;
        if (params == null) {
            params = new SearchParameters();
        }
        if (StringUtils.isNotBlank(text)) {
            params.getAllFields().add(text);
        }
        SearchResult<Resource> result = new SearchResult<>();
        result.setSortField(option);
        asqo.getSearchParameters().add(params);
        asqo.setReservedParams(reservedParams);

        resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());
        return result;
    }

    public SearchResult<Resource> doSearch(String text, TdarUser user, SearchParameters params_, ReservedSearchParameters reservedParams)
            throws ParseException, SolrServerException, IOException {
        return doSearch(text, user, params_, reservedParams, null);
    }

    public SearchResult<Resource> doSearch(String text) throws ParseException, SolrServerException, IOException {
        return doSearch(text, null, null, null, null);
    }

    public boolean resultsContainId(SearchResult<? extends Resource> result, long l) {
        List<Long> extractIds = PersistableUtils.extractIds(result.getResults());
        return extractIds.contains(l);
    }

    protected void updateAndIndex(Indexable doc) throws SolrServerException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }


    protected Project sparseProject(Long id) {
        Project project = new Project(id, "sparse");
        return project;
    }

    protected SharedCollection sparseCollection(Long id) {
        SharedCollection collection = new SharedCollection();
        collection.setId(id);
        return collection;
    }

    protected void setSortThenCheckFirstResult(String message, SortOption sortField, Long projectId, Long expectedId) throws ParseException, SolrServerException, IOException {
        SearchParameters sp = new SearchParameters();
        sp.getProjects().add(sparseProject(projectId));
        SearchResult<Resource> result = doSearch(null, null, sp, null, sortField);
//        logger.info("{}", result.getResults());
        for (Resource d: result.getResults()) {
            InformationResource ir = (InformationResource)d;
            logger.debug("{} {} {}", ir.getDate(),ir.getId(), ir);
        }
        Indexable found = result.getResults().iterator().next();
        logger.info("{}", found);
        Assert.assertEquals(message, expectedId, found.getId());
    }


    protected Document createDocumentWithContributorAndSubmitter() throws InstantiationException, IllegalAccessException, SolrServerException, IOException {
        TdarUser submitter = new TdarUser("E", "deVos", "ecd@tdar.net");
        genericService.save(submitter);
        Document doc = createAndSaveNewInformationResource(Document.class, submitter);
        ResourceCreator rc = new ResourceCreator(new Person("K", "deVos", "kellyd@tdar.net"), ResourceCreatorRole.AUTHOR);
        genericService.save(rc.getCreator());
        // genericService.save(rc);
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        return doc;
    }


    protected Document createDocumentWithDates(int i, int j) throws InstantiationException, IllegalAccessException {
        Document document = createAndSaveNewInformationResource(Document.class);
        CoverageDate date = new CoverageDate(CoverageType.CALENDAR_DATE, i, j);
        document.getCoverageDates().add(date);
        genericService.saveOrUpdate(date);
        return document;
    }


    public SearchResult<Resource> performSearch(String term, TdarUser user, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, null, null, null, null, user, null, null, max);
    }

    public SearchResult<Resource> performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, projectId, collectionId, includeParent, categoryId, user, reservedSearchParameters, null, max);
    }

    public SearchResult<Resource> performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, GeneralPermissions permission, int max) throws ParseException, SolrServerException, IOException {
        SearchResult<Resource> result = new SearchResult<>(max);
        logger.debug("{}, {}", resourceSearchService, MessageHelper.getInstance());
        ResourceLookupObject rl = new ResourceLookupObject(term, projectId, includeParent, collectionId, categoryId, permission, reservedSearchParameters);
        resourceSearchService.lookupResource(user, rl, result, MessageHelper.getInstance());
        return result;
    }

    public void setupTestDocuments() throws InstantiationException, IllegalAccessException, SolrServerException, IOException {
        String[] titles = {
                "Preliminary Archeological Investigation at the Site of a Mid-Nineteenth Century Shop and Yard Complex Associated With the Belvidere and Delaware Railroad, Lambertville, New Jersey",
                "The James Franks Site (41DT97): Excavations at a Mid-Nineteenth Century Farmstead in the South Sulphur River Valley, Cooper Lake Project, Texas",
                "Archeological and Architectural Investigation of Public, Residential, and Hydrological Features at the Mid-Nineteenth Century Quintana Thermal Baths Ponce, Puerto Rico",
                "Final Report On a Phased Archaeological Survey Along the Ohio and Erie Canal Towpath in Cuyahoga Valley NRA, Summit and Cuyahoga Counties, Ohio",
                "Archeological Investigation at the Lock 33 Complex, Chesapeake and Ohio Canal",
                "Arthur Patterson Site, a Mid-Nineteenth Century Site, San Jacinto County" };
        for (String title : titles) {
            Document document = createAndSaveNewInformationResource(Document.class, getBasicUser(), title);
            searchIndexService.index(document);
        }

    }

}
