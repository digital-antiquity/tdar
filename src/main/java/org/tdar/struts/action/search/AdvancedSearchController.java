package org.tdar.struts.action.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.poi.ss.usermodel.Sheet;
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
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.RssService;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralSearchQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.data.KeywordNode;

/**
 * Eventual replacement for LuceneSearchController. extending
 * LuceneSearchController is a temporary hack to allow me to replace
 * functionality in piecemeal fashion.
 * 
 * @author jimdevos
 * 
 */
@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
public class AdvancedSearchController extends
        AbstractLookupController<Resource> {

    @Autowired
    private RssService rssService;
    @Autowired
    private ExcelService excelService;

    private InputStream inputStream;

    // Search titles
    public static final String TITLE_ALL_RECORDS = "All Records";
    public static final String TITLE_ALL_COLLECTIONS = "All Collections";
    public static final String TITLE_FILTERED_BY_KEYWORD = "Filtered by Keyword";
    public static final String TITLE_BY_TDAR_ID = "Search by TDAR ID";
    public static final String TITLE_TAG_KEYWORD_PHRASE = "Referred Query from the Transatlantic Archaeology Gateway";

    // error message of last resort. User entered something we did not
    // anticipate, and we ultimately translated it into query that lucene can't
    // parse
    private static final String ERROR_PARSING_FAILED = "your search is no good.  please try again";

    private static final long serialVersionUID = 1L;

    private List<SearchFieldType> allSearchFieldTypes = SearchFieldType
            .getSearchFieldTypesByGroup();
    // basic searches go in "query"
    private String query = "";

    private List<SearchParameters> groups = new ArrayList<SearchParameters>();

    private Operator topLevelOperator = Operator.AND;

    private List<SortOption> sortOptions = SortOption
            .getOptionsForContext(Resource.class);

    // facet statistics for results.ftl
    private List<ResourceType> resourceTypeFacets = new ArrayList<ResourceType>();
    private List<DocumentType> documentTypeFacets = new ArrayList<DocumentType>();
    private List<ResourceAccessType> fileAccessFacets = new ArrayList<ResourceAccessType>();
    private List<IntegratableOptions> integratableOptionFacets = new ArrayList<IntegratableOptions>();

    // we plan to support some types of legacy requests. For example, the old
    // querystring format for id searches, basic search, and search by keyword
    // we will do this by having the same setter names as the old search
    // controller for these search types, but we will stuff them in a
    // searchParams
    // instance
    private SearchParameters legacySearchParameters = new SearchParameters();

    // SearchParams.toQueryGroup only returns 'dehydrated' query parts. after
    // the search they will (potentially) be hydrated.
    // let's hang on to that state for the search phrase
    private QueryPartGroup topLevelQueryPart;
    private QueryPartGroup reservedQueryPart;

    // support for "explore" requests
    private boolean explore = false;
    private String letter;

    // contentLength for excel download requests
    private Long contentLength;

    @Action(value = "results", results = {
            @Result(name = "success", location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String search() {
        if (explore)
            exploreSearch();
        boolean resetSearch = processLegacySearch();

        try {
            if (StringUtils.isNotBlank(query) && !resetSearch) {
                logger.trace("running basic search");
                return basicSearch();
            } else {
                return advancedSearch();
            }
        } catch (TdarRecoverableRuntimeException trex) {
            logger.error("an error happened", trex);
            addActionError(trex.getMessage());
            return INPUT;
        }
    }

    @Action(value = "collections", results = {
            @Result(name = "success", location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String searchCollections() {
        setSortOptions(SortOption
                .getOptionsForContext(ResourceCollection.class));
        try {
            return collectionSearch();
        } catch (TdarRecoverableRuntimeException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
    }

    // FIXME: "explore" results belong in a separate controller.
    public String exploreSearch() {
        processExploreRequest();
        advancedSearch();
        return SUCCESS;
    }

    private String collectionSearch() {
        determineCollectionSearchTitle();
        QueryBuilder queryBuilder = new ResourceCollectionQueryBuilder();
        queryBuilder.setOperator(Operator.AND);
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append(new GeneralSearchQueryPart(query));
        }
        queryBuilder.append(new FieldQueryPart<String>(
                QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED.name()));

        QueryPartGroup qpg = new QueryPartGroup(Operator.OR);
        qpg.append(new FieldQueryPart<String>(QueryFieldNames.COLLECTION_VISIBLE, "true"));
        if (Persistable.Base.isNotNullOrTransient(getAuthenticatedUser())) {
            // if we're a "real user" and not an administrator -- make sure the user has view rights to things in the collection
            if (!getAuthenticationAndAuthorizationService().can(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
                qpg.append(new FieldQueryPart<Long>(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, getAuthenticatedUser().getId()));
            } else {
                qpg.clear();
            }
        }

        queryBuilder.append(qpg);

        try {
            logger.trace("queryBuilder: {}", queryBuilder);
            getSearchService().handleSearch(queryBuilder, this);

        } catch (ParseException e) {
            logger.warn("search parse exception: {}", e.getMessage());
            addActionErrorWithException(ERROR_PARSING_FAILED, e);
        }

        if (getActionErrors().isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    @Action(value = "rss", results = { @Result(name = "success", type = "stream", params = {
            "documentName", "rssFeed", "formatOutput", "true", "inputName",
            "inputStream", "contentType", "application/rss+xml",
            "contentLength", "${contentLength}", "contentEncoding", "UTF-8" }) })
    public String viewRss() {
        try {
            setSortField(SortOption.ID_REVERSE);
            setSecondarySortField(SortOption.TITLE);
            setMode("rss");
            search();
            setSearchTitle(getSearchSubtitle());
            setSearchDescription(StringEscapeUtils.escapeXml(getSearchPhrase()));
            setInputStream(rssService.createRssFeedFromResourceList(
                    getSessionData().getPerson(), this, getRecordsPerPage(),
                    getStartRecord(), getTotalRecords(), getRssUrl()));
        } catch (Exception e) {
            logger.error("rss error", e);
            addActionErrorWithException("could not process your search", e);
        }
        return SUCCESS;
    }

    /**
     * There are certain types of requests that require special processing
     * before we execute our search. For example: results?id=5
     * results?resourceTypes=DOCUMENT
     * results?query=olsend+standard+book+of+british+birds
     * 
     * For these requests we translate the provided arguments into a data
     * structure that can be understood by "new search".
     * 
     * @return
     */
    // FIXME: processLegacySearch is an inaccurate name. processSimpleApiSearch
    // is more like it.
    private boolean processLegacySearch() {
        // assumption: it's okay to wipe out the groups[] if we detect a legacy
        // request, and that you can't combine two different types (for
        // example: an id search combined with a uncontrolledCultureKeyword
        // search on the same querystring)

        // legacy search by id?
        if (getId() != null) {
            logger.trace("legacy api:  tdar id");
            groups.clear();
            groups.add(new SearchParameters());
            groups.get(0).getResourceIds().add(getId());
            getResourceTypes().clear();
            return true;
        }

        // legacy search by keyword
        // at the time of this writing the view layer only created links for
        // culture, site type, and siteName keywords. everything else
        // was rendered as a ?query= search
        if (!getSiteNameKeywords().isEmpty()
                || !getUncontrolledCultureKeywords().isEmpty()
                || !getUncontrolledSiteTypeKeywords().isEmpty()
                || !getGeographicKeywords().isEmpty()) {
            logger.trace("legacy api: uncontrolled keyword");
            groups.clear();
            groups.add(legacySearchParameters);
            return true;
        }

        return false;
    }

    private String advancedSearch() {
        determineSearchTitle();
        setMode("SEARCH");
        // beforeSearch();
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);

        topLevelQueryPart = new QueryPartGroup(topLevelOperator);

        for (SearchParameters group : groups) {
            group.setExplore(explore);
            topLevelQueryPart.append(group.toQueryPartGroup());
        }
        queryBuilder.append(topLevelQueryPart);

        reservedQueryPart = processReservedTerms();
        queryBuilder.append(reservedQueryPart);

        try {
            logger.trace("queryBuilder: {}", queryBuilder);
            getSearchService().handleSearch(queryBuilder, this);

        } catch (ParseException e) {
            logger.warn("search parse exception: {}", e.getMessage());
            addActionError(ERROR_PARSING_FAILED);
        }

        if (getActionErrors().isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }

    }

    private String basicSearch() {
        // translate basic search field(s) so that they can be processed by
        // advancedSearch()
        SearchParameters terms = new SearchParameters();
        terms.setOperator(Operator.AND);
        terms.getAllFields().add(query);
        terms.getFieldTypes().add(SearchFieldType.ALL_FIELDS);
        groups.add(terms);
        return advancedSearch();
    }

    public LatitudeLongitudeBox getMap() {
        if (CollectionUtils.isNotEmpty(getReservedSearchParameters()
                .getLatitudeLongitudeBoxes())) {
            return getReservedSearchParameters().getLatitudeLongitudeBoxes().get(0);
        }
        return null;
    }

    public void setMap(LatitudeLongitudeBox box) {
        getReservedSearchParameters().getLatitudeLongitudeBoxes().clear();
        getReservedSearchParameters().getLatitudeLongitudeBoxes().add(box);
    }

    @Actions({
            @Action(value = "basic", results = { @Result(name = SUCCESS, location = "advanced.ftl") }),
            @Action(value = "advanced") })
    public String execute() {
        return SUCCESS;
    }

    public List<ResourceCreatorRole> getAllResourceCreatorRoles() {
        ArrayList<ResourceCreatorRole> roles = new ArrayList<ResourceCreatorRole>();
        roles.addAll(ResourceCreatorRole.getAll());
        roles.addAll(ResourceCreatorRole.getOtherRoles());
        return roles;
    }

    public List<SortOption> getSortOptions() {
        sortOptions.remove(SortOption.RESOURCE_TYPE);
        sortOptions.remove(SortOption.RESOURCE_TYPE_REVERSE);
        return sortOptions;
    }

    public void setSortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public List<ResourceCreatorRole> getRelevantPersonRoles() {
        return getRelevantRoles(CreatorType.PERSON);
    }

    public List<ResourceCreatorRole> getRelevantInstitutionRoles() {
        return getRelevantRoles(CreatorType.INSTITUTION);
    }

    private List<ResourceCreatorRole> getRelevantRoles(CreatorType creatorType) {
        List<ResourceCreatorRole> relevantRoles = new ArrayList<ResourceCreatorRole>();
        relevantRoles.addAll(ResourceCreatorRole.getRoles(creatorType));
        relevantRoles.addAll(getRelevantOtherRoles(creatorType));
        return relevantRoles;
    }

    private List<ResourceCreatorRole> getRelevantOtherRoles(CreatorType creatorType) {
        if (creatorType == CreatorType.INSTITUTION) {
            return Arrays.asList(ResourceCreatorRole.RESOURCE_PROVIDER);
        } else if (creatorType == CreatorType.PERSON) {
            return Arrays.asList(ResourceCreatorRole.SUBMITTER, ResourceCreatorRole.UPDATER);
        }
        return Collections.emptyList();
    }

    public List<InvestigationType> getAllInvestigationTypes() {
        return getGenericKeywordService().findAllWithCache(
                InvestigationType.class);
    }

    public KeywordNode<CultureKeyword> getAllApprovedCultureKeywords() {
        return KeywordNode.organizeKeywords(getGenericKeywordService()
                .findAllApprovedWithCache(CultureKeyword.class));
    }

    public KeywordNode<SiteTypeKeyword> getAllApprovedSiteTypeKeywords() {
        return KeywordNode.organizeKeywords(getGenericKeywordService()
                .findAllApprovedWithCache(SiteTypeKeyword.class));
    }

    List<MaterialKeyword> allMaterialKeywords;

    private Keyword exploreKeyword;

    public List<MaterialKeyword> getAllMaterialKeywords() {

        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getGenericKeywordService().findAllWithCache(
                    MaterialKeyword.class);
            Collections.sort(allMaterialKeywords);
        }
        return allMaterialKeywords;
    }

    public List<SearchParameters> getGroups() {
        return groups;
    }

    public List<SearchParameters> getG() {
        return groups;
    }

    public String getRssUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        if (getServletRequest() != null)
            urlBuilder.append(getUrlService().getBaseUrl())
                    .append(getServletRequest().getContextPath())
                    .append("/search/rss").append("?")
                    .append(getServletRequest().getQueryString());
        return urlBuilder.toString();

    }

    public Integer getMaxDownloadRecords() {
        return TdarConfiguration.getInstance().getSearchExcelExportRecordMax();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchSubtitle() {
        return getSearchTitle();
    }

    // TODO: support multiple groups

    public String getSearchPhrase() {
        StringBuilder sb = new StringBuilder();
        if (groups.isEmpty()) {
            sb.append("Showing all resources");
        } else {
            sb.append("Searching for: ");
            String searchingFor = topLevelQueryPart.getDescription();
            sb.append(searchingFor);
        }
        // THIS SHOULD BE LESS BRITTLE THAN CALLING isEmpty()
        String narrowedBy = reservedQueryPart.getDescription();
        if (narrowedBy != null && StringUtils.isNotBlank(narrowedBy.trim())) {
            sb.append("  Narrowed by: ");
            sb.append(narrowedBy);
        }
        return sb.toString();
    }

    public String getSearchPhraseHtml() {
        return StringEscapeUtils.escapeHtml4(getSearchPhrase());
    }

    public void setRawQuery(String rawQuery) {
        throw new NotImplementedException(
                "admin lucene queries not implemented");
    }

    public List<ResourceType> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public List<IntegratableOptions> getIntegratableOptionFacets() {
        return integratableOptionFacets;
    }

    public List<DocumentType> getDocumentTypeFacets() {
        return documentTypeFacets;
    }

    public List<ResourceAccessType> getFileAccessFacets() {
        return fileAccessFacets;
    }

    private <C extends Enum<C> & Facetable> FacetingRequest facetOn(
            String name, String field, FullTextQuery ftq, List<C> facetList,
            Class<C> enumClass) {
        FacetingRequest facetRequest = getSearchService()
                .getQueryBuilder(Resource.class).facet().name(name)
                .onField(field).discrete().orderedBy(FacetSortOrder.COUNT_DESC)
                .includeZeroCounts(false).createFacetingRequest();

        ftq.getFacetManager().enableFaceting(facetRequest);
        for (Facet facet : ftq.getFacetManager().getFacets(name)) {
            C enumVersion = (C) Enum.valueOf(enumClass, facet.getValue());
            enumVersion.setCount(facet.getCount());
            facetList.add(enumVersion);
        }

        return facetRequest;
    }

    @Override
    public void addFacets(FullTextQuery ftq) {
        facetOn(QueryFieldNames.RESOURCE_TYPE, QueryFieldNames.RESOURCE_TYPE,
                ftq, resourceTypeFacets, ResourceType.class);
        facetOn(QueryFieldNames.INTEGRATABLE, QueryFieldNames.INTEGRATABLE,
                ftq, integratableOptionFacets, IntegratableOptions.class);
        facetOn(QueryFieldNames.DOCUMENT_TYPE, QueryFieldNames.DOCUMENT_TYPE,
                ftq, documentTypeFacets, DocumentType.class);
        facetOn(QueryFieldNames.RESOURCE_ACCESS_TYPE,
                QueryFieldNames.RESOURCE_ACCESS_TYPE, ftq, fileAccessFacets,
                ResourceAccessType.class);
    }

    // alias for faceted search.
    public void setDocumentType(DocumentType doctype) {
        if (doctype == null)
            return;
        getReservedSearchParameters().getDocumentTypes().clear();
        getReservedSearchParameters().getDocumentTypes().add(doctype);
    }

    // legacy keyword lookup support
    public void setSiteNameKeywords(List<String> kwds) {
        legacySearchParameters.setSiteNames(kwds);
    }

    // legacy keyword lookup support
    public void setUncontrolledSiteTypeKeywords(List<String> kwds) {
        legacySearchParameters.setUncontrolledSiteTypes(kwds);
    }

    // legacy keyword lookup support
    public void setUncontrolledCultureKeywords(List<String> kwds) {
        legacySearchParameters.setUncontrolledCultureKeywords(kwds);
    }

    // setter's are required here
    public void setGeographicKeywords(List<String> kwds) {
        legacySearchParameters.setGeographicKeywords(kwds);
    }

    public DocumentType getDocumentType() {
        if (getReservedSearchParameters().getDocumentTypes().size() > 0) {
            return getReservedSearchParameters().getDocumentTypes().get(0);
        }
        return null;
    }

    // alias for faceted search.
    public void setFileAccess(ResourceAccessType fileAccess) {
        if (fileAccess == null)
            return;
        getReservedSearchParameters().getResourceAccessTypes().clear();
        getReservedSearchParameters().getResourceAccessTypes().add(fileAccess);
    }

    public ResourceAccessType getFileAccess() {
        if (getReservedSearchParameters().getResourceAccessTypes().size() > 0) {
            return getReservedSearchParameters().getResourceAccessTypes().get(0);
        }
        return null;
    }

    public List<SearchFieldType> getAllSearchFieldTypes() {
        return allSearchFieldTypes;
    }

    private void determineSearchTitle() {
        setSearchTitle(TITLE_ALL_RECORDS); // if all else fails.
        if (getId() != null) {
            setSearchTitle(TITLE_BY_TDAR_ID); // accurate
        } else if (StringUtils.isNotBlank(getQuery())) {
            setSearchTitle(getQuery());
        } else if (isExplore()) {
            // FIXME -- Why can't we delegate this to the searchParameter object?
            if (getExploreKeyword() != null) {
                setSearchTitle(String.format("%s %s", TITLE_FILTERED_BY_KEYWORD, getExploreKeyword().getLabel()));
            } else if (StringUtils.isNotBlank(getFirstGroup().getStartingLetter())) {
                setSearchTitle(String.format("Title Beginning with %s",
                        getFirstGroup().getStartingLetter()));
                // FIXME: only supports 1
            } else if (CollectionUtils.isNotEmpty(getFirstGroup().getCreationDecades())) {
                setSearchTitle(String.format("Created in the Decade: %s",
                        getFirstGroup().getCreationDecades().get(0)));
            }
        } else if (isKeywordSearch()) {
            setSearchTitle(TITLE_FILTERED_BY_KEYWORD);
        }
    }

    private void determineCollectionSearchTitle() {
        if (StringUtils.isEmpty(query)) {
            setSearchTitle(TITLE_ALL_COLLECTIONS);
        } else {
            setSearchTitle(query);
        }
    }

    private boolean isKeywordSearch() {
        // FIXME: not always false...
        return false;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    // legacy keyword lookup support
    public List<String> getSiteNameKeywords() {
        return legacySearchParameters.getSiteNames();
    }

    // legacy keyword lookup support
    public List<String> getUncontrolledSiteTypeKeywords() {
        return legacySearchParameters.getUncontrolledSiteTypes();
    }

    // legacy keyword lookup support
    public List<String> getUncontrolledCultureKeywords() {
        return legacySearchParameters.getUncontrolledCultureKeywords();
    }

    // legach keyword lookup support
    public List<String> getGeographicKeywords() {
        return legacySearchParameters.getGeographicKeywords();
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getProjections() {
        return ListUtils.EMPTY_LIST;
    }

    // 'explore' is a more tailored/guided search experience. for example if
    // searching by keyword then we want to look up the definition of a keyword and
    // display it on the search results page.
    private void processExploreRequest() {
        if (groups.isEmpty())
            return;
        SearchParameters firstGroup = getFirstGroup();

        // was it a keyword lookup? if so show the definition of the keyword
        if (CollectionUtils.isNotEmpty(firstGroup.getInvestigationTypeIdLists())) {
            setExploreKeyword(InvestigationType.class, firstGroup.getInvestigationTypeIdLists());
        } else if (CollectionUtils.isNotEmpty(firstGroup.getApprovedSiteTypeIdLists())) {
            setExploreKeyword(SiteTypeKeyword.class, firstGroup.getApprovedSiteTypeIdLists());
        } else if (CollectionUtils.isNotEmpty(firstGroup.getApprovedCultureKeywordIdLists())) {
            setExploreKeyword(CultureKeyword.class, firstGroup.getApprovedCultureKeywordIdLists());
        } else if (CollectionUtils.isNotEmpty(firstGroup.getMaterialKeywordIdLists())) {
            setExploreKeyword(MaterialKeyword.class, firstGroup.getMaterialKeywordIdLists());
        }

    }

    private SearchParameters getFirstGroup() {
        return groups.get(0);
    }

    private <K extends Keyword> void setExploreKeyword(Class<K> type,
            List<List<String>> listOfLists) {
        Long id = Long.valueOf(listOfLists.get(0).get(0));
        exploreKeyword = getGenericService().find(type, id);
    }

    public boolean isExplore() {
        return explore;
    }

    public void setExplore(boolean explore) {
        this.explore = explore;
    }

    public Keyword getExploreKeyword() {
        return exploreKeyword;
    }

    @Action(value = "download", results = { @Result(name = "success", type = "stream", params = {
            "contentType", "application/vnd.ms-excel", "inputName",
            "inputStream", "contentDisposition",
            "attachment;filename=\"report.xls", "contentLength",
            "${contentLength}" }) })
    public String viewExcelReport() throws ParseException {
        try {
            if (!isAuthenticated())
                return UNAUTHORIZED;
            setMode("excel");
            setRecordsPerPage(200);
            search();
            int rowNum = 0;
            int maxRow = getMaxDownloadRecords();
            if (maxRow > getTotalRecords()) {
                maxRow = getTotalRecords();
            }
            if (getTotalRecords() > 0) {
                Sheet sheet = excelService.createWorkbook("results");

                List<String> fieldNames = new ArrayList<String>(Arrays.asList(
                        "ID", "ResourceType", "Title", "Date", "Authors",
                        "Project", "Description", "Number Of Files", "URL",
                        "Physical Location"));

                if (isEditor()) {
                    fieldNames.add("Status");
                    fieldNames.add("Date Added");
                    fieldNames.add("Submitted By");
                    fieldNames.add("Date Last Updated");
                    fieldNames.add("Updated By");
                }

                // ADD HEADER ROW THAT SHOWS URL and SEARCH PHRASE
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,
                        fieldNames.size()));
                excelService.addDocumentHeaderRow(
                        sheet,
                        rowNum,
                        0,
                        Arrays.asList("tDAR Search Results: "
                                + getSearchPhrase()));
                rowNum++;
                excelService
                        .addPairedHeaderRow(sheet, rowNum, 0, Arrays.asList(
                                "Search Url: ",
                                getUrlService().getBaseUrl()
                                        + getServletRequest().getRequestURI()
                                                .replace("/download",
                                                        "/results") + "?"
                                        + getServletRequest().getQueryString()));
                rowNum++;
                excelService.addPairedHeaderRow(
                        sheet,
                        rowNum,
                        0,
                        Arrays.asList("Downloaded by: ", getAuthenticatedUser()
                                .getProperName() + " on " + new Date()));
                rowNum++;
                rowNum++;
                excelService.addHeaderRow(sheet, rowNum, 0, fieldNames);
                int startRecord = 0;
                int currentRecord = 0;
                while (currentRecord < maxRow) {
                    startRecord = getNextPageStartRecord();
                    setStartRecord(getNextPageStartRecord()); // resetting for
                                                              // next search
                    for (Resource result : getResults()) {
                        rowNum++;
                        if (currentRecord++ > maxRow)
                            break;
                        Resource r = (Resource) result;
                        Integer dateCreated = null;
                        Integer numFiles = 0;
                        if (result instanceof InformationResource) {
                            dateCreated = ((InformationResource) result)
                                    .getDate();
                            numFiles = ((InformationResource) result)
                                    .getTotalNumberOfFiles();
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
                        ArrayList<Object> data = new ArrayList<Object>(
                                Arrays.asList(r.getId(), r.getResourceType(),
                                        r.getTitle(), dateCreated, authors,
                                        projectName,
                                        r.getShortenedDescription(), numFiles,
                                        getUrlService().absoluteUrl(r),
                                        location));

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
                        search();
                    }
                }

                excelService.setColumnWidth(sheet, 0, 5000);

                File tempFile = File.createTempFile("results", ".xls");
                FileOutputStream fos = new FileOutputStream(tempFile);
                sheet.getWorkbook().write(fos);
                fos.close();
                setInputStream(new FileInputStream(tempFile));
                contentLength = tempFile.length();
            }
        } catch (Exception e) {
            addActionErrorWithException("something happened with excel export",
                    e);
            return INPUT;
        }

        return SUCCESS;
    }

    public Long getContentLength() {
        return contentLength;
    }

}
