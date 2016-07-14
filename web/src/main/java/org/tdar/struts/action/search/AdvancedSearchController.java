package org.tdar.struts.action.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.bean.SearchFieldType;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

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
public class AdvancedSearchController extends AbstractAdvancedSearchController implements FacetedResultHandler<Resource> {

    private static final String ADVANCED_FTL = "advanced.ftl";
    private static final long serialVersionUID = -2615014247540428072L;
    private static final String SEARCH_RSS = "/search/rss";
    private boolean hideFacetsAndSort = false;
    private FacetWrapper facetWrapper = new FacetWrapper();

    @Autowired
    private transient CollectionSearchService collectionSearchService;
    
    @Autowired
    private transient ResourceSearchService resourceSearchService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;
    @Autowired
    private transient GenericService genericService;

    private DisplayOrientation orientation;

    private List<SearchFieldType> allSearchFieldTypes = SearchFieldType.getSearchFieldTypesByGroup();

    private List<ResourceCollection> collectionResults = new ArrayList<>();
    private int collectionTotalRecords = 0;
    private boolean showLeftSidebar = false;

    @Override
    public boolean isLeftSidebar() {
        return showLeftSidebar;
    }

    @Action(value = "results", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = ADVANCED_FTL) })
    public String search() throws TdarActionException {
        String result = SUCCESS;
        // FIME: for whatever reason this is not being processed by the SessionSecurityInterceptor and thus
        // needs manual care, but, when the TdarActionException is processed, it returns a blank page instead of
        // not_found
        
        if (getProjectionModel() == null) {
            setProjectionModel(ProjectionModel.LUCENE_EXPERIMENTAL);
        }
        
        try {
            getFacetWrapper().facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class);
            getFacetWrapper().facetBy(QueryFieldNames.INTEGRATABLE, IntegratableOptions.class);
            getFacetWrapper().facetBy(QueryFieldNames.RESOURCE_ACCESS_TYPE, ResourceAccessType.class);
            getFacetWrapper().facetBy(QueryFieldNames.DOCUMENT_TYPE, DocumentType.class);

            result = performResourceSearch();
            getLogger().trace(result);
            if (SUCCESS.equals(result)) {
                searchCollectionsToo();
            }
        } catch (TdarActionException e) {
            getLogger().debug("exception: {}|{}", e.getResponse(), e.getResponseStatusCode(), e);
            if (e.getResponseStatusCode() != StatusCode.NOT_FOUND) {
                addActionErrorWithException(e.getMessage(), e);
            }
            if (e.getResponse() == null) {
                result = INPUT;
            } else {
                return e.getResponse();
            }
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
            result = INPUT;
        }
        showLeftSidebar = true;
        return result;
    }

    private void searchCollectionsToo() throws SolrServerException, IOException {

        try {
            SearchResult<ResourceCollection> result = new SearchResult<>();
            result.setSortField(SortOption.RELEVANCE);
            result.setSecondarySortField(getSecondarySortField());
            result.setAuthenticatedUser(getAuthenticatedUser());
            result.setStartRecord(0);
            result.setRecordsPerPage(10);

            result.setMode("COLLECTION MINI");
            result.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
            CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
            csqo.setAllFields(getAllGeneralQueryFields());
            collectionSearchService.buildResourceCollectionQuery(getAuthenticatedUser(), csqo,  result, this);
            setMode("SEARCH");
            getCollectionResults().addAll(result.getResults());
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
    
    
    @Action(value = "map", results = { @Result(name = SUCCESS, location = "map.ftl") })
    @RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
    public String map() {
        searchBoxVisible = false;
        return SUCCESS;
    }

    @Actions({
            @Action(value = "basic", results = { @Result(name = SUCCESS, location = ADVANCED_FTL) }),
            @Action(value = "collection", results = { @Result(name = SUCCESS, location = "collection.ftl") }),
            @Action(value = "person", results = { @Result(name = SUCCESS, location = "person.ftl") }),
            @Action(value = "institution", results = { @Result(name = SUCCESS, location = "institution.ftl") })
    })
    @Override
    public String execute() {
        searchBoxVisible = false;
        return SUCCESS;
    }

    @Action(value = "advanced")
    public String advanced() {
        getLogger().trace("greetings from advanced search");
        // process query paramter or legacy parameters, if present.
        processBasicSearchParameters();
        processLegacySearchParameters();
        processWhitelabelSearch();

        // if refining a search, make sure we inflate any deflated terms
        for (SearchParameters sp : getGroups()) {
            resourceSearchService.inflateSearchParameters(sp);
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
        getLogger().trace("advanced search done");
        return SUCCESS;
    }

    private void processWhitelabelSearch() {
        SharedCollection rc = getGenericService().find(SharedCollection.class, getCollectionId());
        if (rc == null) {
            return;
        }

        SearchParameters sp = new SearchParameters();
        if (getGroups() != null) {
            getGroups().clear();
        }
        getGroups().add(sp);
        sp.getFieldTypes().addAll(Arrays.asList(SearchFieldType.COLLECTION, SearchFieldType.ALL_FIELDS));
        sp.getCollections().addAll(Arrays.asList(rc, null));
        sp.getAllFields().addAll(Arrays.asList(null, ""));
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
        return genericService.findAll(InvestigationType.class);
    }

    public KeywordNode<CultureKeyword> getAllApprovedCultureKeywords() {
        return KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(CultureKeyword.class));
    }

    public KeywordNode<SiteTypeKeyword> getAllApprovedSiteTypeKeywords() {
        return KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(SiteTypeKeyword.class));
    }

    List<MaterialKeyword> allMaterialKeywords;

    private Keyword exploreKeyword;
    private boolean searchBoxVisible = true;

    public List<MaterialKeyword> getAllMaterialKeywords() {

        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = genericKeywordService.findAllApproved(MaterialKeyword.class);
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

    public List<Facet> getResourceTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_TYPE);
    }

    public List<Facet> getIntegratableOptionFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.INTEGRATABLE);
    }
    public List<Facet> getDocumentTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.DOCUMENT_TYPE);
    }

    public List<Facet> getFileAccessFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_ACCESS_TYPE);
    }

    public List<SearchFieldType> getAllSearchFieldTypes() {
        return allSearchFieldTypes;
    }

    public Keyword getExploreKeyword() {
        return exploreKeyword;
    }

    @Override
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
                && collectionTotalRecords > 0;
    }

    @Override
    protected void updateDisplayOrientationBasedOnSearchResults() {
        if (orientation != null) {
            getLogger().trace("orientation is set to: {}", orientation);
            return;
        }

        if (CollectionUtils.isNotEmpty(getResourceTypeFacets())) {
            boolean allImages = true;
            for (Facet val : getResourceTypeFacets()) {
                if (val.getCount() > 0 && !ResourceType.isImageName(val.getRaw())) {
                    allImages = false;
                }
            }
            // if we're only dealing with images, and an orientation has not been set
            if (allImages) {
                setOrientation(DisplayOrientation.GRID);
                getLogger().trace("switching to grid orientation");
                return;
            }
        }
        LatitudeLongitudeBox map = null;
        try {
            map = getG().get(0).getLatitudeLongitudeBoxes().get(0);
        } catch (Exception e) {
            // ignore
        }
        if (getMap() != null && getMap().isInitializedAndValid() || map != null && map.isInitializedAndValid()) {
            getLogger().trace("switching to map orientation");
            setOrientation(DisplayOrientation.MAP);
        }
    }

    @Override
    public List<Status> getAllStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    @Override
    public boolean isNavSearchBoxVisible() {
        return searchBoxVisible;
    }

    @Override
    public FacetWrapper getFacetWrapper() {
        return facetWrapper;
    }

    public void setFacetWrapper(FacetWrapper facetWrapper) {
        this.facetWrapper = facetWrapper;
    }

}
