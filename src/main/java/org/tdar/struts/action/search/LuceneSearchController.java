package org.tdar.struts.action.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;
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
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.RssService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.KeywordQueryPart;
import org.tdar.search.query.QueryDescriptionBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.RangeQueryPart;
import org.tdar.search.query.ResourceTypeQueryPart;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.SpatialQueryPart;
import org.tdar.search.query.TemporalLimit;
import org.tdar.search.query.TemporalQueryPart;
import org.tdar.search.query.queryBuilder.QueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceQueryBuilder;
import org.tdar.struts.data.KeywordNode;

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
public class LuceneSearchController extends AbstractLookupController<Resource> {

    private static final long serialVersionUID = 3869953915909593269L;

    private String rssUrl;
    private String query;
    private String rawQuery;
    private String referrer;
    private Double minx;
    private Double maxx;
    private Double miny;
    private Double maxy;
    private Integer minDateValue = -1000000000;
    private Integer maxDateValue = 1000000000;
    private DocumentType documentType;
    private Integer dateCreatedMin;
    private Integer dateCreatedMax;
    private List<Project> projects;
    private int defaultRecordsPerPage = 20;
    private List<Long> projectIds;

    private Date dateRegisteredStart;
    private Date dateRegisteredEnd;
    private Date dateUpdatedStart;
    private Date dateUpdatedEnd;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private RssService rssService;

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

    private List<String> otherKeywords = new ArrayList<String>();
    private List<String> geographicKeywords = new ArrayList<String>();
    private List<String> temporalKeywords = new ArrayList<String>();

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private List<Long> approvedSiteTypeKeywordIds;
    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> siteNameKeywords;
    private ResourceAccessType fileAccess;
    private List<SiteTypeKeyword> selectedSiteTypeKeywords = new ArrayList<SiteTypeKeyword>();
    private QueryBuilder q = new ResourceQueryBuilder();
    private Long contentLength;

    private InputStream inputStream;
    private String searchPhrase;
    private String searchSubtitle;
    private QueryDescriptionBuilder descBuilder = new QueryDescriptionBuilder();
    private List<Long> searchSubmitterIds = new ArrayList<Long>();
    private List<Long> searchContributorIds = new ArrayList<Long>();
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
    
    private List<CoverageDate> coverageDates = new ArrayList<CoverageDate>();

    @Autowired
    private ExcelService excelService;

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

    @Action(value = "download", results = {
            @Result(name = "success", type = "stream", params = {
                    "contentType", "application/vnd.ms-excel",
                    "inputName", "inputStream",
                    "contentDisposition", "attachment;filename=\"report.xls",
                    "contentLength", "${contentLength}" })
    })
    public String viewExcelReport() throws ParseException {
        try {
        	if(!isAuthenticated()) return UNAUTHORIZED;
            prepareSearchQuery();
            setMode("excel");
            setRecordsPerPage(200);
            handleSearch(q);
            int rowNum = 0;
            int maxRow = getMaxDownloadRecords();
            if (maxRow > getTotalRecords()) {
                maxRow = getTotalRecords();
            }
            if (getTotalRecords() > 0) {
                HSSFSheet sheet = excelService.createWorkbook("results");

                List<String> fieldNames = new ArrayList<String>(Arrays.asList("ID", "ResourceType", "Title", "Date", "Authors", "Project", "Description",
                        "Number Of Files", "URL",
                        "Physical Location"));

                if (isEditor()) {
                    fieldNames.add("Status");
                    fieldNames.add("Date Added");
                    fieldNames.add("Submitted By");
                    fieldNames.add("Date Last Updated");
                    fieldNames.add("Updated By");
                }

                // ADD HEADER ROW THAT SHOWS URL and SEARCH PHRASE
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, fieldNames.size()));
                excelService.addDocumentHeaderRow(sheet, rowNum, 0, Arrays.asList("tDAR Search Results: " + getDescBuilder().toString()));
                rowNum++;
                excelService.addPairedHeaderRow(
                        sheet,
                        rowNum,
                        0,
                        Arrays.asList("Search Url: ", urlService.getBaseUrl() + getServletRequest().getRequestURI().replace("/download", "/results") + "?"
                                + getServletRequest().getQueryString()));
                rowNum++;
                excelService.addPairedHeaderRow(sheet, rowNum, 0,
                        Arrays.asList("Downloaded by: ", getAuthenticatedUser().getProperName() + " on " + new Date()));
                rowNum++;
                rowNum++;
                excelService.addHeaderRow(sheet, rowNum, 0, fieldNames);
                int startRecord = 0;
                int currentRecord = 0;
                while (currentRecord < maxRow) {
                    startRecord = getNextPageStartRecord();
                    setStartRecord(getNextPageStartRecord()); // resetting for next search
                    for (Resource result : getResults()) {
                        rowNum++;
                        if(currentRecord++ > maxRow) break;
                        Resource r = (Resource) result;
                        Integer dateCreated = null;
                        Integer numFiles = 0;
                        if (result instanceof InformationResource) {
                            dateCreated = ((InformationResource) result).getDate();
                            numFiles = ((InformationResource) result).getTotalNumberOfFiles();
                        }
                        List<Creator> authors = new ArrayList<Creator>();

                        for (ResourceCreator creator : r.getPrimaryCreators()) {
                            authors.add(creator.getCreator());
                        }
                        String location = "";
                        String projectName = "";
                        if (r instanceof InformationResource) {
                            InformationResource ires = ((InformationResource) r);
                            location = ires.getCopyLocation();
                            projectName = ires.getProjectTitle();

                        }
                        ArrayList<Object> data = new ArrayList<Object>(Arrays.asList(r.getId(), r.getResourceType(), r.getTitle(), dateCreated,
                                authors, projectName, r.getShortenedDescription(), numFiles, urlService.absoluteUrl(r), location));

                        if (isEditor()) {
                            data.add(r.getStatus());
                            data.add(r.getDateCreated());
                            data.add(r.getSubmitter().getProperName());
                            data.add(r.getDateUpdated());
                            data.add(r.getUpdatedBy().getProperName());
                        }

                        excelService.addDataRow(sheet, rowNum, 0, data);
                    }
                    if (startRecord < getTotalRecords()) {
                        handleSearch(q);
                    }
                }

                excelService.setColumnWidth(sheet, 0, 5000);

                File tempFile = File.createTempFile("results", "xls");
                FileOutputStream fos = new FileOutputStream(tempFile);
                sheet.getWorkbook().write(fos);
                fos.close();
                setInputStream(new FileInputStream(tempFile));
                setContentLength(tempFile.length());
            }
        } catch (Exception e) {
            addActionErrorWithException("something happened with excel export", e);
            return INPUT;
        }

        return SUCCESS;
    }

    @Action(value = "rss", results = {
            @Result(name = "success", type = "stream", params = {
                    "documentName", "rssFeed",
                    "formatOutput", "true",
                    "inputName", "inputStream",
                    "contentType", "application/rss+xml",
                    "contentLength", "${contentLength}",
                    "contentEncoding", "UTF-8" })
    })
    public String viewRss() {
        try {
            setSortField(SortOption.ID_REVERSE);
            setSecondarySortField(SortOption.TITLE);
            setMode("rss");
            performSearch();
            setInputStream(rssService.createRssFeedFromResourceList(getSessionData().getPerson(), getSearchSubtitle(), getDescBuilder().toString(),
                    getResults(),
                    getRecordsPerPage(), getStartRecord(), getTotalRecords(), getRssUrl()));
        } catch (Exception e) {
            logger.error("rss error", e);
            addActionErrorWithException("could not process your search", e);
        }
        return SUCCESS;
    }

    // If there's an error due to invalid input, it doesn't appear to be enough to simply forward back to the search page, because we
    // still get ftl errors because the freemarker chokes on the same invalid input from original request. So we redirect instead.
    @Action(value = "results", results = { @Result(name = "success", location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String search() {
        logger.trace("begin search");
        if (getMode() == null) {
            setMode("SEARCH");
        }
        try {
            performSearch();
        } catch (Exception e) {
            logger.error("search error occurred", e);
            addActionErrorWithException("could not process your search", e);
            return INPUT;
        }
        logger.trace("search completed search");
        return SUCCESS;
    }

    public void performSearch() throws ParseException {
        logger.trace("prepare search Query");
        prepareSearchQuery();
        logger.trace("handle search");
        handleSearch(q);
        logger.trace("set rss URL");
        setRssUrl();
    }

    private FieldQueryPart setupUsingStrict(boolean strictParsing, String name, String value) {
        FieldQueryPart part = new FieldQueryPart(name);
        if (!strictParsing) {
            part.setEscapedValue(value);
        } else {
            part.setFieldValue(value);
        }
        return part;
    }

    void prepareSearchQuery() throws ParseException {
        // FIXME: jtd: this method is too big, refactor and clarify logic
        setSearchSubtitle(QueryDescriptionBuilder.TITLE_ALL_RECORDS);
        if (getId() != null && getId() > 0) { // ignore all other options if we are searching for specific tdar-id
            q.append(new FieldQueryPart(QueryFieldNames.ID, Long.toString(getId())));
            getDescBuilder().append(QueryDescriptionBuilder.SEARCHING_FOR_RESOURCE_WITH_T_DAR_ID, getId());
            setSearchPhrase(getDescBuilder().toHtml());
            return;
        }

        if (getRecordsPerPage() == 0) {
            setRecordsPerPage(defaultRecordsPerPage);
        }
        
        if (StringUtils.isNotEmpty(getRawQuery())) {
            q.setRawQuery(getRawQuery());
            getDescBuilder().append(QueryDescriptionBuilder.RAW_QUERY, getRawQuery());
            setSearchPhrase(getDescBuilder().toHtml());
            return;
        }
        
        appendGeneralPhraseQuery(q);
        
        appendTemporalLimitQuery(q);

        if (fileAccess != null) {
            getDescBuilder().append(QueryDescriptionBuilder.FILE_ACCESS, fileAccess);
            q.append(new FieldQueryPart(QueryFieldNames.RESOURCE_ACCESS_TYPE, fileAccess));
        }

        appendTitleQuery(q);

        QueryPartGroup projectQueryGroup = new QueryPartGroup();
        projectQueryGroup.setOperator(Operator.OR);
        if (!CollectionUtils.isEmpty(projectIds)) {
            boolean valid = false;
            getDescBuilder().append(QueryDescriptionBuilder.PROJECT, projectIds);
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

        if (dateCreatedMin != null && dateCreatedMax != null) {
            RangeQueryPart part = new RangeQueryPart(QueryFieldNames.DATE, dateCreatedMin.toString(), dateCreatedMax.toString());
            q.append(part);
            getDescBuilder().appendRange(QueryDescriptionBuilder.DATE_CREATED_RANGE_BETWEEN, dateCreatedMin.toString(), dateCreatedMax.toString());
        } else if (dateCreatedMax != null) {
            RangeQueryPart part = new RangeQueryPart(QueryFieldNames.DATE, minDateValue.toString(), dateCreatedMax.toString());
            getDescBuilder().appendRange(QueryDescriptionBuilder.DATE_CREATED_RANGE_BEFORE, "", dateCreatedMax.toString());
            q.append(part);
        } else if (dateCreatedMin != null) {
            RangeQueryPart part = new RangeQueryPart(QueryFieldNames.DATE, dateCreatedMin.toString(), maxDateValue.toString());
            getDescBuilder().appendRange(QueryDescriptionBuilder.DATE_CREATED_RANGE_AFTER, dateCreatedMin.toString(), "");
            q.append(part);
        }

        if (minx != null && maxx != null && miny != null && maxy != null) {
            SpatialQueryPart sq = new SpatialQueryPart();
            getDescBuilder().appendSpatialQuery(QueryDescriptionBuilder.WITHIN_MAP_CONSTRAINTS, minx, maxx, miny, maxy);
            LatitudeLongitudeBox limit = new LatitudeLongitudeBox(minx, miny, maxx, maxy);
            if (!limit.isValid()) {
                throw new TdarRecoverableRuntimeException("the bounding box specified is not valid ");
            }
            sq.addSpatialLimit(limit);
            q.append(sq);
        }
        if (!CollectionUtils.isEmpty(getResourceTypes())) {
            getDescBuilder().append(QueryDescriptionBuilder.SELECTED_RESOURCE_TYPES, getResourceTypes());
            ResourceTypeQueryPart rtq = new ResourceTypeQueryPart();
            for (ResourceType type : getResourceTypes()) {
                rtq.addResourceTypeLimit(type);
            }
            q.append(rtq);
        }

        if (getDocumentType() != null) {
            getDescBuilder().append(QueryDescriptionBuilder.SELECTED_DOCUMENT_TYPES, getDocumentType());
            if (getDocumentType() != null) {
                q.append(new FieldQueryPart(QueryFieldNames.DOCUMENT_TYPE, getDocumentType()));
            }
        }

        if (StringUtils.isNotEmpty(query) && query.contains(QueryFieldNames.INTEGRATABLE)) {
            getDescBuilder().append(QueryDescriptionBuilder.TITLE_TAG_KEYWORD_PHRASE, "true");
        }

        appendMaterialKeywordQuery(q);
        appendCultureKeywordQuery(q);
        appendInvestigationTypeQuery(q);
        appendSiteTypeKeywordQuery(q);
        appendSiteNameKeywordQuery(q);
        appendAdminQuery(q);
        appendOtherKeywordQuery(q);
        appendTemporalKeywordQuery(q);
        appendGeographicKeywordQuery(q);
        appendDateRegisteredQuery(q);
        appendDateUpdatedQuery(q);

        if (getAuthenticatedUser() != null && isUseSubmitterContext()) {
            q.append(new FieldQueryPart(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId().toString()));
        }

        appendStatusInformation(q, getDescBuilder(), getIncludedStatuses(), getAuthenticatedUser());

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }
        logger.trace("setting search phrase");
        setSearchPhrase(getDescBuilder().toHtml());
        return;
    }


    private void appendGeneralPhraseQuery(QueryBuilder q) {
        if (StringUtils.isBlank(query)) return;
        
        setSearchSubtitle(query);
        getDescBuilder().append(QueryDescriptionBuilder.USING_KEYWORD, query);
        
        String cleanedQueryString = query.trim();
        // if we have a leading and trailng quote, strip them
        if (cleanedQueryString.startsWith("\"") && cleanedQueryString.endsWith("\"")) {
            cleanedQueryString = cleanedQueryString.substring(1, cleanedQueryString.length() - 1);
        }
        cleanedQueryString = QueryParser.escape(cleanedQueryString);

        //undo why??
//        // still quotes... undo
//        if (cleanedQueryString.contains("\"")) {
//            cleanedQueryString = query;
//        }

        QueryPartGroup primary = new QueryPartGroup();

        if (cleanedQueryString.contains(" ")) {
            // quoting so we can do things like proximity
            cleanedQueryString = "\"" + cleanedQueryString + "\"";
        }

        FieldQueryPart titlePart = new FieldQueryPart(QueryFieldNames.TITLE, cleanedQueryString);
        FieldQueryPart descriptionPart = new FieldQueryPart(QueryFieldNames.DESCRIPTION, cleanedQueryString);
        FieldQueryPart creatorPart = new FieldQueryPart(QueryFieldNames.RESOURCE_CREATORS_PROPER_NAME, cleanedQueryString);
        FieldQueryPart content = new FieldQueryPart(QueryFieldNames.CONTENT, cleanedQueryString);
        FieldQueryPart allFields = new FieldQueryPart(QueryFieldNames.ALL, cleanedQueryString).setBoost(2f);

        if (cleanedQueryString.contains(" ")) {
            // APPLIES WEIGHTING BASED ON THE "PHRASE" NOT THE TERM
            FieldQueryPart phrase = new FieldQueryPart(QueryFieldNames.ALL_PHRASE, cleanedQueryString);
            //FIXME: magic words
            phrase.setProximity(4);
            phrase.setBoost(3.2f);
            primary.append(phrase);
            creatorPart.setProximity(2);
            titlePart.setProximity(3);
            descriptionPart.setProximity(4);
        }
        primary.append(titlePart.setBoost(6f));
        primary.append(descriptionPart.setBoost(4f));
        primary.append(creatorPart.setBoost(5f));

        primary.append(content);
        primary.append(allFields);
        q.append(primary);
        primary.setOperator(Operator.OR);
    }
    
    private void appendTitleQuery(QueryBuilder q) {
        if (StringUtils.isBlank(getTitle())) return;
        getDescBuilder().append(QueryDescriptionBuilder.WITH_TEXT_IN_TITLE, getTitle());
        QueryPartGroup group= new QueryPartGroup();
        group.setOperator(Operator.OR);
        FieldQueryPart wordsInTitle = new FieldQueryPart(QueryFieldNames.TITLE);
        wordsInTitle.setQuotedEscapeValue(getTitle());
        FieldQueryPart wholeTitle = new FieldQueryPart(QueryFieldNames.TITLE);
        wholeTitle.setQuotedEscapeValue(getTitle());
        wholeTitle.setBoost(6f);//FIXME: magic numbers
        group.append(wholeTitle);
        group.append(wordsInTitle);
        q.append(group);
    }
    
    public String getSearchPhraseHtml() {
        return getDescBuilder().toHtml();
    }

    public String getSearchSubtitle() {
        return searchSubtitle;
    }

    public void setSearchSubtitle(String title) {
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
                getDescBuilder().appendSubmitter(submitter);
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
                getDescBuilder().appendAuthor(contributor);
            }
            q.append(group);
        } else if (searchContributor != null) {
            // need a fqp for each part filled out in the form. (first name, last name)
            addEscapedWildcardField(q, QueryFieldNames.RESOURCE_CREATORS_CREATOR_NAME_KEYWORD, searchContributor.getFirstName());
            addEscapedWildcardField(q, QueryFieldNames.RESOURCE_CREATORS_CREATOR_NAME_KEYWORD, searchContributor.getLastName());
            getDescBuilder().appendAuthor(searchContributor);
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
        getDescBuilder().appendSubmitter(person);
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
        Set<MaterialKeyword> keywords = getGenericKeywordService().findByIds(MaterialKeyword.class, materialKeywordIds);
        getSelectedMaterialKeywords().addAll(keywords);
        getDescBuilder().append(QueryDescriptionBuilder.WITH_MATERIAL_KEYWORDS, getSelectedMaterialKeywords());
        appendKeywords(getSelectedMaterialKeywords(), QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, q);
    }
    
    

    private void appendCultureKeywordQuery(QueryBuilder q) {
        Set<CultureKeyword> cultureKeywords = getGenericKeywordService().findByIds(CultureKeyword.class, approvedCultureKeywordIds);
        selectedCultureKeywords.addAll(cultureKeywords);
        getDescBuilder().append(QueryDescriptionBuilder.WITH_CULTURE_KEYWORDS, selectedCultureKeywords);
        appendKeywords(cultureKeywords, QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, q);
        if (getUncontrolledCultureKeywords() == null)
            return;

        appendKeywords(getUncontrolledCultureKeywords(), QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, q);

    }

    @SuppressWarnings("rawtypes")
    private void appendKeywords(Collection keywords, String type, QueryBuilder q) {
        if (!keywords.isEmpty()) {
            q.append(new KeywordQueryPart(type, keywords));
        }
    }

    private void appendSiteTypeKeywordQuery(QueryBuilder q) {
        Set<SiteTypeKeyword> siteTypeKeywords = getGenericKeywordService().findByIds(SiteTypeKeyword.class, approvedSiteTypeKeywordIds);
        selectedSiteTypeKeywords.addAll(siteTypeKeywords);
        getDescBuilder().append(QueryDescriptionBuilder.WITH_SITE_TYPE_KEYWORDS, selectedSiteTypeKeywords);
        appendKeywords(siteTypeKeywords, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, q);
        if (uncontrolledSiteTypeKeywords == null)
            return;
        appendKeywords(uncontrolledSiteTypeKeywords, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, q);
    }
    
    private void appendTemporalLimitQuery(QueryBuilder q) {
        if(CollectionUtils.isEmpty(coverageDates)) return;
        if(!coverageDates.get(0).isValid()) return;
        TemporalQueryPart tq = new TemporalQueryPart();
        tq.addTemporalLimit(new TemporalLimit(coverageDates.get(0)));
        getDescBuilder().appendCoverageDate(coverageDates.get(0));
        q.append(tq);
    }

    private void appendSiteNameKeywordQuery(QueryBuilder q) {
        if (siteNameKeywords == null)
            return;
        getDescBuilder().append(QueryDescriptionBuilder.WITH_SITE_NAME_KEYWORDS, siteNameKeywords);
        appendKeywords(siteNameKeywords, QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, q);
    }

    private void appendInvestigationTypeQuery(QueryBuilder q) {
        Set<InvestigationType> investigationTypes = getGenericKeywordService().findByIds(InvestigationType.class, investigationTypeIds);
        getDescBuilder().append(QueryDescriptionBuilder.WITH_INVESTIGATION_TYPES, investigationTypes);
        selectedInvestigationTypes.addAll(investigationTypes);
        appendKeywords(investigationTypes, QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, q);
    }

    private void appendOtherKeywordQuery(QueryBuilder q) {
        getDescBuilder().append(QueryDescriptionBuilder.WITH_OTHER_KEYWORDS, otherKeywords);
        appendKeywords(otherKeywords, QueryFieldNames.ACTIVE_OTHER_KEYWORDS, q);
    }

    private void appendTemporalKeywordQuery(QueryBuilder q) {
        getDescBuilder().append(QueryDescriptionBuilder.WITH_TEMPORAL_KEYWORDS, getTemporalKeywords());
        appendKeywords(getTemporalKeywords(), QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, q);
    }

    private void appendGeographicKeywordQuery(QueryBuilder q) {
        getDescBuilder().append(QueryDescriptionBuilder.WITH_GEOGRAPHIC_KEYWORDS, getGeographicKeywords());
        appendKeywords(getGeographicKeywords(), QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, q);
    }

    private void appendDateUpdatedQuery(QueryBuilder q) {
        if (dateUpdatedStart == null && dateUpdatedEnd == null)
            return;
        if (dateUpdatedStart != null)
            getDescBuilder().append(QueryDescriptionBuilder.WITH_UPDATE_DATE_AFTER, dateUpdatedStart);
        if (dateUpdatedEnd != null)
            getDescBuilder().append(QueryDescriptionBuilder.WITH_UPDATE_DATE_BEFORE, dateUpdatedEnd);
        RangeQueryPart dateRangeQueryPart = new RangeQueryPart(QueryFieldNames.DATE_UPDATED, dateUpdatedStart, dateUpdatedEnd);
        q.append(dateRangeQueryPart);
    }

    private void appendDateRegisteredQuery(QueryBuilder q) {
        if (dateRegisteredStart == null && dateRegisteredEnd == null)
            return;
        if (dateRegisteredStart != null)
            getDescBuilder().append(QueryDescriptionBuilder.WITH_REGISTRATION_DATE_AFTER, dateRegisteredStart);
        if (dateRegisteredEnd != null)
            getDescBuilder().append(QueryDescriptionBuilder.WITH_REGISTRATION_DATE_BEFORE, dateRegisteredEnd);
        RangeQueryPart dateRangeQueryPart = new RangeQueryPart(QueryFieldNames.DATE_CREATED, dateRegisteredStart, dateRegisteredEnd);
        q.append(dateRangeQueryPart);
    }

    private FacetingRequest facetOn(String name, String field, FullTextQuery ftq, List<Facet> facetList) {
        FacetingRequest facetRequest = getSearchService().getQueryBuilder(Resource.class).facet().name(name).onField(field).discrete()
                .orderedBy(FacetSortOrder.COUNT_DESC).includeZeroCounts(false).createFacetingRequest();

        if (name.equals(QueryFieldNames.DATE)) {
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
//        facetOn("cultures", QueryFieldNames.ACTIVE_CULTURE_KEYWORDS_LABEL, ftq, getCultureFacets());
//        facetOn("location", QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS_LABEL, ftq, getLocationFacets());
//        facetOn(QueryFieldNames.DATE, QueryFieldNames.DATE, ftq, getDateCreatedFacets());
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

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getGenericKeywordService().findAll(MaterialKeyword.class);
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
            approvedCultureKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(CultureKeyword.class));
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
            allInvestigationTypes = getGenericKeywordService().findAll(InvestigationType.class);
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
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(SiteTypeKeyword.class));
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

    public void setInputStream(InputStream rssInputStream) {
        this.inputStream = rssInputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
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

    /**
     * @return the dateCreatedMax
     */
    public Integer getDateCreatedMax() {
        return dateCreatedMax;
    }

    /**
     * @param dateCreatedMax
     *            the dateCreatedMax to set
     */
    public void setDateCreatedMax(Integer dateCreatedMax) {
        this.dateCreatedMax = dateCreatedMax;
    }

    /**
     * @return the dateCreatedMin
     */
    public Integer getDateCreatedMin() {
        return dateCreatedMin;
    }

    /**
     * @param dateCreatedMin
     *            the dateCreatedMin to set
     */
    public void setDateCreatedMin(Integer dateCreatedMin) {
        this.dateCreatedMin = dateCreatedMin;
    }

    /**
     * @return the descBuilder
     */
    public QueryDescriptionBuilder getDescBuilder() {
        return descBuilder;
    }

    /**
     * @param descBuilder
     *            the descBuilder to set
     */
    public void setDescBuilder(QueryDescriptionBuilder descBuilder) {
        this.descBuilder = descBuilder;
    }

    /**
     * @return the documentType
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * @param documentType
     *            the documentType to set
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    /**
     * @return the searchPhrase
     */
    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * @param searchPhrase
     *            the searchPhrase to set
     */
    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    /**
     * @return the maxDateValue
     */
    public Integer getMaxDateValue() {
        return maxDateValue;
    }

    /**
     * @param maxDateValue
     *            the maxDateValue to set
     */
    public void setMaxDateValue(Integer maxDateValue) {
        this.maxDateValue = maxDateValue;
    }

    /**
     * @return the minDateValue
     */
    public Integer getMinDateValue() {
        return minDateValue;
    }

    /**
     * @param minDateValue
     *            the minDateValue to set
     */
    public void setMinDateValue(Integer minDateValue) {
        this.minDateValue = minDateValue;
    }

    /**
     * @return the contentLength
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * @param contentLength
     *            the contentLength to set
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
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

    public int getDefaultRecordsPerPage() {
        return defaultRecordsPerPage;
    }

    public void setDefaultRecordsPerPage(int defaultRecordsPerPage) {
        this.defaultRecordsPerPage = defaultRecordsPerPage;
    }

    public List<String> getOtherKeywords() {
        return otherKeywords;
    }

    public List<String> getGeographicKeywords() {
        return geographicKeywords;
    }

    public List<String> getTemporalKeywords() {
        return temporalKeywords;
    }

    public Integer getMaxDownloadRecords() {
        return TdarConfiguration.getInstance().getSearchExcelExportRecordMax();
    }

    public Date getDateRegisteredStart() {
        return dateRegisteredStart;
    }

    public void setDateRegisteredStart(Date dateRegisteredStart) {
        this.dateRegisteredStart = dateRegisteredStart;
    }

    public Date getDateRegisteredEnd() {
        return dateRegisteredEnd;
    }

    public void setDateRegisteredEnd(Date dateRegisteredEnd) {
        this.dateRegisteredEnd = dateRegisteredEnd;
    }

    public Date getDateUpdatedStart() {
        return dateUpdatedStart;
    }

    public void setDateUpdatedStart(Date dateUpdatedStart) {
        this.dateUpdatedStart = dateUpdatedStart;
    }

    public Date getDateUpdatedEnd() {
        return dateUpdatedEnd;
    }

    public void setDateUpdatedEnd(Date dateUpdatedEnd) {
        this.dateUpdatedEnd = dateUpdatedEnd;
    }

    public void setGeographicKeywords(List<String> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }

    public void setTemporalKeywords(List<String> temporalKeywords) {
        this.temporalKeywords = temporalKeywords;
    }

    public void setSelectedMaterialKeywords(List<MaterialKeyword> selectedMaterialKeywords) {
        this.selectedMaterialKeywords = selectedMaterialKeywords;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public List<CoverageDate> getCoverageDates() {
        return coverageDates;
    }

    public void setCoverageDates(List<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }
    
    public CoverageDate getBlankCoverageDate() {
        return new CoverageDate(CoverageType.CALENDAR_DATE);
    }

    public List<CoverageType> getAllCoverageTypes() {
        List<CoverageType> coverageTypes = new ArrayList<CoverageType>();
        coverageTypes.add(CoverageType.CALENDAR_DATE);
        coverageTypes.add(CoverageType.RADIOCARBON_DATE);
        return coverageTypes;
    }


}
