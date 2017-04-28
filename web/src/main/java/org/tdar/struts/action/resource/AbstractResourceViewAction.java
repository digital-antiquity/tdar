package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts.action.SlugViewAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.transform.OpenUrlFormatter;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.ResourceCitationFormatter;

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

    // public static final String RESOURCE_EDIT_TEMPLATE = "../resource/edit-template.ftl";

    private List<EmailMessageType> emailTypes = EmailMessageType.valuesWithoutConfidentialFiles();

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    private boolean hasDeletedFiles = false;

    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private ObfuscationService obfuscationService;

    @Autowired
    public ResourceCollectionService resourceCollectionService;

    @Autowired
    private BillingAccountService accountService;

    @Autowired
    private ResourceService resourceService;

    private List<ResourceCollection> resourceCollections = new ArrayList<>();
    private List<ResourceCollection> effectiveResourceCollections = new ArrayList<>();

    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;
    private List<ResourceCreatorProxy> contactProxies;
    private ResourceCitationFormatter resourceCitation;

    private List<ResourceCollection> viewableResourceCollections;

    private String schemaOrgJsonLD;

    private Map<DataTableColumn, String> mappedData;

    private void initializeResourceCreatorProxyLists() {
        Set<ResourceCreator> resourceCreators = getPersistable().getResourceCreators();
        resourceCreators = getPersistable().getActiveResourceCreators();
        if (resourceCreators == null) {
            return;
        }
        authorshipProxies = new ArrayList<>();
        creditProxies = new ArrayList<>();

        // this may be duplicative... check
        for (ResourceCreator rc : resourceCreators) {
            if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
                if ((rc.getCreatorType() == CreatorType.PERSON) && !isAuthenticated()) {
                    obfuscationService.obfuscate(rc.getCreator(), getAuthenticatedUser());
                }
            }

            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }

            if (proxy.isValidEmailContact()) {
                getContactProxies().add(proxy);
            }
        }
        Collections.sort(authorshipProxies);
        Collections.sort(creditProxies);
    }

    public String getOpenUrl() {
        return OpenUrlFormatter.toOpenURL(getResource());
    }

    public String getGoogleScholarTags() throws Exception {
        return resourceService.getGoogleScholarTags(getResource());
    }

    // Return list of acceptable billing accounts. If the resource has an account, this method will include it in the returned list even
    // if the user does not have explicit rights to the account (e.g. so that a user w/ edit rights on the resource can modify the resource
    // and maintain original billing account).
    protected List<BillingAccount> determineActiveAccounts() {
        List<BillingAccount> accounts = new LinkedList<>(accountService.listAvailableAccountsForUser(getAuthenticatedUser()));
        if (getResource() != null) {
            BillingAccount resourceAccount = getResource().getAccount();
            if ((resourceAccount != null) && !accounts.contains(resourceAccount)) {
                accounts.add(0, resourceAccount);
            }
        }
        return accounts;
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
        // only showing access count when logged in (speeds up page loads)
        if (isAuthenticated()) {
            resourceService.updateTransientAccessCount(getResource());
        }
        // don't count if we're an admin
        if (!PersistableUtils.isEqual(getPersistable().getSubmitter(), getAuthenticatedUser()) && !isEditor()) {
            resourceService.incrementAccessCounter(getPersistable(), isBot());
        }
        accountService.updateTransientAccountInfo((List<Resource>) Arrays.asList(getResource()));
        bookmarkedResourceService.applyTransientBookmarked(Arrays.asList(getResource()), getAuthenticatedUser());
        if (isEditor()) {
            if (getPersistableClass().equals(Project.class)) {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatisticsForProject(getId(), null));
            } else {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(Arrays.asList(getId()), null));
            }
        }

        return SUCCESS;
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

    public <T extends Sequenceable<T>> void prepSequence(List<T> list) {
        if (list == null) {
            return;
        }
        if (list.isEmpty()) {
            return;
        }
        list.removeAll(Collections.singletonList(null));
        AbstractSequenced.applySequence(list);
    }

    public void loadBasicViewMetadata() {
        getAuthorizedUsers().addAll(resourceCollectionService.getAuthorizedUsersForResource(getResource(), getAuthenticatedUser()));
        initializeResourceCreatorProxyLists();
        loadEffectiveResourceCollections();
        getLogger().trace("effective collections: {}", getEffectiveResourceCollections());
    }

    private void loadEffectiveResourceCollections() {
        getResourceCollections().addAll(getResource().getSharedResourceCollections());
        getEffectiveResourceCollections().addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(getResource()));
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

    public void setCreditProxies(List<ResourceCreatorProxy> creditProxies) {
        this.creditProxies = creditProxies;
    }

    public List<ResourceCreatorRole> getInstitutionAuthorshipRoles() {
        return ResourceCreatorRole.getAuthorshipRoles(CreatorType.INSTITUTION, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getInstitutionCreditRoles() {
        return ResourceCreatorRole.getCreditRoles(CreatorType.INSTITUTION, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getPersonAuthorshipRoles() {
        return ResourceCreatorRole.getAuthorshipRoles(CreatorType.PERSON, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getPersonCreditRoles() {
        return ResourceCreatorRole.getCreditRoles(CreatorType.PERSON, getResource().getResourceType());
    }

    /**
     * @param resourceCollections
     *            the resourceCollections to set
     */
    public void setResourceCollections(List<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    public List<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    /**
     * @return the effectiveResourceCollections
     */
    public List<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    /**
     * @param effectiveResourceCollections
     *            the effectiveResourceCollections to set
     */
    public void setEffectiveResourceCollections(List<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    // return all of the collections that the currently-logged-in user is allowed to view. We define viewable as either shared+visible, or
    // shared+invisible+canEdit
    public List<ResourceCollection> getViewableResourceCollections() {
        if (viewableResourceCollections != null) {
            return viewableResourceCollections;
        }

        // if nobody logged in, just get the shared+visible collections
        Set<ResourceCollection> collections = new HashSet<>(getResource().getSharedVisibleResourceCollections());
        collections.addAll(getResource().getVisibleUnmanagedResourceCollections());
        // if authenticated, also add the collections that the user can modify
        if (isAuthenticated()) {
            Set<ResourceCollection> all = new HashSet<>(getResource().getSharedResourceCollections());
            all.addAll(getResource().getUnmanagedResourceCollections());
            for (ResourceCollection resourceCollection : all) {
                if (authorizationService.canViewCollection(resourceCollection, getAuthenticatedUser())) {
                    collections.add(resourceCollection);
                }
            }
        }

        viewableResourceCollections = new ArrayList<>(collections);
        return viewableResourceCollections;
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
        return isEditor();
    }

    protected void loadCustomViewMetadata() throws TdarActionException {
        if (getResource() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) getResource();
            boolean fail = false;
            if (getTdarConfiguration().isProductionEnvironment()) {
                fail = true;
            }
            setMappedData(resourceService.getMappedDataForInformationResource(informationResource, fail));
            setTransientViewableStatus(informationResource, getAuthenticatedUser());
        }

    }

    public List<EmailMessageType> getEmailTypes() {
        if (getResource() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) getResource();
            if (informationResource.hasConfidentialFiles()) {
                emailTypes = Arrays.asList(EmailMessageType.values());
            }
        }
        return emailTypes;
    }

    public void setEmailTypes(List<EmailMessageType> emailTypes) {
        this.emailTypes = emailTypes;
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
        if (PersistableUtils.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                informationResourceFileService.updateTransientDownloadCount(irf);
                if (irf.isDeleted()) {
                    setHasDeletedFiles(true);
                }
            }
        }
    }

    public boolean isHasDeletedFiles() {
        return hasDeletedFiles;
    }

    public void setHasDeletedFiles(boolean hasDeletedFiles) {
        this.hasDeletedFiles = hasDeletedFiles;
    }

    private Boolean editable = null;

    public boolean isEditable() {
        if (isNullOrNew()) {
            return false;
        }
        if (editable == null) {
            editable = authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_METADATA);
        }
        return editable;
    }

    public String getSchemaOrgJsonLD() {
        return schemaOrgJsonLD;
    }

    public void setSchemaOrgJsonLD(String schemaOrgJsonLD) {
        this.schemaOrgJsonLD = schemaOrgJsonLD;
    }

    private transient WhiteLabelCollection whiteLabelCollection;
    
    @XmlTransient
    /**
     * We assume for now that a resource will only belong to a single white-label collection.
     *
     * @return
     */
    public WhiteLabelCollection getWhiteLabelCollection() {
        if (whiteLabelCollection == null) {
            whiteLabelCollection = resourceCollectionService.getWhiteLabelCollectionForResource(getResource());
        }
        return whiteLabelCollection;
    }

    public boolean isWhiteLabelLogoAvailable() {
        WhiteLabelCollection wlc = getWhiteLabelCollection();
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
}
