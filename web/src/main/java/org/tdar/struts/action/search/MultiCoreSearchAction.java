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
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
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
public class MultiCoreSearchAction extends AbstractAdvancedSearchController implements FacetedResultHandler<Resource> {

    private static final String SIMPLE_FTL = "simple.ftl";
    private static final long serialVersionUID = -7767557393006858614L;
    private static final String SEARCH_RSS = "/search/rss";
    private FacetWrapper facetWrapper = new FacetWrapper();

    @Autowired
    private transient ResourceSearchService resourceSearchService;

    @Autowired
    private transient AuthorizationService authorizationService;

    private DisplayOrientation orientation;

    private boolean showLeftSidebar = true;

    @Override
    public boolean isLeftSidebar() {
        return showLeftSidebar;
    }

    @Action(value = "multi", results = {
            @Result(name = SUCCESS, location = "multi.ftl"),
            @Result(name = INPUT, location = SIMPLE_FTL) })
    public String search() throws TdarActionException {
        String result = SUCCESS;
        // FIME: for whatever reason this is not being processed by the SessionSecurityInterceptor and thus
        // needs manual care, but, when the TdarActionException is processed, it returns a blank page instead of
        // not_found
        
        	setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        
        getAsqo().setMultiCore(true);
        try {
            getFacetWrapper().facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class);
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

    @Actions({
            @Action(value = "simple", results = { @Result(name = SUCCESS, location = SIMPLE_FTL) }),
    })
    @Override
    public String execute() {
        searchBoxVisible = false;
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

    private boolean searchBoxVisible = true;

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

    @Override
    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
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
