package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.search.SearchParameters;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralCreatorQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

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
@HttpOnlyIfUnauthenticated
public class AdvancedSearchController extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -2615014247540428072L;
    private static final String SEARCH_RSS = "/search/rss";
    private boolean hideFacetsAndSort = false;

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient ExcelService excelService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient UrlService urlService;

    private DisplayOrientation orientation;
    // error message of last resort. User entered something we did not
    // anticipate, and we ultimately translated it into query that lucene can't
    // parse

    private List<SearchFieldType> allSearchFieldTypes = SearchFieldType.getSearchFieldTypesByGroup();
    // basic searches go in "query"

    private List<ResourceCollection> collectionResults = new ArrayList<>();
    private int collectionTotalRecords = 0;

    // facet statistics for results.ftl
    private ArrayList<FacetValue> resourceTypeFacets = new ArrayList<>();
    private ArrayList<FacetValue> documentTypeFacets = new ArrayList<>();
    private ArrayList<FacetValue> fileAccessFacets = new ArrayList<>();
    private ArrayList<FacetValue> integratableOptionFacets = new ArrayList<>();
    private boolean collectionSearchBoxVisible = false;

    @Action(value = "results", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String search() throws TdarActionException {
        String result = performResourceSearch();
        searchCollectionsToo();
        return result;
    }

    @SuppressWarnings("unchecked")
    private void searchCollectionsToo() {
        QueryBuilder queryBuilder = new ResourceCollectionQueryBuilder();
        buildResourceCollectionQuery(queryBuilder);

        try {
            getLogger().trace("queryBuilder: {}", queryBuilder);
            SearchResult result = new SearchResult();
            result.setSortField(getSortField());
            result.setSecondarySortField(getSecondarySortField());
            result.setAuthenticatedUser(getAuthenticatedUser());
            result.setStartRecord(0);
            result.setRecordsPerPage(10);

            result.setMode("COLLECTION MINI");
            result.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
            searchService.handleSearch(queryBuilder, result, this);
            setMode("SEARCH");
            setCollectionResults((List<ResourceCollection>) (List<?>) result.getResults());
            getCollectionResults().removeAll(Collections.singleton(null));
            for (ResourceCollection col : getCollectionResults()) {
                authorizationService.applyTransientViewableFlag(col, getAuthenticatedUser());
            }
            setCollectionTotalRecords(result.getTotalRecords());
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception: {}", tdre.getMessage());
            addActionError(tdre.getMessage());
        } catch (ParseException e) {
            getLogger().warn("search parse exception: {}", e.getMessage());
            addActionErrorWithException(getText("advancedSearchController.error_parsing_failed"), e);
        }
    }

    @Action(value = "collections", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String searchCollections() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(ResourceCollection.class));
        try {
            return collectionSearch();
        } catch (TdarRecoverableRuntimeException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
    }

    @Action(value = "institutions", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String searchInstitutions() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(Institution.class));
        setMinLookupLength(0);
        setLookupSource(LookupSource.INSTITUTION);
        setMode("INSTITUTION");
        InstitutionQueryBuilder iqb = new InstitutionQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        group.append(new GeneralCreatorQueryPart(new Institution(getQuery())));
        iqb.append(group);
        try {
            handleSearch(iqb);
        } catch (TdarRecoverableRuntimeException | ParseException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    @Action(value = "people", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = "advanced.ftl") })
    public String searchPeople() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(Person.class));
        setMinLookupLength(0);
        setMode("PERSON");
        setLookupSource(LookupSource.PERSON);
        PersonQueryBuilder pqb = new PersonQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        Person person = Person.fromName(getQuery());
        group.append(new GeneralCreatorQueryPart(person));
        pqb.append(group);
        try {
            handleSearch(pqb);
        } catch (TdarRecoverableRuntimeException | ParseException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    private String collectionSearch() {
        setLookupSource(LookupSource.COLLECTION);
        setMode("COLLECTION SEARCH:");
        determineCollectionSearchTitle();
        QueryBuilder queryBuilder = new ResourceCollectionQueryBuilder();
        buildResourceCollectionQuery(queryBuilder);

        try {
            getLogger().trace("queryBuilder: {}", queryBuilder);
            searchService.handleSearch(queryBuilder, this, this);
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception: {}", tdre.getMessage());
            addActionError(tdre.getMessage());
        } catch (ParseException e) {
            getLogger().warn("search parse exception: {}", e.getMessage());
            addActionErrorWithException(getText("advancedSearchController.error_parsing_failed"), e);
        }

        if (getActionErrors().isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    @DoNotObfuscate(reason = "user submitted map")
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
            @Action(value = "collection", results = { @Result(name = SUCCESS, location = "advanced.ftl") }),
            @Action(value = "person", results = { @Result(name = SUCCESS, location = "advanced.ftl") }),
            @Action(value = "institution", results = { @Result(name = SUCCESS, location = "advanced.ftl") })
    })
    @Override
    public String execute() {
        return SUCCESS;
    }

    @Action(value = "advanced")
    public String advanced() {
        // process query paramter or legacy parameters, if present.
        processBasicSearchParameters();
        processLegacySearchParameters();

        // if refining a search, make sure we inflate any deflated terms
        for (SearchParameters sp : getGroups()) {
            searchService.inflateSearchParameters(sp);
            try {
                sp.toQueryPartGroup(null);
                getLogger().debug("inflating parameters for group {}", sp);
            } catch (TdarRecoverableRuntimeException trex) {
                addActionError(trex.getMessage());
            }
        }
        // in the situation where we could not reconstitute the search object graph, wipe out the search terms and show the error messages.
        if (hasActionErrors()) {
            getGroups().clear();
        }

        return SUCCESS;
    }

    public List<ResourceCreatorRole> getAllResourceCreatorRoles() {
        ArrayList<ResourceCreatorRole> roles = new ArrayList<ResourceCreatorRole>();
        roles.addAll(ResourceCreatorRole.getAll());
        roles.addAll(ResourceCreatorRole.getOtherRoles());
        return roles;
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
        return genericKeywordService.findAllWithCache(InvestigationType.class);
    }

    public KeywordNode<CultureKeyword> getAllApprovedCultureKeywords() {
        return KeywordNode.organizeKeywords(genericKeywordService.findAllApprovedWithCache(CultureKeyword.class));
    }

    public KeywordNode<SiteTypeKeyword> getAllApprovedSiteTypeKeywords() {
        return KeywordNode.organizeKeywords(genericKeywordService.findAllApprovedWithCache(SiteTypeKeyword.class));
    }

    List<MaterialKeyword> allMaterialKeywords;

    private Keyword exploreKeyword;

    public List<MaterialKeyword> getAllMaterialKeywords() {

        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = genericKeywordService.findAllWithCache(MaterialKeyword.class);
            Collections.sort(allMaterialKeywords);
        }
        return allMaterialKeywords;
    }

    public String getRssUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        if (getServletRequest() != null) {
            urlBuilder.append(UrlService.getBaseUrl())
                    .append(getServletRequest().getContextPath())
                    .append(SEARCH_RSS).append("?")
                    .append(getServletRequest().getQueryString());
        }
        return urlBuilder.toString();

    }

    public Integer getMaxDownloadRecords() {
        return TdarConfiguration.getInstance().getSearchExcelExportRecordMax();
    }

    public void setRawQuery(String rawQuery) {
        throw new NotImplementedException(getText("advancedSearchController.admin_not_implemented"));
    }

    public List<FacetValue> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public List<FacetValue> getIntegratableOptionFacets() {
        return integratableOptionFacets;
    }

    public List<FacetValue> getDocumentTypeFacets() {
        return documentTypeFacets;
    }

    public List<FacetValue> getFileAccessFacets() {
        return fileAccessFacets;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        List<FacetGroup<? extends Enum>> group = new ArrayList<>();
        // List<FacetGroup<?>> group = new ArrayList<FacetGroup<?>>();
        group.add(new FacetGroup<ResourceType>(ResourceType.class, QueryFieldNames.RESOURCE_TYPE, resourceTypeFacets, ResourceType.DOCUMENT));
        group.add(new FacetGroup<IntegratableOptions>(IntegratableOptions.class, QueryFieldNames.INTEGRATABLE, integratableOptionFacets,
                IntegratableOptions.YES));
        group.add(new FacetGroup<ResourceAccessType>(ResourceAccessType.class, QueryFieldNames.RESOURCE_ACCESS_TYPE, fileAccessFacets,
                ResourceAccessType.CITATION));
        group.add(new FacetGroup<DocumentType>(DocumentType.class, QueryFieldNames.DOCUMENT_TYPE, documentTypeFacets, DocumentType.BOOK));
        return group;
    }
//
//    // alias for faceted search.
//    public void setDocumentType(DocumentType doctype) {
//        if (doctype == null) {
//            return;
//        }
//        getReservedSearchParameters().getDocumentTypes().clear();
//        getReservedSearchParameters().getDocumentTypes().add(doctype);
//    }
//
//
//    // legacy keyword lookup support
//    public void setSiteNameKeywords(List<String> kwds) {
//        legacySearchParameters.setSiteNames(kwds);
//    }
//
//    // legacy keyword lookup support
//    public void setUncontrolledSiteTypeKeywords(List<String> kwds) {
//        legacySearchParameters.setUncontrolledSiteTypes(kwds);
//    }
//
//    // legacy keyword lookup support
//    public void setUncontrolledCultureKeywords(List<String> kwds) {
//        legacySearchParameters.setUncontrolledCultureKeywords(kwds);
//    }
//
//    // setter's are required here
//    public void setGeographicKeywords(List<String> kwds) {
//        legacySearchParameters.setGeographicKeywords(kwds);
//    }
//
//    public DocumentType getDocumentType() {
//        if (getReservedSearchParameters().getDocumentTypes().size() > 0) {
//            return getReservedSearchParameters().getDocumentTypes().get(0);
//        }
//        return null;
//    }
//
//    // alias for faceted search.
//    public void setFileAccess(ResourceAccessType fileAccess) {
//        if (fileAccess == null) {
//            return;
//        }
//        getReservedSearchParameters().getResourceAccessTypes().clear();
//        getReservedSearchParameters().getResourceAccessTypes().add(fileAccess);
//    }
//
//    public ResourceAccessType getFileAccess() {
//        if (getReservedSearchParameters().getResourceAccessTypes().size() > 0) {
//            return getReservedSearchParameters().getResourceAccessTypes().get(0);
//        }
//        return null;
//    }

    public List<SearchFieldType> getAllSearchFieldTypes() {
        return allSearchFieldTypes;
    }
//
//    // legacy keyword lookup support
//    public List<String> getSiteNameKeywords() {
//        return legacySearchParameters.getSiteNames();
//    }
//
//    // legacy keyword lookup support
//    public List<String> getUncontrolledSiteTypeKeywords() {
//        return legacySearchParameters.getUncontrolledSiteTypes();
//    }
//
//    // legacy keyword lookup support
//    public List<String> getUncontrolledCultureKeywords() {
//        return legacySearchParameters.getUncontrolledCultureKeywords();
//    }
//
//    // legach keyword lookup support
//    public List<String> getGeographicKeywords() {
//        return legacySearchParameters.getGeographicKeywords();
//    }
//
//    public String getLetter() {
//        return letter;
//    }
//
//    public void setLetter(String letter) {
//        this.letter = letter;
//    }
//
//    public boolean isExplore() {
//        return explore;
//    }
//
//    public void setExplore(boolean explore) {
//        this.explore = explore;
//    }

    public Keyword getExploreKeyword() {
        return exploreKeyword;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public boolean isHideFacetsAndSort() {
        return hideFacetsAndSort;
    }

    public void setHideFacetsAndSort(boolean hideFacetsAndSort) {
        this.hideFacetsAndSort = hideFacetsAndSort;
    }

//    public String getLatLongBox() {
//        return latLongBox;
//    }
//
//    public void setLatLongBox(String latLongBox) {
//        this.latLongBox = latLongBox;
//    }
//
//    public Long getProjectId() {
//        return projectId;
//    }
//
//    public void setProjectId(Long projectId) {
//        this.projectId = projectId;
//    }
//
//    public Long getCollectionId() {
//        return collectionId;
//    }
//
//    public void setCollectionId(Long collectionId) {
//        this.collectionId = collectionId;
//    }

//    /**
//     * Indicates whether current search is "contextual search", i.e. is the search implicitly filtered by
//     * project or filtered by collection.
//     * 
//     * @return
//     */
//    public boolean isContextualSearch() {
//        return (collectionId != null) || (projectId != null);
//    }

    public List<ResourceCollection> getCollectionResults() {
        return collectionResults;
    }

    public void setCollectionResults(List<ResourceCollection> collectionResults) {
        this.collectionResults = collectionResults;
    }

    public int getCollectionTotalRecords() {
        return collectionTotalRecords;
    }

    public void setCollectionTotalRecords(int collectionTotalRecords) {
        this.collectionTotalRecords = collectionTotalRecords;
    }

    /**
     * Hint to view layer: true if it should display collection search results along with resource search results.
     * 
     * @return true, if rendering search results, the list of results is not empty, the collection search box is visible
     */
    public boolean isShowCollectionResults() {
        return getLookupSource() == LookupSource.RESOURCE
                && collectionSearchBoxVisible
                && collectionTotalRecords > 0;
    }

}
