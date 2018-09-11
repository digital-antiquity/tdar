package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts.action.SlugViewAction;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.transform.OpenUrlFormatter;
import org.tdar.utils.ResourceCitationFormatter;
import org.tdar.web.service.ResourceViewControllerService;

/**
 * $Id$
 * 
 * Provides basic metadata support for controllers that manage subtypes of
 * Resource.
 * 
 * Don't extend this class unless you need this metadata to be set.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/resource")
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, location = "../resource/view-template.ftl")
})
public abstract class AbstractResourceViewAction<R extends Resource> extends AbstractPersistableViewableAction<R> implements SlugViewAction {

    private static final long serialVersionUID = 896347341133309643L;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient UserRightsProxyService userRightsProxyService;

    @Autowired
    public ResourceCollectionService resourceCollectionService;

    @Autowired
    private BillingAccountService accountService;

    @Autowired
    private ResourceService resourceService;

    private List<ResourceCollection> effectiveShares = new ArrayList<>();
    private List<ResourceCollection> effectiveResourceCollections = new ArrayList<>();

    private List<ResourceCreatorProxy> authorshipProxies = new ArrayList<>();
    private List<ResourceCreatorProxy> creditProxies = new ArrayList<>();
    private List<ResourceCreatorProxy> contactProxies = new ArrayList<>();
    private ResourceCitationFormatter resourceCitation;

    private String schemaOrgJsonLD;

    private Map<DataTableColumn, String> mappedData;

    private List<UserInvite> invites;

    @Autowired
    ResourceViewControllerService viewService;

    private List<ResourceCollection> visibleUnmanagedCollections;

    public String getOpenUrl() {
        return OpenUrlFormatter.toOpenURL(getResource());
    }

    public String getGoogleScholarTags() throws Exception {
        return resourceService.getGoogleScholarTags(getResource());
    }

    @Override
    public String loadViewMetadata() throws TdarActionException {
        if (getResource() == null) {
            return ERROR;
        }
        setResourceCitation(new ResourceCitationFormatter(getResource()));
        setSchemaOrgJsonLD(resourceService.getSchemaOrgJsonLD(getResource()));
        loadBasicViewMetadata();
        loadCustomViewMetadata();
        AuthWrapper<Resource> authWrapper = new AuthWrapper<Resource>(getPersistable(), isAuthenticated(), getAuthenticatedUser(), isEditor());

        viewService.updateResourceInfo(authWrapper, isBot());
        if (isEditor()) {
            if (getPersistableClass().equals(Project.class)) {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatisticsForProject(getId(), null));
            } else {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(Arrays.asList(getId()), null));
            }
        }

        setInvites(userRightsProxyService.findUserInvites(getPersistable()));
        return SUCCESS;
    }

    protected void loadCustomViewMetadata() throws TdarActionException {
    }

    @Override
    public boolean authorize() throws TdarActionException {
        boolean result = authorizationService.isResourceViewable(getAuthenticatedUser(), getResource());
        if (result == false) {
            if (getResource() == null) {
                abort(StatusCode.UNKNOWN_ERROR, getText("abstractPersistableController.not_found"));
            }
            if (getResource().isDeleted()) {
                getLogger().debug("resource not viewable because it is deleted: {}", getResource());
                throw new TdarActionException(StatusCode.GONE, getText("abstractResourceController.resource_deleted"));
            }

            if (getResource().isDraft()) {
                getLogger().trace("resource not viewable because it is draft: {}", getResource());
                throw new TdarActionException(StatusCode.OK, DRAFT,
                        getText("abstractResourceController.this_record_is_in_draft_and_is_only_available_to_authorized_users"));
            }

        }
        return result;
    }

    public void loadBasicViewMetadata() {
        AuthWrapper<Resource> authWrapper = new AuthWrapper<Resource>(getPersistable(), isAuthenticated(), getAuthenticatedUser(), isEditor());
        viewService.initializeResourceCreatorProxyLists(authWrapper, authorshipProxies, creditProxies, contactProxies);
        viewService.loadSharesCollectionsAuthUsers(authWrapper, getEffectiveShares(), getEffectiveResourceCollections(), getAuthorizedUsers());
        getLogger().trace("effective collections: {}", getEffectiveResourceCollections());
        visibleCollections = viewService.getVisibleManagedCollections(authWrapper);
        visibleUnmanagedCollections = viewService.getVisibleUnmanagedCollections(authWrapper);
        if (getResource() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) getResource();
            setMappedData(resourceService.getMappedDataForInformationResource(informationResource, getTdarConfiguration().isProductionEnvironment()));
        }

    }

    public Resource getResource() {
        return getPersistable();
    }

    public void setResource(R resource) {
        getLogger().debug("setResource: {}", resource);
        setPersistable(resource);
    }

    public boolean isAbleToViewConfidentialFiles() {
        return authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getPersistable());
    }

    public List<ResourceCreatorRole> getAllResourceCreatorRoles() {
        return ResourceCreatorRole.getAll();
    }

    public Set<ResourceAnnotationKey> getAllResourceAnnotationKeys() {
        Set<ResourceAnnotationKey> keys = new HashSet<>();
        if ((getPersistable() != null) && CollectionUtils.isNotEmpty(getPersistable().getActiveResourceAnnotations())) {
            for (ResourceAnnotation ra : getPersistable().getActiveResourceAnnotations()) {
                keys.add(ra.getResourceAnnotationKey());
            }
        }
        return keys;
    }

    public List<ResourceCreatorProxy> getAuthorshipProxies() {
        if (CollectionUtils.isEmpty(authorshipProxies)) {
            authorshipProxies = new ArrayList<>();
        }
        return authorshipProxies;
    }

    public List<ResourceCreatorProxy> getContactProxies() {
        if (CollectionUtils.isEmpty(contactProxies)) {
            contactProxies = new ArrayList<>();
        }
        return contactProxies;
    }

    public void setAuthorshipProxies(List<ResourceCreatorProxy> authorshipProxies) {
        this.authorshipProxies = authorshipProxies;
    }

    public List<ResourceCreatorProxy> getCreditProxies() {
        if (CollectionUtils.isEmpty(creditProxies)) {
            creditProxies = new ArrayList<>();
        }
        return creditProxies;
    }

    private List<ResourceCollection> visibleCollections = new ArrayList<>();

    /**
     * All shares and list collections
     * 
     * @return
     */
    public List<ResourceCollection> getViewableResourceCollections() {
        return visibleCollections;
    }

    public boolean isUserAbleToReTranslate() {
        if (authorizationService.canEdit(getAuthenticatedUser(), getPersistable())) {
            return true;
        }
        return false;
    }

    public boolean isUserAbleToViewDeletedFiles() {
        return isEditor();
    }

    public boolean isUserAbleToViewUnobfuscatedMap() {
        return isEditor() && authorizationService.isMember(getAuthenticatedUser(), TdarGroup.TDAR_RPA_MEMBER);
    }

    private Boolean editable = null;

    public boolean isEditable() {
        if (isNullOrNew()) {
            return false;
        }
        if (editable == null) {
            editable = authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), Permissions.MODIFY_METADATA);
        }
        return editable;
    }

    public String getSchemaOrgJsonLD() {
        return schemaOrgJsonLD;
    }

    public void setSchemaOrgJsonLD(String schemaOrgJsonLD) {
        this.schemaOrgJsonLD = schemaOrgJsonLD;
    }

    private transient ResourceCollection whiteLabelCollection;

    @XmlTransient
    /**
     * We assume for now that a resource will only belong to a single white-label collection.
     *
     * @return
     */
    public ResourceCollection getWhiteLabelCollection() {
        if (whiteLabelCollection == null) {
            whiteLabelCollection = resourceCollectionService.getWhiteLabelCollectionForResource(getResource());
        }
        return whiteLabelCollection;
    }

    public boolean isWhiteLabelLogoAvailable() {
        ResourceCollection wlc = getWhiteLabelCollection();
        return wlc != null && checkLogoAvailable(FilestoreObjectType.COLLECTION, wlc.getId(), VersionType.WEB_LARGE);
    }

    public String getWhiteLabelLogoUrl() {
        return String.format("/files/collection/lg/%s/logo", getWhiteLabelCollection().getId());
    }

    public ResourceCitationFormatter getResourceCitation() {
        return resourceCitation;
    }

    public void setResourceCitation(ResourceCitationFormatter resourceCitation) {
        this.resourceCitation = resourceCitation;
    }

    public Map<DataTableColumn, String> getMappedData() {
        return mappedData;
    }

    public void setMappedData(Map<DataTableColumn, String> mappedData) {
        this.mappedData = mappedData;
    }

    @Override
    public boolean isRightSidebar() {
        return true;
    }

    public List<ResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    public void setEffectiveShares(List<ResourceCollection> effectiveShares) {
        this.effectiveShares = effectiveShares;
    }

    public List<BillingAccount> getBillingAccounts() {
        return accountService.listAvailableAccountsForUser(getAuthenticatedUser());
    }

    public List<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    public void setEffectiveResourceCollections(List<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    public List<UserInvite> getInvites() {
        return invites;
    }

    public void setInvites(List<UserInvite> invites) {
        this.invites = invites;
    }

    public List<ResourceCollection> getVisibleUnmanagedCollections() {
        return visibleUnmanagedCollections;
    }

    public void setVisibleUnmanagedCollections(List<ResourceCollection> visibleUnmanagedCollections) {
        this.visibleUnmanagedCollections = visibleUnmanagedCollections;
    }
}
