package org.tdar.tag;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
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
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.tag.Query.What;
import org.tdar.tag.Query.When;
import org.tdar.tag.Query.Where;
import org.tdar.tag.SearchResults.Meta;
import org.tdar.tag.SearchResults.Results;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
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
    private ResourceSearchService resourceSearchService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private ProjectService projectService;
    
    private SiteTypeQueryMapper siteTypeQueryMapper;

    static { // initialize the XSLT for inclusion in the SOAP response
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            URL url = TagGateway.class.getClassLoader().getResource("wsdl/tagPortal.xsl");
            File configFile = new File(url.toURI());

            Document d = db.parse(configFile);
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

            SearchResult<Resource> q = new SearchResult<>();
            List<Project> resources = Collections.emptyList();
            int totalRecords = 0;
            int firstRec = 0;
            int lastRec = 0;
            q.setRecordsPerPage(numberOfRecords);
            // actually perform the search against the index
            AdvancedSearchQueryObject asqo = buildSearchQuery(what, where, when, freetext);
            try {
                resourceSearchService.buildAdvancedSearch(asqo,null, q, MessageHelper.getInstance());
            } catch (ParseException e) {
                logger.warn("Could not parse supplied query.", e);
            }

            // determine if we need to make a second projection query to get all
            // of the record ids
            boolean totalExceedsRequested = false;

            if (q != null) {
                totalRecords = q.getTotalRecords();
                if (totalRecords > 0) {
                    firstRec = 1;
                }

                if (totalRecords > numberOfRecords) {
                    totalExceedsRequested = true;
                    lastRec = numberOfRecords;
                } else {
                    lastRec = totalRecords;
                }
                logger.debug("Number of records requested: " + numberOfRecords);
                logger.debug("Total number of records: " + totalRecords);
                resources = (List<Project>)(List<?>)q.getResults();
            }

            int recordsReturned = totalRecords == 0 ? 0 : (lastRec - firstRec) + 1;

            Results tdarRes = createResults(sessionId, searchRes, totalRecords, firstRec, lastRec, recordsReturned);

            // project ids used to grab the integratable datasets
            List<Long> projIds = processProjects(resources, totalExceedsRequested, tdarRes);
            getAllProjectIds(asqo, totalExceedsRequested, projIds);

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

    private void getAllProjectIds(AdvancedSearchQueryObject asqo, boolean totalExceedsRequested, List<Long> projIds) {
        if (totalExceedsRequested) { // query again to get all of the projectIds
            try {
                SearchResult<Resource> q = new SearchResult<>();
                resourceSearchService.buildAdvancedSearch(asqo, null, q, MessageHelper.getInstance());
                projIds.addAll(PersistableUtils.extractIds(q.getResults()));
            } catch (ParseException e) {
                logger.warn("Could not parse supplied query.", e);
            } catch (SolrServerException e) {
                logger.warn("Could not parse supplied query.", e);
            } catch (IOException e) {
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

    private AdvancedSearchQueryObject buildSearchQuery(What what, Where where, When when, String freetext) {
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
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
        asqo.setReservedParams(reserved);
        asqo.getSearchParameters().add(params);
        return asqo;
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
