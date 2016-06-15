package org.tdar.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractResourceSearchITCase extends AbstractWithIndexIntegrationTestCase {

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
        latLong.setMinimumLongitude(-117.124);
        latLong.setMaximumLongitude(-117.101);
        latLong.setMaximumLatitude(35.791);
        latLong.setMinimumLatitude(33.354);
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

}
