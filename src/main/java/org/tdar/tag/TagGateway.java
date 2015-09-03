package org.tdar.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.FullTextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.search.ReservedSearchParameters;
import org.tdar.core.service.search.SearchParameters;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.tag.Query.What;
import org.tdar.tag.Query.When;
import org.tdar.tag.Query.Where;
import org.tdar.tag.SearchResults.Meta;
import org.tdar.tag.SearchResults.Results;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * $Id$
 * 
 * Implementation of the TAG Gateway service.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
@Component
@WebService(portName = "TagGateway", serviceName = "TagGatewayService",
        targetNamespace = "http://archaeologydataservice.ac.uk/tag/schema",
        endpointInterface = "org.tdar.tag.TagGatewayPort")
public class TagGateway implements TagGatewayPort, QueryFieldNames {

    private static final transient Logger logger = LoggerFactory.getLogger(TagGateway.class);
    private static final Element XSLT;
    private String version;
    @Autowired
    private SearchService searchService;

    @Autowired
    private AuthorizationService authenticationAndAuthorizationService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UrlService urlService;
    private SiteTypeQueryMapper siteTypeQueryMapper;

    static { // initialize the XSLT for inclusion in the SOAP response
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(TagGateway.class.getResourceAsStream("/wsdl/tagPortal.xsl"));
            XSLT = d.getDocumentElement();
        } catch (Exception e) {
            String msg = "Could not initialize the TagGateway service. " +
                    "Error reading or parsing tagPortal.xsl.";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public SearchResults getTopRecords(String sessionId, Query query, int numberOfRecords) {
        SearchResults searchRes = new SearchResults();
        logger.debug("TAGGateway: Called getTopRecords...");
        try {
            // grab relevant parts of the query
            What what = query.getWhat();
            Where where = query.getWhere();
            When when = query.getWhen();
            String freetext = query.getFreetext();

            ResourceQueryBuilder qb = buildSearchQuery(what, where, when, freetext);
            FullTextQuery q = null;
            List<Project> resources = Collections.emptyList();
            int totalRecords = 0;
            int firstRec = 0;
            int lastRec = 0;

            // actually perform the search against the index
            try {
                q = searchService.search(qb);
                logger.info(qb.generateQueryString());
            } catch (ParseException e) {
                logger.warn("Could not parse supplied query.", e);
            }

            // determine if we need to make a second projection query to get all
            // of the record ids
            boolean totalExceedsRequested = false;

            if (q != null) {
                totalRecords = q.getResultSize();
                if (totalRecords > 0) {
                    firstRec = 1;
                }
                q.setMaxResults(numberOfRecords);
                if (totalRecords > numberOfRecords) {
                    totalExceedsRequested = true;
                    lastRec = numberOfRecords;
                } else {
                    lastRec = totalRecords;
                }
                logger.debug("Number of records requested: " + numberOfRecords);
                logger.debug("Total number of records: " + totalRecords);
                resources = q.list();
            }

            int recordsReturned = totalRecords == 0 ? 0 : (lastRec - firstRec) + 1;

            Results tdarRes = createResults(sessionId, searchRes, totalRecords, firstRec, lastRec, recordsReturned);

            // project ids used to grab the integratable datasets
            List<Long> projIds = processProjects(resources, totalExceedsRequested, tdarRes);
            getAllProjectIds(qb, totalExceedsRequested, projIds);

            // build a link to our search page listing the integratable datasets
            if (projectService.containsIntegratableDatasets(projIds)) {
                tdarRes.setContainsIntegratableData(true);
                tdarRes.setIntegratableDatasetUrl(buildIntegratableDatasetUrl(projIds));
            }
        } catch (Throwable t) {
            logger.error("issue in TAG: {}", t);
        }
        return searchRes;
    }

    private void getAllProjectIds(ResourceQueryBuilder qb, boolean totalExceedsRequested, List<Long> projIds) {
        if (totalExceedsRequested) { // query again to get all of the projectIds
            try {
                FullTextQuery idq = searchService.search(qb);
                idq.setProjection("id");
                @SuppressWarnings("unchecked")
                List<Object[]> idresults = idq.list();
                for (Object[] idresult : idresults) {
                    projIds.add((Long) idresult[0]);
                }
            } catch (ParseException e) {
                logger.warn("Could not parse supplied query.", e);
            }
        }
    }

    private Results createResults(String sessionId, SearchResults searchRes, int totalRecords, int firstRec, int lastRec, int recordsReturned) {
        // create results to be returned
        // FIXME: maybe this should go into the ObjectFactory?
        Meta resMeta = new Meta();
        searchRes.setMeta(resMeta);
        Results tdarRes = new Results();
        searchRes.setResults(tdarRes);

        resMeta.setProviderName(TdarConfiguration.getInstance().getSiteAcronym());
        resMeta.setSessionID(sessionId);
        resMeta.setFirstRecord(firstRec);
        resMeta.setLastRecord(lastRec);
        resMeta.setTotalRecords(totalRecords);
        resMeta.setRecordsReturned(recordsReturned);
        return tdarRes;
    }

    private List<Long> processProjects(List<Project> resources, boolean totalExceedsRequested, Results tdarRes) {
        List<Long> projIds = new ArrayList<Long>();

        for (Project p : resources) {
            if (!totalExceedsRequested) {
                projIds.add(p.getId());
            }
            ResultType res = new ResultType();
            res.setIdentifier(p.getId().toString());
            res.setTitle(p.getTitle());
            res.setUrl(UrlService.absoluteUrl(p));
            Set<ResourceCreator> preparers = p.getResourceCreators(ResourceCreatorRole.PREPARER);
            if (preparers.isEmpty()) {
                res.setPublisher("");
            } else {
                String preparer = preparers.iterator().next().getCreator().getProperName();
                res.setPublisher(preparer);
            }

            res.setSummary(p.getShortenedDescription());
            tdarRes.getResult().add(res);
        }
        return projIds;
    }

    private ResourceQueryBuilder buildSearchQuery(What what, Where where, When when, String freetext) {
        // build the query from the supplied parameters
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        SearchParameters params = new SearchParameters();
        ReservedSearchParameters reserved = new ReservedSearchParameters();
        reserved.setResourceTypes(Arrays.asList(ResourceType.PROJECT));
        reserved.setStatuses(Arrays.asList(Status.ACTIVE));
        
        if (what != null) {
            List<String> terms = new ArrayList<String>();
            for (SubjectType type : what.getSubjectTerm()) {
                for (String stTerm : siteTypeQueryMapper.findMappedValues(type)) {
                    SiteTypeKeyword stk = genericKeywordService.findByLabel(SiteTypeKeyword.class, stTerm);
                    // params.getUncontrolledSiteTypes().add(stTerm);
                    terms.add(stk.getId().toString());
                }
            }
            params.getApprovedSiteTypeIdLists().add(terms);
            // qb.append(new HydrateableKeywordQueryPart<>(ACTIVE_SITE_TYPE_KEYWORDS, originalClass, fieldValues_));
        }
        if (when != null) {
            params.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, when.getMinDate(), when.getMaxDate()));
        }
        if (where != null) {
            LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
            latLong.setMinimumLatitude(where.getMinLatitude().doubleValue());
            latLong.setMaximumLatitude(where.getMaxLatitude().doubleValue());
            latLong.setMinimumLongitude(where.getMinLongitude().doubleValue());
            latLong.setMaximumLongitude(where.getMaxLongitude().doubleValue());
            params.getLatitudeLongitudeBoxes().add(latLong);
        }
        if (StringUtils.isNotBlank(freetext) && !"*:*".equals(freetext)) {
            params.getAllFields().add(freetext);
        }
        authenticationAndAuthorizationService.initializeReservedSearchParameters(reserved, null);
        QueryPartGroup reservedPart = reserved.toQueryPartGroup(null);
        qb.append(reservedPart);
        qb.append(params, null);
        // initialize detail values for results
        return qb;
    }

    @Override
    public GetXsltTemplateResponse getXsltTemplate(GetXsltTemplate parameters) {
        GetXsltTemplateResponse resp = new GetXsltTemplateResponse();
        resp.setAny(XSLT);
        return resp;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setUrlService(UrlService urlService) {
        this.urlService = urlService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setSiteTypeQueryMapper(SiteTypeQueryMapper siteTypeQueryMapper) {
        this.siteTypeQueryMapper = siteTypeQueryMapper;
    }

    private String buildIntegratableDatasetUrl(List<Long> projIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < projIds.size(); i++) {
            sb.append("groups[").append(i).append("].projects.id=").append(projIds.get(i)).append("&");
        }
        return String.format("%s/search/search?query=%s&integratableOptions=%s&resourceTypes=DATASET&referrer=TAG",
                UrlService.getBaseUrl(), sb.toString(), IntegratableOptions.YES);
    }
}
