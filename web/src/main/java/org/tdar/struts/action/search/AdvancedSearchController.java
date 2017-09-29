package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
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
import org.tdar.search.bean.ObjectType;
import org.tdar.search.bean.PersonSearchOption;
import org.tdar.search.bean.SearchFieldType;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;

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
@HttpsOnly
public class AdvancedSearchController extends AbstractAdvancedSearchController implements FacetedResultHandler<Resource> {

    private static final long serialVersionUID = -7767557393006858614L;
    private static final String SEARCH_RSS = "/api/search/rss";
    private FacetWrapper facetWrapper = new FacetWrapper();
    private static final String ADVANCED_FTL = "advanced.ftl";
    private boolean hideFacetsAndSort = false;

    @Autowired
    private transient ResourceSearchService resourceSearchService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;
    @Autowired
    private transient GenericService genericService;
    
    @Autowired
    private transient AuthorizationService authorizationService;

    private DisplayOrientation orientation;
    private List<SearchFieldType> allSearchFieldTypes = SearchFieldType.getSearchFieldTypesByGroup();

    private boolean showLeftSidebar = true;
    
    private List<PersonSearchOption> personSearchOptions = Arrays.asList(PersonSearchOption.values());

    @Override
    public boolean isLeftSidebar() {
        return showLeftSidebar;
    }

    @Action(value = "results", results = {
            @Result(name = SUCCESS, location = "results.ftl"),
            @Result(name = INPUT, location = ADVANCED_FTL)
            })
    public String search() throws TdarActionException {
        String result = SUCCESS;
        // FIME: for whatever reason this is not being processed by the SessionSecurityInterceptor and thus
        // needs manual care, but, when the TdarActionException is processed, it returns a blank page instead of
        // not_found


        //FIXME jtd: This is a workaround for a (possible) bug in Struts that inserts null into lists if a request querystring has parameter name w/o a value.
        // Normally we would expect struts to set such properties to be empty lists, so we make sure this is the case by stripping null entries.
        // Note that sometimes null entries are important placeholders, so don't do this *everywhere*, just in lists where nulls are never expected.
        stripNulls(getIntegratableOptions(), getDocumentTypeFacets(), getResourceTypeFacets(), getObjectTypeFacets(), getObjectTypes(), getResourceTypes());

        setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);

        getAsqo().setMultiCore(true);
        try {
            getFacetWrapper().facetBy(QueryFieldNames.OBJECT_TYPE, ObjectType.class);
            getFacetWrapper().facetBy(QueryFieldNames.COLLECTION_TYPE, CollectionType.class);
            getFacetWrapper().facetBy(QueryFieldNames.GENERAL_TYPE, LookupSource.class);
            getFacetWrapper().facetBy(QueryFieldNames.INTEGRATABLE, IntegratableOptions.class);
            getFacetWrapper().facetBy(QueryFieldNames.RESOURCE_ACCESS_TYPE, ResourceAccessType.class);
            getFacetWrapper().facetBy(QueryFieldNames.DOCUMENT_TYPE, DocumentType.class);

            result = performResourceSearch();
            getLogger().trace(result);

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
        processCollectionProjectLimit();
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
        ResourceCollection rc = getGenericService().find(ResourceCollection.class, getCollectionId());
        if (rc == null) {
            return;
        }

        SearchParameters sp = new SearchParameters();
        if (getGroups() != null) {
            getGroups().clear();
        }
        getGroups().add(sp);
        sp.getFieldTypes().addAll(Arrays.asList(SearchFieldType.COLLECTION, SearchFieldType.ALL_FIELDS));
        sp.getCollections().addAll(Arrays.asList((ResourceCollection)rc, null));
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
            return Arrays.asList(ResourceCreatorRole.SUBMITTER, ResourceCreatorRole.UPDATER); //, ResourceCreatorRole.UPLOADER (add after reindex)
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

    public List<Facet> getObjectTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.OBJECT_TYPE);
    }

    public List<Facet> getTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.GENERAL_TYPE);
    }

    public List<Facet> getCollectionTypeFacets() {
        return getFacetWrapper().getFacetResults().get(QueryFieldNames.COLLECTION_TYPE);
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

    public List<LookupSource> getTypes() {
        return getReservedSearchParameters().getTypes();
    }

    public void setTypes(List<LookupSource> types) {
        getReservedSearchParameters().setTypes(types);
    }

    public List<CollectionType> getCollectionTypes() {
        return getReservedSearchParameters().getCollectionTypes();
    }

    public void setCollectionTypes(List<CollectionType> types) {
        getReservedSearchParameters().setCollectionTypes(types);
    }

    public List<LookupSource> getAvailableTypes() {
        return Arrays.asList(LookupSource.COLLECTION, LookupSource.INTEGRATION, LookupSource.RESOURCE);
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


    @Override
    protected void updateDisplayOrientationBasedOnSearchResults() {
        if (orientation != null) {
            getLogger().trace("orientation is set to: {}", orientation);
            return;
        }

        if (CollectionUtils.isNotEmpty(getObjectTypeFacets())) {
            boolean allImages = true;
            for (Facet val : getObjectTypeFacets()) {
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

    public List<DisplayOrientation> getAvailableOrientations() {
        List<String> keys = getGeneralTypes();

        if (keys.size() == 1) {
            return DisplayOrientation.getOrientationsFor(keys.get(0));
        }

        if (keys.size() == 2) {
            return DisplayOrientation.getCommonOrientations();
        }
        
        return new ArrayList<>();
    }

    private List<String> getGeneralTypes() {
        List<String> keys = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(getTypeFacets())) {
            getTypeFacets().forEach(facet -> keys.add(facet.getRaw()));
        }
        return keys;
    }

    public List<SearchFieldType> getAllSearchFieldTypes() {
        return allSearchFieldTypes;
    }
    
    public Keyword getExploreKeyword() {
        return exploreKeyword;
    }
    
    public List<PersonSearchOption> getPersonSearchOptions(){
    	return this.personSearchOptions;
    }
    
    public void setPersonSearchOptions(List<PersonSearchOption> options){
    	this.personSearchOptions = options;
    }
}
