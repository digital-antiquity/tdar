package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.facet.Facet;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.hibernate.search.query.facet.FacetingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ProjectService;
import org.tdar.core.service.UrlService;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.FreetextQueryPart;
import org.tdar.search.query.KeywordQueryPart;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.search.query.ResourceTypeQueryPart;
import org.tdar.search.query.SpatialLimit;
import org.tdar.search.query.SpatialQueryPart;
import org.tdar.search.query.TemporalLimit;
import org.tdar.search.query.TemporalQueryPart;
import org.tdar.utils.keyword.KeywordNode;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.opensearch.OpenSearchModule;
import com.sun.syndication.feed.module.opensearch.impl.OpenSearchModuleImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * $Id$
 * 
 * 
 * @author Matt Cordial, Adam Brin
 * @version $Rev$
 */
@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
public class LuceneSearchController extends AuthenticationAware.Base {

    public static final String TAG_KEYWORD_PHRASE = "referred query from the Transatlantic Archaeology Gateway";
    public static final String FILTERED_BY_KEYWORD = "Filtered by Keyword";
    public static final String ALL_RECORDS = "All Records";
    public static final String WITH_SITE_NAME_KEYWORDS = " with site name keywords ";
    public static final String WITH_SITE_TYPE_KEYWORDS = " with site type keywords ";
    public static final String WITH_MATERIAL_KEYWORDS = " with material keywords ";
    public static final String WITH_CULTURE_KEYWORDS = " with culture keywords ";
    public static final String WITH_INVESTIGATION_TYPES = " with investigation types ";
    public static final String BETWEEN = " between ";
    public static final String WITH_TEXT_IN_TITLE = " with text in title";
    public static final String USING_KEYWORD = " using keyword ";
    public static final String COMMA_SEPARATOR = ", ";
    public static final String AND = " and ";
    public static final String BC = " BC ";
    public static final String AD = " AD ";
    public static final String WITH_RADIOCARBON_DATE_BETWEEN = " with radiocarbon date between ";
    public static final String WITHIN_MAP_CONSTRAINTS = " within map constraints ";
    public static final String SEARCHING_FOR_RESOURCE_WITH_T_DAR_ID = "Searching for resource with tDAR ID:";
    public static final String SELECTED_RESOURCE_TYPES = "Selected resource types: ";
    public static final String SELECTED_DOCUMENT_TYPES = "Selected document type: ";
    public static final String STRONG_CLOSE = " </strong> ";
    public static final String STRONG = " <strong> ";
    public static final String SEARCHING_ALL_RESOURCE_TYPES = " Searching all resource types ";

    private static final long serialVersionUID = 3869953915909593269L;

    private String rssUrl;
    private String query;
    private List<ResourceType> resourceTypes;
    private int startRecord;
    private int recordsPerPage;
    private int totalRecords;
    private int id;
    private String referrer;
    boolean sortOrder = false;
    private static final String SORTFIELD_RELEVANCE = "relevance";
    private String sortField = SORTFIELD_RELEVANCE;
    private Double minx;
    private Double maxx;
    private Double miny;
    private Double maxy;
    private String documentType;
    private String yearType;
    private Integer fromYear;
    private Integer toYear;
    private List<Project> projects;
    private String title;
    private int defaultRecordsPerPage = 20;
    private Boolean showIndependentProjects = false;
    private List<Long> projectIds;
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UrlService urlService;
    // for keyword search
    // TODO: push these up to AuthenticationAware.Base?
    private List<MaterialKeyword> allMaterialKeywords;
    private List<Long> materialKeywordIds;

    private List<String> uncontrolledCultureKeywords;
    private List<Long> approvedCultureKeywordIds;
    private KeywordNode<CultureKeyword> approvedCultureKeywords;

    private List<Long> investigationTypeIds;
    private List<InvestigationType> allInvestigationTypes;

    private List<InvestigationType> selectedInvestigationTypes = new ArrayList<InvestigationType>();
    private List<MaterialKeyword> selectedMaterialKeywords = new ArrayList<MaterialKeyword>();
    private List<CultureKeyword> selectedCultureKeywords = new ArrayList<CultureKeyword>();

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private List<Long> approvedSiteTypeKeywordIds;
    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> siteNameKeywords;

    private List<SiteTypeKeyword> selectedSiteTypeKeywords = new ArrayList<SiteTypeKeyword>();
    private Set<String> selectedSiteNameKeywords = new HashSet<String>();

    public int getDefaultRecordsPerPage() {
        return defaultRecordsPerPage;
    }

    public void setDefaultRecordsPerPage(int defaultRecordsPerPage) {
        this.defaultRecordsPerPage = defaultRecordsPerPage;
    }

    private List<Resource> resources;

    private SyndFeed feed;
    private InputStream rssInputStream;
    private String searchPhrase;
    private String searchSubtitle;

    private List<Long> searchSubmitterIds;
    private List<Long> searchContributorIds;
    private Date updatedAfter;
    private Date updatedBefore;
    private List<Status> includedStatuses;
    private List<Facet> resourceTypeFacets = new ArrayList<Facet>();
    private List<Facet> documentTypeFacets = new ArrayList<Facet>();
    private List<Facet> cultureFacets = new ArrayList<Facet>();
    private List<Facet> locationFacets = new ArrayList<Facet>();
    private List<Facet> dateCreatedFacets = new ArrayList<Facet>();

    
    //todo: put this in service... maybe
    private static String escape(String str) {
    	if(str == null) return null;
        // trim and escape input
        String escaped = str.trim();
        escaped = QueryParser.escape(escaped);
        return escaped;
    }
    
    public LuceneSearchController() {
        // default to only show active resources
        includedStatuses = new ArrayList<Status>();
        includedStatuses.add(Status.ACTIVE);
    }
    
    public void setupSearch() {
        projects = projectService.findAllSparse();
    }

    @Actions({ @Action(value = "basic"), @Action(value = "advanced") })
    public String execute() {
        setupSearch();
        return SUCCESS;
    }

    @Action(value = "rss", results = {
            @Result(name = "success", type = "stream", params = { "documentName", "rssFeed", "formatOutput", "true", "inputName", "rssInputStream",
                    "contentType", "application/rss+xml", "contentEncoding", "UTF-8" }),
            @Result(name = "notfound", type = "httpheader", params = { "status", "404" }) })
    public String viewRss() {
        sortField = "dateRegistered";
        sortOrder = true;
        performSearch();
        feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");
        String subtitle = getSearchSubtitle();
        subtitle = StringUtils.replace(subtitle, STRONG, "");
        subtitle = StringUtils.replace(subtitle, STRONG_CLOSE, "");
        feed.setTitle("tDAR Search Results: " + subtitle);
        OpenSearchModule osm = new OpenSearchModuleImpl();
        osm.setItemsPerPage(getRecordsPerPage());
        osm.setStartIndex(getStartRecord());
        osm.setTotalResults(getTotalRecords());

        Link link = new Link();
        link.setHref(getUrlService().getBaseUrl() + "/includes/opensearch.xml");
        link.setType("application/opensearchdescription+xml");
        osm.setLink(link);
        @SuppressWarnings("unchecked")
        List<Module> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);
        feed.setLink(getRssUrl());
        feed.setDescription(getSearchPhrase());
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (Resource res : resources) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(res.getTitle());
            SyndContent description = new SyndContentImpl();

            if (StringUtils.isEmpty(res.getDescription())) {
                description.setValue("no description");
            } else {
                description.setValue(res.getDescription());
            }
            List<SyndPerson> authors = new ArrayList<SyndPerson>();
            for (ResourceCreator creator : res.getPrimaryCreators()) {
                SyndPerson person = new SyndPersonImpl();
                person.setName(creator.getCreator().getProperName());
                authors.add(person);
            }
            if (authors.size() > 0) {
                entry.setAuthors(authors);
            }
            entry.setDescription(description);
            entry.setLink(urlService.absoluteUrl(res));
            entry.setPublishedDate(res.getDateRegistered());
            entries.add(entry);
        }
        feed.setEntries(entries);
        feed.setPublishedDate(new Date());
        getRssFeed();
        return SUCCESS;
    }

    @Action(value = "results", results = { @Result(name = "success", location = "results.ftl") })
    public String performSearch() {
        try {
            return performSearch(true);
        } catch(ParseException px) {
            try {
                return performSearch(false);
            } catch(ParseException ipx) {
                addActionError("Invalid query syntax, please try using simpler terms without special characters.");
                return INPUT;
            }
            
        }
    }
    
    //package-private 
    String performSearch(boolean strictParsing) throws ParseException {
        if (recordsPerPage == 0) {
            recordsPerPage = defaultRecordsPerPage;
        }
        
        if(!strictParsing) {
            query = escape(query);
            title = escape(title);
        }

        QueryBuilder q = new ResourceQueryBuilder();

        if (id > 0) {
            FieldQueryPart fqp = new FieldQueryPart("id", Integer.toString(id));
            q.append(fqp);
        }

        if (StringUtils.isNotBlank(query)) {
            FreetextQueryPart ft = new FreetextQueryPart();
            ft.setQueryString(query);
            q.append(ft);
        }
        if (fromYear != null && toYear != null && getYearType() != null) {
            TemporalQueryPart tq = new TemporalQueryPart();
            tq.addTemporalLimit(new TemporalLimit(CoverageType.valueOf(getYearType()),
                    fromYear, toYear));
            q.append(tq);
        }
        if (StringUtils.isNotBlank(title)) {
            FieldQueryPart fqp = new FieldQueryPart("title", title);
            q.append(fqp);
        }

        QueryPartGroup projectQueryGroup = new QueryPartGroup();
        projectQueryGroup.setOperator(Operator.OR);
        if (!CollectionUtils.isEmpty(projectIds)) {
            boolean valid = false;
            for (Long projectId : projectIds) {
                if (projectId != null && projectId > -1) {
                    projectQueryGroup.addPart(new FieldQueryPart("projectId", projectId.toString()));
                    valid = true;
                }
            }
            if (valid) {
                q.append(projectQueryGroup);
            }
        }

        if (minx != null && maxx != null && miny != null && maxy != null) {
            SpatialQueryPart sq = new SpatialQueryPart();
            sq.addSpatialLimit(new SpatialLimit(LatitudeLongitudeBox.obfuscate(
                    minx, maxx, LatitudeLongitudeBox.LATITUDE),
                    LatitudeLongitudeBox.obfuscate(maxx, minx,
                            LatitudeLongitudeBox.LATITUDE),
                    LatitudeLongitudeBox.obfuscate(miny, maxy,
                            LatitudeLongitudeBox.LONGITUDE),
                    LatitudeLongitudeBox.obfuscate(maxy, miny,
                            LatitudeLongitudeBox.LONGITUDE)));
            q.append(sq);
        }
        if (!CollectionUtils.isEmpty(resourceTypes)) {
            ResourceTypeQueryPart rtq = new ResourceTypeQueryPart();
            for (ResourceType type : resourceTypes) {
                rtq.addResourceTypeLimit(type);
            }
            q.append(rtq);
        }

        if (!StringUtils.isEmpty(documentType)) {
            DocumentType docType = DocumentType.fromString(documentType);
            if (docType != null) {
                q.append(new FieldQueryPart("documentType", docType.name()));
            }
        }
        appendMaterialKeywordQuery(q);
        appendCultureKeywordQuery(q);
        appendInvestigationTypeQuery(q);
        appendSiteTypeKeywordQuery(q);
        appendSiteNameKeywordQuery(q);
        appendAdminQuery(q);
        appendStatusTypes(q);
        setSearchPhrase();
        setRssUrl();
        setSearchSubtitle();
        return callSearchService(q);        
    }
    

    // @Action(value = "basic", results = { @Result(name = "success", location = "basic.ftl") })
    // public String basic() {
    // setupSearch();
    // return SUCCESS;
    // }

    // @Action(value = "advanced", results = { @Result(name = "success", location = "advanced.ftl") })
    // public String advanced() {
    // setupSearch();
    // return SUCCESS;
    // }

    public String getRssFeed() {
        if (feed != null) {
            StringWriter writer = new StringWriter();
            SyndFeedOutput output = new SyndFeedOutput();
            try {
                output.output(feed, writer);
                setRssInputStream(new ByteArrayInputStream(writer.toString().getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FeedException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    public String getSearchPhrase() {
        return searchPhrase;
    }

    /*
     * the search phrase is used by multiple items including the results page and the rss feed
     */

    // FIXME: COMBINE WITH THE QUERY BUILDING BELOW, this SHOULD NOT BE IT'S OWN METHOD!!
    public void setSearchPhrase() {
        StringBuilder result = new StringBuilder();
        if (getId() > 0) {
            result.append(SEARCHING_FOR_RESOURCE_WITH_T_DAR_ID).append(STRONG).append(getId()).append(STRONG_CLOSE);
        }
        if (!CollectionUtils.isEmpty(getResourceTypes())) {
            result.append(SELECTED_RESOURCE_TYPES);
            Iterator<ResourceType> iter = resourceTypes.listIterator();
            while (iter.hasNext()) {
                ResourceType type = iter.next();
                result.append(STRONG).append(type.getLabel()).append(STRONG_CLOSE);
                if (iter.hasNext())
                    result.append(COMMA_SEPARATOR);
            }
        } else {
            result.append(SEARCHING_ALL_RESOURCE_TYPES);
        }

        if (!StringUtils.isEmpty(documentType)) {
            try {
                DocumentType docType = DocumentType.fromString(documentType);
                if (docType != null) {
                    result.append(SELECTED_DOCUMENT_TYPES);
                    result.append(STRONG);
                    result.append(docType.getLabel());
                    result.append(STRONG_CLOSE);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        if (!CollectionUtils.isEmpty(projectIds)) {
            if (projectIds.size() == 1) {
                if (projectIds.get(0) != null) {
                    result.append("limited to project id: ").append(STRONG).append(projectIds.get(0)).append(STRONG_CLOSE);
                }
            } else {
                result.append("limited to ").append(STRONG).append("selected project ids").append(STRONG_CLOSE);
            }
        }

        if (!StringUtils.isEmpty(getQuery())) {
            if (getQuery().contains("integratable:true")) {
                result.append("using ").append(STRONG).append(TAG_KEYWORD_PHRASE).append(STRONG_CLOSE);

            } else {
                result.append(USING_KEYWORD).append(STRONG).append(query.toString()).append(STRONG_CLOSE);
            }
        }

        if (!StringUtils.isEmpty(getTitle())) {
            result.append(WITH_TEXT_IN_TITLE).append(STRONG).append(title.toString()).append(STRONG_CLOSE);
        }

        if (!StringUtils.isEmpty(getYearType())) {
            switch (CoverageType.valueOf(getYearType())) {
                case CALENDAR_DATE:
                    result.append(BETWEEN).append(STRONG);
                    if (getFromYear() > 0) {
                        result.append(getFromYear()).append(AD);
                    } else {
                        result.append(Math.abs(getFromYear())).append(BC);
                    }

                    result.append(STRONG_CLOSE).append(AND).append(STRONG);
                    if (getToYear() > 0) {
                        result.append(getToYear()).append(AD);
                    } else {
                        result.append(Math.abs(getFromYear())).append(BC);
                    }
                    result.append(STRONG_CLOSE);
                    break;
                case RADIOCARBON_DATE:
                    result.append(WITH_RADIOCARBON_DATE_BETWEEN).append(STRONG).append(getFromYear()).append(STRONG_CLOSE).append(AND).
                            append(STRONG_CLOSE).append(getToYear()).append(STRONG_CLOSE);
                    break;
            }
        }
        if (getMinx() != null) {
            result.append(WITHIN_MAP_CONSTRAINTS);
        }

        appendKeywordsFromList(result, selectedCultureKeywords, WITH_CULTURE_KEYWORDS);
        appendKeywordsFromList(result, selectedInvestigationTypes, WITH_INVESTIGATION_TYPES);
        appendKeywordsFromList(result, selectedMaterialKeywords, WITH_MATERIAL_KEYWORDS);
        appendKeywordsFromList(result, selectedSiteTypeKeywords, WITH_SITE_TYPE_KEYWORDS);

        if (!CollectionUtils.isEmpty(selectedSiteNameKeywords)) {
            result.append(WITH_SITE_NAME_KEYWORDS);
            Iterator<String> iter = selectedSiteNameKeywords.iterator();
            while (iter.hasNext()) {
                result.append(STRONG).append(iter.next()).append(STRONG_CLOSE);
                if (iter.hasNext())
                    result.append(COMMA_SEPARATOR);
            }
        }
        this.searchPhrase = result.toString();
    }

    private <K extends Keyword> void appendKeywordsFromList(StringBuilder result, List<K> list, String extra) {
        if (!CollectionUtils.isEmpty(list)) {
            result.append(" ");
            result.append(extra);
            result.append(" ");
            Iterator<K> iter = list.listIterator();
            while (iter.hasNext()) {
                Keyword type = iter.next();
                result.append(STRONG).append(type.getLabel()).append(STRONG_CLOSE);
                if (iter.hasNext())
                    result.append(COMMA_SEPARATOR);
            }
        }

    }

    public String getSearchSubtitle() {
        return searchSubtitle;
    }

    public void setSearchSubtitle() {
        String title = ALL_RECORDS;

        if (!CollectionUtils.isEmpty(selectedCultureKeywords) || !CollectionUtils.isEmpty(selectedInvestigationTypes)
                || !CollectionUtils.isEmpty(selectedMaterialKeywords) || !CollectionUtils.isEmpty(selectedSiteTypeKeywords)
                || !CollectionUtils.isEmpty(selectedSiteNameKeywords)) {
            title = FILTERED_BY_KEYWORD;
        }

        if (!StringUtils.isEmpty(query)) {
            title = query;
            if (getQuery().contains("integratable:true")) {
                title = TAG_KEYWORD_PHRASE;
            }
        }
        this.searchSubtitle = title;
    }

    private void appendAdminQuery(QueryBuilder q) {
        // if(!isAdministrator()) return;

        List<Person> submitters = getPeopleFromRawList(searchSubmitterIds);
        List<Person> contributors = getPeopleFromRawList(searchContributorIds);

        if (!submitters.isEmpty()) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            for (Person submitter : submitters) {
                FieldQueryPart qp = new FieldQueryPart("submitter.id", submitter.getId().toString());
                group.addPart(qp);
            }
            q.append(group);
        }

        if (!contributors.isEmpty()) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            for (Person contributor : contributors) {
                FieldQueryPart qp = new FieldQueryPart("resourceCreators.creator.id", contributor.getId().toString());
                group.addPart(qp);
            }
            q.append(group);
        }

        appendUpdateDateRangeQuery(q);

    }

    private void appendUpdateDateRangeQuery(QueryBuilder q) {

    }

    private void appendStatusTypes(QueryBuilder q) {
        if (CollectionUtils.isEmpty(includedStatuses))
            return;
        QueryPartGroup group = new QueryPartGroup();
        group.setOperator(Operator.OR);
        for (Status status : includedStatuses) {
            FieldQueryPart qp = new FieldQueryPart("status", status.name().toLowerCase());
            group.addPart(qp);
        }
        q.append(group);
    }

    private List<Person> getPeopleFromRawList(List<Long> rawIds) {
        if (rawIds == null)
            return Collections.<Person> emptyList();
        List<Person> people = new ArrayList<Person>();
        List<Long> validIds = new ArrayList<Long>(filterInvalidUsersIds(rawIds));
        for (Long id : validIds) {
            Person person = getEntityService().findPerson(id);
            if (person != null) {
                people.add(person);
            }
        }
        return people;
    }

    private void appendMaterialKeywordQuery(QueryBuilder q) {
        Set<MaterialKeyword> keywords = getMaterialKeywordService().findByIds(
                materialKeywordIds);
        selectedMaterialKeywords.addAll(keywords);
        appendKeywords(selectedMaterialKeywords, "activeMaterialKeywords", q);
    }

    private void appendCultureKeywordQuery(QueryBuilder q) {
        Set<CultureKeyword> cultureKeywords = getCultureKeywordService()
                .findByIds(approvedCultureKeywordIds);
        selectedCultureKeywords.addAll(cultureKeywords);
        appendKeywords(cultureKeywords, "activeCultureKeywords", q);
        if (getUncontrolledCultureKeywords() == null)
            return;

        appendKeywords(getUncontrolledCultureKeywords(), "activeCultureKeywords", q);

    }

    @SuppressWarnings("rawtypes")
    private void appendKeywords(Collection keywords, String type, QueryBuilder q) {
        if (!keywords.isEmpty())
            q.append(new KeywordQueryPart(type, keywords));

    }

    private void appendSiteTypeKeywordQuery(QueryBuilder q) {
        Set<SiteTypeKeyword> siteTypeKeywords = getSiteTypeKeywordService()
                .findByIds(approvedSiteTypeKeywordIds);
        selectedSiteTypeKeywords.addAll(siteTypeKeywords);
        appendKeywords(siteTypeKeywords, "activeSiteTypeKeywords", q);
        if (uncontrolledSiteTypeKeywords == null)
            return;
        appendKeywords(uncontrolledSiteTypeKeywords, "activeSiteTypeKeywords", q);
    }

    private void appendSiteNameKeywordQuery(QueryBuilder q) {
        if (siteNameKeywords == null)
            return;
        appendKeywords(siteNameKeywords, "activeSiteNameKeywords", q);
    }

    private void appendInvestigationTypeQuery(QueryBuilder q) {
        Set<InvestigationType> investigationTypes = getInvestigationTypeService().findByIds(investigationTypeIds);
        selectedInvestigationTypes.addAll(investigationTypes);
        appendKeywords(investigationTypes, "activeInvestigationTypes", q);
    }

    @SuppressWarnings("unchecked")
    private String callSearchService(QueryBuilder q) throws ParseException{
        logger.debug("{}", q);
            long num = System.currentTimeMillis();
            FullTextQuery ftq;
            if (sortField.equalsIgnoreCase(SORTFIELD_RELEVANCE)) {
                ftq = getSearchService().search(q);

            } else {
                ftq = getSearchService().search(q, getSortField(), sortOrder);
            }
            totalRecords = ftq.getResultSize();
            ftq.setFirstResult(startRecord);
            ftq.setMaxResults(recordsPerPage);
            long lucene = System.currentTimeMillis() - num;
            num = System.currentTimeMillis();
            resources = ftq.list();
            logger.debug("LUCENE: {} HYDRATION: {}", lucene, (System.currentTimeMillis() - num));
            addFacets(ftq);
        return SUCCESS;
    }

    private FacetingRequest facetOn(String name, String field, FullTextQuery ftq, List<Facet> facetList) {
        FacetingRequest facetRequest = getSearchService().getQueryBuilder(Resource.class).facet().name(name).onField(field).discrete()
                .orderedBy(FacetSortOrder.COUNT_DESC).includeZeroCounts(false).createFacetingRequest();

        if (name.equals("dateCreated")) {
            facetRequest = getSearchService().getQueryBuilder(Resource.class).facet().name(name).onField(field).range()
                    .below(1800).
                    from(1800).to(1850).
                    from(1850).to(1900).
                    from(1900).to(1950).
                    from(1950).to(1970).
                    from(1970).to(1980).
                    from(1980).to(1990).
                    from(1990).to(2000).
                    from(2000).to(2010).
                    above(2010).excludeLimit()
                    .orderedBy(FacetSortOrder.COUNT_DESC).includeZeroCounts(true).createFacetingRequest();
        }
        ftq.getFacetManager().enableFaceting(facetRequest);
        facetList.addAll(ftq.getFacetManager().getFacets(name));
        return facetRequest;
    }

    // public
    private void addFacets(FullTextQuery ftq) {
        facetOn("resourceType", "resourceType", ftq, getResourceTypeFacets());
        facetOn("documentType", "documentType", ftq, getDocumentTypeFacets());
        facetOn("cultures", "activeCultureKeywords.label", ftq, getCultureFacets());
        facetOn("location", "activeGeographicKeywords.label", ftq, getLocationFacets());
//        facetOn("dateCreated", "dateCreated", ftq, getDateCreatedFacets());
    }

//    private boolean hasAdminSearchPriviliges() {
//        if (getAuthenticatedUser() == null)
//            return false;
//        return getCrowdService().isAdministrator(getAuthenticatedUser());
//    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public String getQuery() {
        return query;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public String getSortField() {
        sanitizeSortField();

        return sortField;
    }

    private void sanitizeSortField() {
        if (sortField == null || sortField.length() == 0)
            return;

        if (sortField.startsWith("+")) {
            sortField = sortField.substring(1);
        } else if (sortField.startsWith("-")) {
            sortOrder = true;
            sortField = sortField.substring(1);
        }
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
        sanitizeSortField();
    }

    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public Double getMinx() {
        return minx;
    }

    public void setMinx(Double minx) {
        this.minx = minx;
    }

    public Double getMaxx() {
        return maxx;
    }

    public void setMaxx(Double maxx) {
        this.maxx = maxx;
    }

    public Double getMiny() {
        return miny;
    }

    public void setMiny(Double miny) {
        this.miny = miny;
    }

    public Double getMaxy() {
        return maxy;
    }

    public void setMaxy(Double maxy) {
        this.maxy = maxy;
    }

    public String getYearType() {
        if (StringUtils.isEmpty(yearType) || yearType == "NONE")
            return "NONE";
        try {
            CoverageType.valueOf(yearType);
        } catch (IllegalArgumentException ie) {
            yearType = "NONE";
        }
        return yearType;
    }

    public void setYearType(String yearType) {
        this.yearType = yearType;
    }

    public Integer getFromYear() {
        return fromYear;
    }

    public void setFromYear(Integer fromYear) {
        this.fromYear = fromYear;
    }

    public Integer getToYear() {
        return toYear;
    }

    public void setToYear(Integer toYear) {
        this.toYear = toYear;
    }

    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    // public Boolean getSearchProjects() {
    // if (searchProjects == null)
    // return false;
    // return searchProjects;
    // }
    //
    // public void setSearchProjects(Boolean searchProjects) {
    // this.searchProjects = searchProjects;
    // }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getShowIndependentProjects() {
        return showIndependentProjects;
    }

    public void setShowIndependentProjects(Boolean showIndependentProjects) {
        logger.debug("show independent projects: {}", showIndependentProjects);
        this.showIndependentProjects = showIndependentProjects;
    }

    public boolean getSortOrder() {
        return sortOrder;
    }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getMaterialKeywordService().findAll();
        }
        return allMaterialKeywords;
    }

    public List<Long> getMaterialKeywordIds() {
        if (CollectionUtils.isEmpty(materialKeywordIds)) {
            materialKeywordIds = Collections.<Long> emptyList();
        }
        return materialKeywordIds;
    }

    public void setMaterialKeywordIds(List<Long> materialKeywordIds) {
        this.materialKeywordIds = materialKeywordIds;
    }

    public KeywordNode<CultureKeyword> getApprovedCultureKeywords() {
        if (approvedCultureKeywords == null) {
            approvedCultureKeywords = KeywordNode
                    .organizeKeywords(getCultureKeywordService()
                            .findAllApproved());
        }
        return approvedCultureKeywords;
    }

    public List<Long> getApprovedCultureKeywordIds() {
        if (CollectionUtils.isEmpty(approvedCultureKeywordIds)) {
            approvedCultureKeywordIds = createListWithSingleNull();
        }
        return approvedCultureKeywordIds;
    }

    public List<String> getUncontrolledCultureKeywords() {
        if (CollectionUtils.isEmpty(uncontrolledCultureKeywords)) {
            uncontrolledCultureKeywords = createListWithSingleNull();
        }
        return uncontrolledCultureKeywords;
    }

    public List<Long> getInvestigationTypeIds() {
        if (CollectionUtils.isEmpty(investigationTypeIds)) {
            investigationTypeIds = createListWithSingleNull();
        }
        return investigationTypeIds;
    }

    public void setInvestigationTypeIds(List<Long> investigationTypeIds) {
        this.investigationTypeIds = investigationTypeIds;
    }

    public List<InvestigationType> getAllInvestigationTypes() {
        if (CollectionUtils.isEmpty(allInvestigationTypes)) {
            allInvestigationTypes = getInvestigationTypeService().findAll();
        }
        return allInvestigationTypes;
    }

    public void setApprovedCultureKeywordIds(
            List<Long> approvedCultureKeywordIds) {
        this.approvedCultureKeywordIds = approvedCultureKeywordIds;
    }

    public List<InvestigationType> getSelectedInvestigationTypes() {
        return selectedInvestigationTypes;
    }

    public List<MaterialKeyword> getSelectedMaterialKeywords() {
        return selectedMaterialKeywords;
    }

    public List<CultureKeyword> getSelectedCultureKeywords() {
        return selectedCultureKeywords;
    }

    public KeywordNode<SiteTypeKeyword> getApprovedSiteTypeKeywords() {
        if (approvedSiteTypeKeywords == null) {
            approvedSiteTypeKeywords = KeywordNode
                    .organizeKeywords(getSiteTypeKeywordService()
                            .findAllApproved());
        }
        return approvedSiteTypeKeywords;
    }

    public void setApprovedSiteTypeKeywords(
            KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords) {
        this.approvedSiteTypeKeywords = approvedSiteTypeKeywords;
    }

    public List<Long> getApprovedSiteTypeKeywordIds() {
        if (CollectionUtils.isEmpty(approvedSiteTypeKeywordIds)) {
            approvedSiteTypeKeywordIds = createListWithSingleNull();

        }
        return approvedSiteTypeKeywordIds;
    }

    public void setApprovedSiteTypeKeywordIds(
            List<Long> approvedSiteTypeKeywordIds) {
        this.approvedSiteTypeKeywordIds = approvedSiteTypeKeywordIds;
    }

    public List<String> getUncontrolledSiteTypeKeywords() {
        if (uncontrolledSiteTypeKeywords == null) {
            uncontrolledSiteTypeKeywords = createListWithSingleNull();
        }
        return uncontrolledSiteTypeKeywords;
    }

    public void setUncontrolledSiteTypeKeywords(
            List<String> uncontrolledSiteTypeKeywords) {
        this.uncontrolledSiteTypeKeywords = uncontrolledSiteTypeKeywords;
    }

    public List<String> getSiteNameKeywords() {
        if (siteNameKeywords == null) {
            siteNameKeywords = createListWithSingleNull();
        }
        return siteNameKeywords;
    }

    public void setSiteNameKeywords(List<String> siteNameKeywords) {
        this.siteNameKeywords = siteNameKeywords;
    }

    public List<SiteTypeKeyword> getSelectedSiteTypeKeywords() {
        return selectedSiteTypeKeywords;
    }

    public Set<String> getSelectedSiteNameKeywords() {
        return selectedSiteNameKeywords;
    }

    public void setRssInputStream(InputStream rssInputStream) {
        this.rssInputStream = rssInputStream;
    }

    public InputStream getRssInputStream() {
        return rssInputStream;
    }

    public void setRssUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        if (getServletRequest() != null)
            urlBuilder.append(urlService.getBaseUrl()).append(getServletRequest().getContextPath()).append("/search/rss").append("?")
                    .append(getServletRequest().getQueryString());
        this.rssUrl = urlBuilder.toString();
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public List<Long> getSearchSubmitterIds() {
        return searchSubmitterIds;
    }

    public void setSearchSubmitterIds(List<Long> searchSubmitterIds) {
        this.searchSubmitterIds = searchSubmitterIds;
    }

    public List<Long> getSearchContributorIds() {
        return searchContributorIds;
    }

    public void setSearchContributorIds(List<Long> searchContributorIds) {
        this.searchContributorIds = searchContributorIds;
    }

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(Date updatedSince) {
        this.updatedAfter = updatedSince;
    }

    public List<Status> getIncludedStatuses() {
        return includedStatuses;
    }

    public void setIncludedStatuses(List<Status> includedStatuses) {
        this.includedStatuses = includedStatuses;
    }

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    public void setUncontrolledCultureKeywords(List<String> uncontrolledCultureKeywords) {
        this.uncontrolledCultureKeywords = uncontrolledCultureKeywords;
    }

    public void setResourceTypeFacets(List<Facet> resourceTypeFacets) {
        this.resourceTypeFacets = resourceTypeFacets;
    }

    public List<Facet> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public void setCultureFacets(List<Facet> facets) {
        this.cultureFacets = facets;
    }

    public List<Facet> getCultureFacets() {
        return cultureFacets;
    }

    public void setLocationFacets(List<Facet> locationFacets) {
        this.locationFacets = locationFacets;
    }

    public List<Facet> getLocationFacets() {
        return locationFacets;
    }

    public void setDocumentTypeFacets(List<Facet> documentTypeFacets) {
        this.documentTypeFacets = documentTypeFacets;
    }

    public List<Facet> getDocumentTypeFacets() {
        return documentTypeFacets;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentType() {
        return documentType;
    }

    /**
     * @param dateCreatedFacets
     *            the dateCreatedFacets to set
     */
    public void setDateCreatedFacets(List<Facet> dateCreatedFacets) {
        this.dateCreatedFacets = dateCreatedFacets;
    }

    /**
     * @return the dateCreatedFacets
     */
    public List<Facet> getDateCreatedFacets() {
        return dateCreatedFacets;
    }

    /**
     * @param projects
     *            the projects to set
     */
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * @return the projects
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * @param projectIds
     *            the projectIds to set
     */
    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    /**
     * @return the projectIds
     */
    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getReferrer() {
        return referrer;
    }
    
    public List<Status> getAllStatuses() {
        return Arrays.asList(Status.values());
    }

}
