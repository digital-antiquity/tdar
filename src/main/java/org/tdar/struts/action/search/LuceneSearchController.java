package org.tdar.struts.action.search;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
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
import org.tdar.core.bean.Indexable;
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
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.KeywordQueryPart;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.search.query.ResourceTypeQueryPart;
import org.tdar.search.query.SortOption;
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
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;
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
public class LuceneSearchController extends AbstractLookupController {

    public static final String TITLE_TAG_KEYWORD_PHRASE = "referred query from the Transatlantic Archaeology Gateway";
    public static final String TITLE_FILTERED_BY_KEYWORD = "Filtered by Keyword";
    private static final String LIMITED_TO = "Limited to :";
    public static final String TITLE_ALL_RECORDS = "All Records";
    public static final String TITLE_BY_TDAR_ID = "Search by TDAR ID";
    public static final String WITH_SITE_NAME_KEYWORDS = " with site name keywords ";
    public static final String WITH_SITE_TYPE_KEYWORDS = " with site type keywords ";
    public static final String WITH_MATERIAL_KEYWORDS = " with material keywords ";
    public static final String WITH_CULTURE_KEYWORDS = " with culture keywords ";
    public static final String WITH_INVESTIGATION_TYPES = " with investigation types ";
    public static final String WITH_UNCONTROLLED_SITE_TYPE_KEYWORDS = " with free-form site type keywords ";
    public static final String WITH_UNCONTROLLED_CULTURE_KEYWORDS = " with free-form culture keywords ";
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
    public static final Pattern INVALID_XML_CHARS = Pattern.compile("[\u0001\u0009\\u000A\\u000D\uD800\uDFFF]");
    // \uDC00-\uDBFF -\uD7FF\uE000-\uFFFD
    private static final long serialVersionUID = 3869953915909593269L;
    private static final String WITH_GEOGRAPHIC_KEYWORDS = " with geographic keywords: ";
    private String rssUrl;
    private String query;
    private String referrer;
    private Double minx;
    private Double maxx;
    private Double miny;
    private Double maxy;
    private String documentType;
    private String yearType;
    private Integer fromYear;
    private Integer toYear;
    private List<Project> projects;
    private int defaultRecordsPerPage = 20;
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
    private List<String> geographicKeywords;
    private List<String> siteNameKeywords;
    private ResourceAccessType fileAccess;
    private List<SiteTypeKeyword> selectedSiteTypeKeywords = new ArrayList<SiteTypeKeyword>();

    public int getDefaultRecordsPerPage() {
        return defaultRecordsPerPage;
    }

    public void setDefaultRecordsPerPage(int defaultRecordsPerPage) {
        this.defaultRecordsPerPage = defaultRecordsPerPage;
    }

    private SyndFeed feed;
    private InputStream rssInputStream;
    private String searchPhrase;
    private String searchSubtitle;

    private List<Long> searchSubmitterIds;
    private List<Long> searchContributorIds;
    private Person searchSubmitter;
    private Person searchContributor;

    private Date updatedAfter;
    private Date updatedBefore;
    private List<Facet> resourceTypeFacets = new ArrayList<Facet>();
    private List<Facet> documentTypeFacets = new ArrayList<Facet>();
    private List<Facet> fileAccessFacets = new ArrayList<Facet>();
    private List<Facet> cultureFacets = new ArrayList<Facet>();
    private List<Facet> locationFacets = new ArrayList<Facet>();
    private List<Facet> dateCreatedFacets = new ArrayList<Facet>();
    private String dateCreated = "";

    public LuceneSearchController() {
        // default to only show active resources
        setIncludedStatuses(new ArrayList<Status>());
        getIncludedStatuses().add(Status.ACTIVE);
    }

    public void setupSearch() {
        projects = projectService.findAllSparse();
    }

    @Actions({ @Action(value = "basic"), @Action(value = "advanced") })
    public String execute() {
        logger.trace("execute() called. fromYear:{}  toYear{}", fromYear, toYear);

        setupSearch();
        return SUCCESS;
    }

    // same as advanced, but since we are getting redirected here we should log that
    @Action(value = "advanced-error", results = { @Result(name = SUCCESS, location = "advanced.ftl") })
    public String executeWithError() {
        // TODO:this error message sucks... try to replace w/ more useful info (but we are coming from a redirect so we don't have much to go on)
        logger.error("Error while processing advanced search.  Check logs for more info.");
        addActionError("There was a problem with the search terms you entered.  Please try again, or notify an administrator if the problem persists.");
        setupSearch();
        return SUCCESS;
    }

    public static String cleanStringForXML(String input) {
        return INVALID_XML_CHARS.matcher(input).replaceAll("");
    }

    @SuppressWarnings("unchecked")
    @Action(value = "rss", results = {
            @Result(name = "success", type = "stream", params = { "documentName", "rssFeed", "formatOutput", "true", "inputName", "rssInputStream",
                    "contentType", "application/rss+xml", "contentEncoding", "UTF-8" }),
            @Result(name = "notfound", type = "httpheader", params = { "status", "404" }) })
    public String viewRss() {
        try {
            setSortField(SortOption.ID_REVERSE);
            setSecondarySortField(SortOption.TITLE);
            setMode("rss");
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
            List<Module> modules = feed.getModules();
            modules.add(osm);
            feed.setModules(modules);
            feed.setLink(getRssUrl());
            feed.setDescription(getSearchPhrase());
            List<SyndEntry> entries = new ArrayList<SyndEntry>();
            for (Indexable resource_ : getResults()) {
                Resource resource = (Resource) resource_;
                SyndEntry entry = new SyndEntryImpl();
                entry.setTitle(cleanStringForXML(resource.getTitle()));
                SyndContent description = new SyndContentImpl();

                if (StringUtils.isEmpty(resource.getDescription())) {
                    description.setValue("no description");
                } else {
                    description.setValue(cleanStringForXML(resource.getDescription()));
                }
                List<SyndPerson> authors = new ArrayList<SyndPerson>();
                for (ResourceCreator creator : resource.getPrimaryCreators()) {
                    SyndPerson person = new SyndPersonImpl();
                    person.setName(cleanStringForXML(creator.getCreator().getProperName()));
                    authors.add(person);
                }
                if (authors.size() > 0) {
                    entry.setAuthors(authors);
                }
                if (resource instanceof InformationResource && ((InformationResource) resource).getLatestUploadedVersions().size() > 0) {
                    for (InformationResourceFileVersion version : ((InformationResource) resource).getLatestUploadedVersions()) {
                        logger.trace("enclosure:" + version);
                        addEnclosure(entry, version);
                        addEnclosure(entry, version.getInformationResourceFile().getLatestThumbnail());
                    }
                }

                entry.setDescription(description);
                entry.setLink(urlService.absoluteUrl(resource));
                entry.setPublishedDate(resource.getDateRegistered());
                entries.add(entry);
            }
            feed.setEntries(entries);
            feed.setPublishedDate(new Date());
            getRssFeed();
        } catch (Exception e) {
            logger.error("rss error", e);
            addActionErrorWithException("could not process your search", e);
        }
        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private void addEnclosure(SyndEntry entry, InformationResourceFileVersion version) {
        if (version == null)
            return;
        if (getSessionData().getPerson() != null && getEntityService().canDownload(version, getSessionData().getPerson())) {
            logger.info("allowed:" + version);
            SyndEnclosure enclosure = new SyndEnclosureImpl();
            enclosure.setLength(version.getSize());
            enclosure.setType(version.getMimeType());
            enclosure.setUrl(getUrlService().downloadUrl(version));
            entry.getEnclosures().add(enclosure);
        }
    }

    // If there's an error due to invalid input, it doesn't appear to be enough to simply forward back to the search page, because we
    // still get ftl errors because the freemarker chokes on the same invalid input from original request. So we redirect instead.
    @Action(value = "results", results = { @Result(name = "success", location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    // , type = "redirect"
    public String performSearch() {
        String actionResult = INPUT;
        if (getMode() == null) {
            setMode("SEARCH");
        }
        try {
            try {
                actionResult = performSearch(true);
            } catch (ParseException px) {
                logger.debug("parse exception: {}, trying with escaping", px);
                try {
                    actionResult = performSearch(false);
                } catch (ParseException ipx) {
                    logger.debug("parse exception: {}", ipx);
                    addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", ipx);
                    actionResult = INPUT;
                }
            }
            setSearchPhrase();
            setRssUrl();
            setSearchSubtitle();
        } catch (Exception e) {
            logger.error("search error: {}", e);
            addActionErrorWithException("could not process your search", e);
        }
        return actionResult;
    }

    private FieldQueryPart setupUsingStrict(boolean strictParsing, String name, String value) {
        FieldQueryPart part = new FieldQueryPart(name);
        if (!strictParsing) {
            part.setEscapedValue(query);
        } else {
            part.setFieldValue(query);
        }
        return part;
    }

    // package-private
    String performSearch(boolean strictParsing) throws ParseException {
        QueryBuilder q = new ResourceQueryBuilder();
        if (getId() != null && getId() > 0) { // ignore all other options if we are searching for specific tdar-id
            q.append(new FieldQueryPart(QueryFieldNames.ID, Long.toString(getId())));
            handleSearch(q);
            return SUCCESS;
        }

        if (getRecordsPerPage() == 0) {
            setRecordsPerPage(defaultRecordsPerPage);
        }

        if (StringUtils.isNotBlank(query)) {
            String query_ = query.trim();
            if (query_.startsWith("\"") && query_.endsWith("\"")) {
                query_ = query_.substring(1, query_.length() - 1);
            }
            // still quotes... undo
            if (query_.contains("\"")) {
                query_ = query;
            }
            QueryPartGroup primary = new QueryPartGroup();

            FieldQueryPart titlePart = setupUsingStrict(strictParsing, QueryFieldNames.TITLE, query_);
            FieldQueryPart descriptionPart = setupUsingStrict(strictParsing, QueryFieldNames.DESCRIPTION, query);
            FieldQueryPart creatorPart = setupUsingStrict(strictParsing, QueryFieldNames.RESOURCE_CREATORS_PROPER_NAME, query);
            if (query_.contains(" ")) {
                titlePart.setQuotedEscapeValue(query_);
                descriptionPart.setQuotedEscapeValue(query_);
                creatorPart.setQuotedEscapeValue(query_);
                FieldQueryPart phrase = new FieldQueryPart(QueryFieldNames.ALL_PHRASE);
                phrase.setQuotedEscapeValue(query_);
                primary.append(phrase.setBoost(3.2f));
                creatorPart.setProximity(2);
                titlePart.setProximity(3);
                descriptionPart.setProximity(4);
                phrase.setProximity(4);
            }
            primary.append(titlePart.setBoost(6f));
            primary.append(descriptionPart.setBoost(4f));
            primary.append(creatorPart.setBoost(5f));

            primary.append(setupUsingStrict(strictParsing, QueryFieldNames.CONTENT, query_));
            primary.append(setupUsingStrict(strictParsing, QueryFieldNames.ALL, query_).setBoost(2f));
            q.append(primary);
            primary.setOperator(Operator.OR);
        }

        if (fromYear != null && toYear != null && getYearType() != null) {
            TemporalQueryPart tq = new TemporalQueryPart();
            tq.addTemporalLimit(new TemporalLimit(CoverageType.valueOf(getYearType()),
                    fromYear, toYear));
            q.append(tq);
        }

        if (fileAccess != null) {
            q.append(new FieldQueryPart(QueryFieldNames.RESOURCE_ACCESS_TYPE, fileAccess.name()));
        }

        if (StringUtils.isNotBlank(getTitle())) {
            FieldQueryPart fqp = new FieldQueryPart(QueryFieldNames.TITLE, getTitle());
            if (!strictParsing) {
                fqp.setEscapedValue(getTitle());
            }
            FieldQueryPart titlePart = new FieldQueryPart(QueryFieldNames.TITLE);
            titlePart.setQuotedEscapeValue(getTitle());
            q.append(titlePart.setBoost(6f));
            q.append(fqp);
        }

        QueryPartGroup projectQueryGroup = new QueryPartGroup();
        projectQueryGroup.setOperator(Operator.OR);
        if (!CollectionUtils.isEmpty(projectIds)) {
            boolean valid = false;
            for (Long projectId : projectIds) {
                if (projectId != null && projectId > -1) {
                    projectQueryGroup.append(new FieldQueryPart(QueryFieldNames.PROJECT_ID, projectId.toString()));
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
        if (!CollectionUtils.isEmpty(getResourceTypes())) {
            ResourceTypeQueryPart rtq = new ResourceTypeQueryPart();
            for (ResourceType type : getResourceTypes()) {
                rtq.addResourceTypeLimit(type);
            }
            q.append(rtq);
        }

        if (!StringUtils.isEmpty(documentType)) {
            DocumentType docType = DocumentType.fromString(documentType);
            if (docType != null) {
                q.append(new FieldQueryPart(QueryFieldNames.DOCUMENT_TYPE, docType.name()));
            }
        }

        appendMaterialKeywordQuery(q);
        appendCultureKeywordQuery(q);
        appendGeographicKeywordQuery(q);
        appendInvestigationTypeQuery(q);
        appendSiteTypeKeywordQuery(q);
        appendSiteNameKeywordQuery(q);
        appendAdminQuery(q);

        if (getAuthenticatedUser() != null && isUseSubmitterContext()) {
            q.append(new FieldQueryPart(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId().toString()));
        }

        if (CollectionUtils.isEmpty(getIncludedStatuses())) {
            getIncludedStatuses().add(Status.ACTIVE);
            if (getAuthenticatedUser() != null && isUseSubmitterContext()) {
                // SHOULD BE IN USER CONTEXT, SO SHOULD BE OK
                getIncludedStatuses().add(Status.DRAFT);
            }
        }
        appendStatusTypes(q, getIncludedStatuses());
        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }
        handleSearch(q);
        return SUCCESS;
    }

    public String getRssFeed() {
        if (feed != null) {
            StringWriter writer = new StringWriter();
            SyndFeedOutput output = new SyndFeedOutput();
            try {
                output.output(feed, writer);
                setRssInputStream(new ByteArrayInputStream(writer.toString().getBytes()));
            } catch (Exception e) {
                addActionErrorWithException("could not process your search", e);
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
        if (getId() != null && getId() > 0) {
            result.append(SEARCHING_FOR_RESOURCE_WITH_T_DAR_ID).append(STRONG).append(getId()).append(STRONG_CLOSE);
            this.searchPhrase = result.toString();
            return;
        }
        if (!CollectionUtils.isEmpty(getResourceTypes())) {
            result.append(SELECTED_RESOURCE_TYPES);
            Iterator<ResourceType> iter = getResourceTypes().iterator();
            while (iter.hasNext()) {
                ResourceType type = iter.next();
                if (type != null) {
                    result.append(STRONG).append(type.getLabel()).append(STRONG_CLOSE);
                }
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
                addActionErrorWithException("could not process your search", e);
            }
        }

        if (fileAccess != null) {
            result.append(LIMITED_TO).append(STRONG).append(fileAccess.getLabel()).append(STRONG_CLOSE);
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
                result.append("using ").append(STRONG).append(TITLE_TAG_KEYWORD_PHRASE).append(STRONG_CLOSE);

            } else {
                result.append(USING_KEYWORD).append(STRONG).append(query.toString()).append(STRONG_CLOSE);
            }
        }

        if (!StringUtils.isEmpty(getTitle())) {
            result.append(WITH_TEXT_IN_TITLE).append(STRONG).append(getTitle().toString()).append(STRONG_CLOSE);
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
        appendUncontrolledKeywordsFromList(result, geographicKeywords, WITH_GEOGRAPHIC_KEYWORDS);
        appendKeywordsFromList(result, selectedInvestigationTypes, WITH_INVESTIGATION_TYPES);
        appendKeywordsFromList(result, selectedMaterialKeywords, WITH_MATERIAL_KEYWORDS);
        appendKeywordsFromList(result, selectedSiteTypeKeywords, WITH_SITE_TYPE_KEYWORDS);

        appendUncontrolledKeywordsFromList(result, siteNameKeywords, WITH_SITE_NAME_KEYWORDS);
        appendUncontrolledKeywordsFromList(result, uncontrolledSiteTypeKeywords, WITH_UNCONTROLLED_SITE_TYPE_KEYWORDS);
        appendUncontrolledKeywordsFromList(result, uncontrolledCultureKeywords, WITH_UNCONTROLLED_CULTURE_KEYWORDS);

        this.searchPhrase = result.toString();
    }

    private void appendUncontrolledKeywordsFromList(StringBuilder result, List<String> list, String extra) {
        if (!isEmpty(list)) {
            result.append(extra);
            Iterator<String> iter = list.iterator();
            while (iter.hasNext()) {
                result.append(STRONG).append(iter.next()).append(STRONG_CLOSE);
                if (iter.hasNext())
                    result.append(COMMA_SEPARATOR);
            }
        }
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
        String title = TITLE_ALL_RECORDS;
        if (getId() != null && getId() > 0) {
            title = TITLE_BY_TDAR_ID;
        }

        if (!CollectionUtils.isEmpty(selectedCultureKeywords) || !CollectionUtils.isEmpty(selectedInvestigationTypes)
                || !CollectionUtils.isEmpty(selectedMaterialKeywords) || !CollectionUtils.isEmpty(selectedSiteTypeKeywords)
                || !isEmpty(siteNameKeywords)
                || !isEmpty(uncontrolledSiteTypeKeywords) || !isEmpty(uncontrolledCultureKeywords)) {
            title = TITLE_FILTERED_BY_KEYWORD;
        }

        if (!StringUtils.isEmpty(query)) {
            title = query;
            if (getQuery().contains(QueryFieldNames.INTEGRATABLE + ":true")) {
                title = TITLE_TAG_KEYWORD_PHRASE;
            }
        }
        this.searchSubtitle = title;
    }

    private void appendPersonQuery(QueryBuilder q) {
        List<Person> submitters = getPeopleFromRawList(searchSubmitterIds);
        List<Person> contributors = getPeopleFromRawList(searchContributorIds);
        if (!submitters.isEmpty()) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            for (Person submitter : submitters) {
                group.append(new FieldQueryPart(QueryFieldNames.SUBMITTER_ID, submitter.getId().toString()));
            }
            q.append(group);
        } else {
            appendManualSubmitterQuery(q, "submitter", searchSubmitter);
        }

        if (!contributors.isEmpty()) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            for (Person contributor : contributors) {
                group.append(new FieldQueryPart(QueryFieldNames.RESOURCE_CREATORS_CREATOR_ID, contributor.getId().toString()));
            }
            q.append(group);
        } else if (searchContributor != null) {
            // need a fqp for each part filled out in the form. (first name, last name)
            addEscapedWildcardField(q, QueryFieldNames.RESOURCE_CREATORS_CREATOR_NAME_KEYWORD, searchContributor.getFirstName());
            addEscapedWildcardField(q, QueryFieldNames.RESOURCE_CREATORS_CREATOR_NAME_KEYWORD, searchContributor.getLastName());
        }
    }

    private void appendManualSubmitterQuery(QueryBuilder q, String fieldPrefix, Person person) {
        if (person == null)
            return;
        addEscapedWildcardField(q, fieldPrefix + ".firstName", person.getFirstName());
        addEscapedWildcardField(q, fieldPrefix + ".lastName", person.getLastName());
        addEscapedWildcardField(q, fieldPrefix + ".email", person.getEmail());
        if (checkMinString(person.getInstitutionName()) && StringUtils.isNotBlank(person.getInstitutionName())) {
            FieldQueryPart fqp = new FieldQueryPart(fieldPrefix + ".institution.name_auto", person.getInstitutionName());
            if (person.getInstitutionName().contains(" ")) {
                fqp.setQuotedEscapeValue(person.getInstitutionName());
            }
            q.append(fqp);
        }
    }

    private void appendAdminQuery(QueryBuilder q) {
        // if(!isAdministrator()) return;
        appendPersonQuery(q);

        appendUpdateDateRangeQuery(q);
    }

    // FIXME!!!!
    private void appendUpdateDateRangeQuery(QueryBuilder q) {

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
        Set<MaterialKeyword> keywords = getMaterialKeywordService().findByIds(materialKeywordIds);
        selectedMaterialKeywords.addAll(keywords);
        appendKeywords(selectedMaterialKeywords, QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, q);
    }

    private void appendCultureKeywordQuery(QueryBuilder q) {
        Set<CultureKeyword> cultureKeywords = getCultureKeywordService().findByIds(approvedCultureKeywordIds);
        selectedCultureKeywords.addAll(cultureKeywords);
        appendKeywords(cultureKeywords, QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, q);
        if (getUncontrolledCultureKeywords() == null)
            return;

        appendKeywords(getUncontrolledCultureKeywords(), QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, q);

    }

    @SuppressWarnings("rawtypes")
    private void appendKeywords(Collection keywords, String type, QueryBuilder q) {
        if (!keywords.isEmpty())
            q.append(new KeywordQueryPart(type, keywords));

    }

    private void appendSiteTypeKeywordQuery(QueryBuilder q) {
        Set<SiteTypeKeyword> siteTypeKeywords = getSiteTypeKeywordService().findByIds(approvedSiteTypeKeywordIds);
        selectedSiteTypeKeywords.addAll(siteTypeKeywords);
        appendKeywords(siteTypeKeywords, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, q);
        if (uncontrolledSiteTypeKeywords == null)
            return;
        appendKeywords(uncontrolledSiteTypeKeywords, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, q);
    }

    private void appendSiteNameKeywordQuery(QueryBuilder q) {
        if (siteNameKeywords == null)
            return;
        appendKeywords(siteNameKeywords, QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, q);
    }

    private void appendGeographicKeywordQuery(QueryBuilder q) {
        if (geographicKeywords == null)
            return;
        appendKeywords(geographicKeywords, QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, q);
    }

    private void appendInvestigationTypeQuery(QueryBuilder q) {
        Set<InvestigationType> investigationTypes = getInvestigationTypeService().findByIds(investigationTypeIds);
        selectedInvestigationTypes.addAll(investigationTypes);
        appendKeywords(investigationTypes, QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, q);
    }

    private FacetingRequest facetOn(String name, String field, FullTextQuery ftq, List<Facet> facetList) {
        FacetingRequest facetRequest = getSearchService().getQueryBuilder(Resource.class).facet().name(name).onField(field).discrete()
                .orderedBy(FacetSortOrder.COUNT_DESC).includeZeroCounts(false).createFacetingRequest();

        if (name.equals(QueryFieldNames.DATE_CREATED)) {
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

    @Override
    public void addFacets(FullTextQuery ftq) {
        facetOn(QueryFieldNames.RESOURCE_TYPE, QueryFieldNames.RESOURCE_TYPE, ftq, getResourceTypeFacets());
        facetOn(QueryFieldNames.DOCUMENT_TYPE, QueryFieldNames.DOCUMENT_TYPE, ftq, getDocumentTypeFacets());
        facetOn(QueryFieldNames.RESOURCE_ACCESS_TYPE, QueryFieldNames.RESOURCE_ACCESS_TYPE, ftq, getFileAccessFacets());
        facetOn("cultures", QueryFieldNames.ACTIVE_CULTURE_KEYWORDS_LABEL, ftq, getCultureFacets());
        facetOn("location", QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS_LABEL, ftq, getLocationFacets());
        facetOn(QueryFieldNames.DATE_CREATED, QueryFieldNames.DATE_CREATED, ftq, getDateCreatedFacets());
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
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
            logger.debug("{}", ie);
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

    // public Boolean getShowIndependentProjects() {
    // return showIndependentProjects;
    // }
    //
    // public void setShowIndependentProjects(Boolean showIndependentProjects) {
    // logger.debug("show independent projects: {}", showIndependentProjects);
    // this.showIndependentProjects = showIndependentProjects;
    // }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getMaterialKeywordService().findAll();
            Collections.sort(allMaterialKeywords);
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
            approvedCultureKeywords = KeywordNode.organizeKeywords(getCultureKeywordService().findAllApproved());
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
            Collections.sort(allInvestigationTypes);
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
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(getSiteTypeKeywordService().findAllApproved());
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

    // FIXME: this business of lists with single nulls needs to go away. In the meantime, use this to check if a list is "empty-ish"
    private <E extends Object> boolean isEmpty(Collection<E> col) {
        if (CollectionUtils.isEmpty(col))
            return true;
        return col.size() == 1 && col.iterator().next() == null;
    }

    public List<SortOption> getSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    /**
     * @return the fileAccessFacets
     */
    public List<Facet> getFileAccessFacets() {
        return fileAccessFacets;
    }

    /**
     * @param fileAccessFacets
     *            the fileAccessFacets to set
     */
    public void setFileAccessFacets(List<Facet> fileAccessFacets) {
        this.fileAccessFacets = fileAccessFacets;
    }

    public ResourceAccessType getFileAccess() {
        return fileAccess;
    }

    public void setFileAccess(ResourceAccessType fileAccess) {
        this.fileAccess = fileAccess;
    }

    /**
     * @return the dateCreated
     */
    public String getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated
     *            the dateCreated to set
     */
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Person getSearchSubmitter() {
        if (searchSubmitter == null) {
            searchSubmitter = new Person();
        }
        return searchSubmitter;
    }

    public void setSearchSubmitter(Person searchSubmitter) {
        this.searchSubmitter = searchSubmitter;
    }

    public Person getSearchContributor() {
        if (searchContributor == null) {
            searchContributor = new Person();
        }
        return searchContributor;
    }

    public void setSearchContributor(Person searchContributor) {
        this.searchContributor = searchContributor;
    }

    public List<String> getGeographicKeywords() {
        return geographicKeywords;
    }

    public void setGeographicKeywords(List<String> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }
}
