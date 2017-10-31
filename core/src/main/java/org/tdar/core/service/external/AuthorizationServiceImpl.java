package org.tdar.core.service.external;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.HasUsers;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.ConfidentialViewable;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.RightsResolver;
import org.tdar.utils.PersistableUtils;

/*
 * This service is designed to hide the complexity of users and permissions from the rest of tDAR.  It handles a number different functions including:
 * (a) hiding access to different external authentication systems
 * (b) caching permissions and group memberships
 * (c) getting file access and resource access permissions
 */
@Service
public class AuthorizationServiceImpl implements Accessible, AuthorizationService {

    /*
     * we use a weak hashMap of the group permissions to prevent tDAR from constantly hammering the auth system with the group permissions. The hashMap will
     * track these permissions for short periods of time. Logging out and logging in should reset this
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthorizedUserDao authorizedUserDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#findEditableResources(org.tdar.core.bean.entity.TdarUser, boolean, java.util.List)
     */
    @Override
    public List<Resource> findEditableResources(TdarUser person, boolean isAdmin, List<ResourceType> resourceTypes) {
        return authorizedUserDao.findEditableResources(person, resourceTypes, isAdmin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isMemberOfAny(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.TdarGroup)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isMemberOfAny(TdarUser person, TdarGroup... groups) {
        if ((person == null) || (groups == null)) {
            return false;
        }
        for (TdarGroup group : groups) {
            if (authenticationService.isMember(person, group)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isMember(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.TdarGroup)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isMember(TdarUser person, TdarGroup group) {
        return authenticationService.isMember(person, group);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isAdministrator(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAdministrator(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_ADMIN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isBillingManager(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBillingManager(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_BILLING_MANAGER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isEditor(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isEditor(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_EDITOR);
    }

    /*
     * @return all of the resource statuses that a user is allowed to view in a search. Different users have different search permissions in different contexts.
     * A user
     * should be able to see their own DRAFTs, but never DELETED statuss unless they're an admin, for example
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#getAllowedSearchStatuses(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Status> getAllowedSearchStatuses(TdarUser person) {
        // assumption: ACTIVE always allowed.
        Set<Status> allowed = new HashSet<>(Arrays.asList(Status.ACTIVE));
        if (person == null) {
            return allowed;
        }

        allowed.add(Status.DRAFT);
        if (can(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, person)) {
            allowed.add(Status.DELETED);
        }

        if (can(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, person)) {
            allowed.add(Status.FLAGGED);
            if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
                allowed.add(Status.FLAGGED_ACCOUNT_BALANCE);
            }
        }
        if (can(InternalTdarRights.SEARCH_FOR_DUPLICATE_RECORDS, person)) {
            allowed.add(Status.DUPLICATE);
        }
        return allowed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canViewResource(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canViewResource(TdarUser person, Resource resource) {
        // is the request valid
        if (person == null) {
            return false;
        }
        if (resource == null) {
            return false;
        }

        // does the user have special privileges to edit resources in any status?
        if (can(InternalTdarRights.VIEW_ANYTHING, person)) {
            return true;
        }

        //NOTE: this was a change in Quartz ... it may just need to be return true, which is what it 
        // was historically (things that call this skip this call if resource is active)
        if (resource.isActive()) {
            if (CollectionUtils.isEmpty(resource.getActiveLatitudeLongitudeBoxes())) {
                return true;
            }
            if (!resource.getFirstActiveLatitudeLongitudeBox().isObfuscatedObjectDifferent()) {
                return true;
            }

        }

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, Permissions.VIEW_ALL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEditResource(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.entity.permissions.GeneralPermissions)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEditResource(TdarUser person, Resource resource, Permissions basePermission) {
        // is the request valid
        if (person == null) {
            return false;
        }
        if (resource == null) {
            return false;
        }

        // does the user have special privileges to edit resources in any status?
        if (can(InternalTdarRights.EDIT_ANY_RESOURCE, person)) {
            logger.trace("checking if person can edit any resource");
            return true;
        }

        if (CollectionUtils.isEmpty(resource.getAuthorizedUsers()) && CollectionUtils.isEmpty(resource.getManagedResourceCollections())) {
            return false;
        }

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, basePermission);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEditCollection(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEditCollection(TdarUser authenticatedUser, ResourceCollection persistable) {
        if (authenticatedUser == null) {
            logger.trace("person is null");
            return false;
        }

        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser)) {
            return true;
        }

        return authorizedUserDao.isAllowedTo(authenticatedUser, persistable, Permissions.ADD_TO_COLLECTION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canAdministerCollection(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canAdministerCollection(TdarUser authenticatedUser, ResourceCollection persistable) {
        if (authenticatedUser == null) {
            logger.trace("person is null");
            return false;
        }

        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser)) {
            return true;
        }

        Permissions permission = Permissions.ADMINISTER_COLLECTION;
        // if (persistable instanceof ListCollection) {
        // permission = GeneralPermissions.ADMINISTER_GROUP;
        // }

        return authorizedUserDao.isAllowedTo(authenticatedUser, persistable, permission);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#can(org.tdar.core.dao.external.auth.InternalTdarRights, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean can(InternalTdarRights rights, TdarUser person) {
        if ((person == null) || (rights == null)) {
            return false;
        }
        if (isMemberOfAny(person, rights.getPermittedGroups())) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#cannot(org.tdar.core.dao.external.auth.InternalTdarRights, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean cannot(InternalTdarRights rights, TdarUser person) {
        return !can(rights, person);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#removeIfNotAllowed(java.util.Collection, E, org.tdar.core.dao.external.auth.InternalTdarRights,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public <E> void removeIfNotAllowed(Collection<E> list, E item, InternalTdarRights permission, TdarUser person) {
        // NOTE: this will FAIL if you use Arrays.asList because that collection is immutable
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (cannot(permission, person)) {
            list.remove(item);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEdit(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.Persistable)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEdit(TdarUser authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canEditResource(authenticatedUser, (Resource) item, Permissions.MODIFY_METADATA);
        } else if (item instanceof ResourceCollection) {
            return canEditCollection(authenticatedUser, (ResourceCollection) item);
        } else if (item instanceof Institution) {
            return canEditInstitution(authenticatedUser, (Institution) item);
        } else if (item instanceof DataIntegrationWorkflow) {
            return canEditWorkflow(authenticatedUser, (DataIntegrationWorkflow) item);
        } else {
            return can(InternalTdarRights.EDIT_ANYTHING, authenticatedUser);
        }
    }

    private boolean canEditInstitution(TdarUser authenticatedUser, Institution item) {
        // is the request valid
        if (authenticatedUser == null) {
            return false;
        }

        // does the user have special privileges to edit resources in any status?
        if (can(InternalTdarRights.EDIT_INSTITUTIONAL_ENTITES, authenticatedUser)) {
            logger.trace("checking if person can edit any resource");
            return true;
        }

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return institutionDao.canEditInstitution(authenticatedUser, item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canView(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.Persistable)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canView(TdarUser authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canViewResource(authenticatedUser, (Resource) item);
        } else if (item instanceof ResourceCollection) {
            return canViewCollection(authenticatedUser, (ResourceCollection) item);
        } else if (item instanceof DataIntegrationWorkflow) {
            return canViewWorkflow(authenticatedUser, (DataIntegrationWorkflow) item);
        } else {
            return can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canViewConfidentialInformation(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canViewConfidentialInformation(TdarUser person, Resource resource) {

        if (resource instanceof Project || resource instanceof SupportsResource) {
            return true;
        }
        if (((InformationResource) resource).isPublicallyAccessible()) {
            return true;
        }
        return canDo(person, resource, InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO, Permissions.VIEW_ALL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canUploadFiles(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canUploadFiles(TdarUser person, Resource resource) {
        return canDo(person, resource, InternalTdarRights.EDIT_ANY_RESOURCE, Permissions.MODIFY_RECORD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canDo(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.HasAuthorizedUsers,
     * org.tdar.core.dao.external.auth.InternalTdarRights, org.tdar.core.bean.entity.permissions.GeneralPermissions)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canDo(TdarUser person, HasAuthorizedUsers resource, InternalTdarRights equivalentAdminRight, Permissions permission) {
        // This function used to pre-test on the resource, but it doesn't have to and is now more granular
        if (resource == null) {
            return false;
        }

        if (person == null) {
            logger.trace("person is null");
            return false;
        }

        if (isAdminOrOwner(person, resource, equivalentAdminRight)) {
            return true;
        }

        // ab added:12/11/12
        if (PersistableUtils.isTransient(resource)) {
            logger.trace("resource is transient");
            return true;
        }

        if (isMember(person, TdarGroup.TDAR_RPA_MEMBER) && false) {
            return true;
        }

        if (authorizedUserDao.isAllowedTo(person, resource, permission)) {
            logger.trace("person is an authorized user");
            return true;

        }

        logger.trace("returning false... access denied");
        return false;
    }

    private boolean isAdminOrOwner(TdarUser person, HasAuthorizedUsers resource, InternalTdarRights equivalentAdminRight) {

        if (can(equivalentAdminRight, person)) {
            logger.trace("person is admin");
            return true;
        }
        return false;
    }

    /*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFileVersion
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canDownload(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.resource.file.InformationResourceFileVersion)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canDownload(TdarUser person, InformationResourceFileVersion irFileVersion) {
        if (irFileVersion == null) {
            return false;
        }
        return canDownload(person, irFileVersion.getInformationResourceFile());
    }

    /*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFile
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canDownload(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.resource.file.InformationResourceFile)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canDownload(TdarUser person, InformationResourceFile irFile) {
        if (irFile == null) {
            return false;
        }
        if (irFile.isDeleted() && PersistableUtils.isNullOrTransient(person)) {
            return false;
        }
        if (!irFile.isPublic() && !canViewConfidentialInformation(person, irFile.getInformationResource())) {
            return false;
        }
        return true;
    }

    /*
     * Checks whether a person has the rights to view a collection based on their @link GeneralPermission on the @link ResourceCollection; filters by Shared
     * Visible collections
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canViewCollection(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.collection.VisibleCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canViewCollection(TdarUser person, ResourceCollection collection) {
        if (collection == null) {
            return false;
        }

        if (!collection.isHidden() && collection.getStatus() == Status.ACTIVE) {
            return true;
        }

        if (can(InternalTdarRights.VIEW_ANYTHING, person)) {
            logger.trace("\tuser is special': {}", person);
            return true;
        }
        return authorizedUserDao.isAllowedTo(person, collection, Permissions.VIEW_ALL);
    }

    /*
     * For the view-layer, we add a hint via the @link Viewable interface about whether a @link Resource @link Person or other @link Indexable is viewable or
     * not by
     * the @link Person performing the action.
     * 
     * (a) if the item has a @link Status, and it's Active, ok
     * (b) if not active, then check whether the @link Person can view that status
     * (c) if it's a collection, make sure it's public and shared
     * (d) otherwise, it's probably not
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#applyTransientViewableFlag(org.tdar.core.bean.Indexable, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public void applyTransientViewableFlag(Indexable p, TdarUser authenticatedUser) {
        /*
         * If the Persistable supports the "Viewable" interface, then inject the
         * permissions into the transient property
         */

        // FIXME: it'd be nice if this took an array and could handle multiple lookups at once
        logger.trace("applying transient viewable flag to : {}", p);
        if (p instanceof Viewable) {
            logger.trace("item is a 'viewable': {}", p);
            Viewable item = (Viewable) p;
            boolean viewable = setupViewable(authenticatedUser, item);
            if (item instanceof ResourceCollection) {
                logger.trace("item is resource collection: {}", p);
                if (item instanceof ResourceCollection && !((ResourceCollection) item).isHidden()) {
                    viewable = true;
                }
            }

            if (item instanceof InformationResource) {
                logger.trace("item is information resource (download): {}", p);
                setTransientViewableStatus((InformationResource) item, authenticatedUser);
            }

            if (!viewable && canView(authenticatedUser, p)) {
                logger.trace("user can edit: {}", p);
                viewable = true;
            }

            if (viewable && item instanceof Resource && item instanceof ConfidentialViewable) {
                ((ConfidentialViewable) item).setConfidentialViewable(canViewConfidentialInformation(authenticatedUser, (Resource) item));
            }

            item.setViewable(viewable);
        }
    }

    private boolean setupViewable(TdarUser authenticatedUser, Viewable item) {
        boolean viewable = false;
        if (item instanceof HasStatus) { // if we have status, then work off that
            // logger.trace("item 'has status': {}", item);
            HasStatus status = ((HasStatus) item);
            if (!status.isActive()) { // if not active, check other permissions
                // logger.trace("item 'is not active': {}", item);
                if (can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser)) {
                    logger.trace("\tuser is special': {}", item);
                    viewable = true;
                }
            } else {
                viewable = true;
            }
        }
        return viewable;
    }

    /*
     * sets the @link Viewable status on @link InformationResourceFile and @link InformationResourceFileVersion to simplify lookups on the view layer
     * (Freemarker)
     */
    private void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        Boolean viewable = null;
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            // if (viewable == null) {
            viewable = canDownload(p, irf);
            // }

            irf.setViewable(viewable);
            for (InformationResourceFileVersion irfv : irf.getInformationResourceFileVersions()) {
                irfv.setViewable(viewable);
            }
        }
    }

    /*
     * checks that the specified @link Person can assign an @link Invoice to an @link Account; 1/2 of the check whether the person has the rights to do anything
     * with the
     * Invoive itself
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canAssignInvoice(org.tdar.core.bean.billing.Invoice, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canAssignInvoice(Invoice invoice, TdarUser authenticatedUser) {
        if (authenticatedUser.equals(invoice.getTransactedBy())) {
            return true;
        }
        if (authenticationService.isMember(authenticatedUser, TdarGroup.TDAR_BILLING_MANAGER)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isResourceViewable(org.tdar.core.bean.entity.TdarUser, R)
     */
    @Override
    @Transactional(readOnly = true)
    public <R extends Resource> boolean isResourceViewable(TdarUser authenticatedUser, R resource) {
        if (resource == null) {
            return false;
        }
        if (resource.isActive()
                || can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser) || canView(authenticatedUser, resource)
                || canEditResource(authenticatedUser, resource, Permissions.MODIFY_METADATA)) {
            logger.trace("{} is viewable: {}", resource.getId(), resource.getClass().getSimpleName());
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#isResourceEditable(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isResourceEditable(TdarUser authenticatedUser, Resource resource) {
        return canEditResource(authenticatedUser, resource, Permissions.MODIFY_METADATA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#applyTransientViewableFlag(org.tdar.core.bean.resource.file.InformationResourceFileVersion,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public void applyTransientViewableFlag(InformationResourceFileVersion informationResourceFileVersion, TdarUser authenticatedUser) {
        boolean visible = false;
        if (informationResourceFileVersion == null) {
            return;
        }
        InformationResourceFile irFile = informationResourceFileVersion.getInformationResourceFile();
        if (irFile.isPublic() || canDownload(authenticatedUser, irFile)) {
            visible = true;
        }
        for (InformationResourceFileVersion vers : irFile.getLatestVersions()) {
            vers.setViewable(visible);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthorizationService#checkValidUnauthenticatedDownload(org.tdar.core.bean.resource.file.InformationResourceFileVersion,
     * java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> checkValidUnauthenticatedDownload(InformationResourceFileVersion informationResourceFileVersion, String apiKey, String referrer_) {
        String referrer = referrer_;
        // String referrer = request.getHeader("referer");
        // this may be an issue: http://webmasters.stackexchange.com/questions/47405/how-can-i-pass-referrer-header-from-my-https-domain-to-http-domains
        List<String> errors = new ArrayList<>();
        try {
            URL url = new URL(referrer);
            referrer = url.getHost();
        } catch (MalformedURLException e) {
            logger.warn("Referrer invalid,  treating as blank: {}", referrer);
            referrer = "";
        }
        if (StringUtils.isBlank(referrer)) {
            errors.add("authorizationService.referrer_invalid");
        } else {
            List<DownloadAuthorization> authorizations = resourceCollectionDao.getDownloadAuthorizations(informationResourceFileVersion, apiKey, referrer);
            if (CollectionUtils.isEmpty(authorizations)) {
                errors.add("authorizationService.invalid_request");
            }
        }
        return errors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEditWorkflow(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.integration.DataIntegrationWorkflow)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEditWorkflow(TdarUser authenticatedUser, DataIntegrationWorkflow workflow) {
        if (PersistableUtils.isNullOrTransient(workflow)) {
            return true;
        }

        if (isAdministrator(authenticatedUser)) {
            return true;
        }

        for (AuthorizedUser au : workflow.getAuthorizedUsers()) {
            if (au.getUser().equals(authenticatedUser) && (Permissions.EDIT_INTEGRATION.ordinal() - 1) < au.getEffectiveGeneralPermission()) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canViewWorkflow(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.integration.DataIntegrationWorkflow)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canViewWorkflow(TdarUser authenticatedUser, DataIntegrationWorkflow workflow) {

        if (!workflow.isHidden()) {
            return true;
        }
        return canEditWorkflow(authenticatedUser, workflow);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEditAccount(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.billing.BillingAccount)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEditAccount(TdarUser authenticatedUser, BillingAccount account) {
        logger.debug("can edit account: {} ({})", account, authenticatedUser);
        if (can(InternalTdarRights.EDIT_BILLING_INFO, authenticatedUser)) {
            return true;
        }

        for (AuthorizedUser au : account.getAuthorizedUsers()) {
            logger.debug("au: {}", au);
            if (au.getUser().equals(authenticatedUser) && (Permissions.EDIT_ACCOUNT.ordinal() - 1) < au.getEffectiveGeneralPermission()) {
                return true;
            }
        }
        return false;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canAdministerAccount(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.billing.BillingAccount)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canAdministerAccount(TdarUser authenticatedUser, BillingAccount account) {
        return canEditAccount(authenticatedUser, account);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#applyTransientViewableFlag(org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.entity.TdarUser, java.util.Collection)
     */
    @Override
    @Transactional(readOnly = true)
    public void applyTransientViewableFlag(Resource r_, TdarUser authenticatedUser, Collection<Long> collectionIds) {
        Viewable item = (Viewable) r_;
        boolean viewable = setupViewable(authenticatedUser, item);
        boolean allowedToViewAll = authorizedUserDao.isAllowedTo(authenticatedUser, Permissions.VIEW_ALL, collectionIds);
        if (logger.isTraceEnabled()) {
            Long auid = null;
            if (authenticatedUser != null) {
                auid = authenticatedUser.getId();
            }
            logger.trace("::applytransientViewable: r:{} u:{} c:{}", r_.getId(), auid, collectionIds);
            logger.trace(":: st:{} admin:{} ", r_.getStatus(), isAdministrator(authenticatedUser));
            logger.trace(":: viewable:{} ({}) ", viewable, allowedToViewAll);
        }
        if (viewable) {
            item.setViewable(true);
        } else if (allowedToViewAll) {
            r_.setViewable(true);
        }

        if (r_ instanceof InformationResource) {
            InformationResource ir = (InformationResource) r_;
            boolean adminOrOwner = isAdminOrOwner(authenticatedUser, ir, InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO);

            for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
                if (irFile == null) {
                    continue;
                }
                if (irFile.isDeleted() && PersistableUtils.isNullOrTransient(authenticatedUser)) {
                    continue;
                }
                if (!adminOrOwner &&
                        !irFile.isPublic() && !allowedToViewAll) {
                    continue;
                }
                irFile.setViewable(true);
                for (InformationResourceFileVersion vers : irFile.getLatestVersions()) {
                    vers.setViewable(true);
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canAdminiserUsersOn(org.tdar.core.bean.resource.HasAuthorizedUsers,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canAdminiserUsersOn(HasAuthorizedUsers source, TdarUser actor) {
        // if we're internal we want to check if the actor is the submitter
        if (source instanceof Resource && canEditResource(actor, (Resource) source, Permissions.MODIFY_RECORD)) {
            return true;
        }
        if (source instanceof ResourceCollection && canAdministerCollection(actor, (ResourceCollection) source)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#updateAuthorizedMembers(org.tdar.core.bean.billing.HasUsers, java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateAuthorizedMembers(HasUsers entity, List<TdarUser> members) {
        logger.info("authorized members (was): {}", entity.getAuthorizedMembers());
        entity.getAuthorizedMembers().clear();
        entity.getAuthorizedMembers().addAll(members);
        logger.info("authorized members ( is): {}", entity.getAuthorizedMembers());
        institutionDao.saveOrUpdate(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canViewBillingAccount(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.billing.BillingAccount)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canViewBillingAccount(TdarUser authenticatedUser, BillingAccount account) {
        if (PersistableUtils.isNullOrTransient(authenticatedUser)) {
            return false;
        }

        if (can(InternalTdarRights.VIEW_BILLING_INFO, authenticatedUser)) {
            return true;
        }

        for (AuthorizedUser au : account.getAuthorizedUsers()) {
            logger.debug("au: {}", au);
            if (au.getUser().equals(authenticatedUser) && (Permissions.EDIT_ACCOUNT.ordinal() - 1) < au.getEffectiveGeneralPermission()) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canEditCreator(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.entity.Creator)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canEditCreator(TdarUser tdarUser, Creator<?> persistable) {
        if (PersistableUtils.isNullOrTransient(tdarUser)) {
            return false;
        }

        if (persistable instanceof Institution) {
            return canEdit(tdarUser, persistable);
        }

        if (Objects.equals(persistable, tdarUser) || can(InternalTdarRights.EDIT_PERSONAL_ENTITES, tdarUser)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canAddToCollection(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    public boolean canAddToCollection(TdarUser user, ResourceCollection collectionToAdd) {
        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, user)) {
            return true;
        }
        Permissions permission =  Permissions.ADD_TO_COLLECTION;
        return authorizedUserDao.isAllowedTo(user, collectionToAdd, permission);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#canRemoveFromCollection(org.tdar.core.bean.collection.ResourceCollection,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public boolean canRemoveFromCollection(ResourceCollection collection, TdarUser user) {
        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, user)) {
            return true;
        }

        Permissions permission = Permissions.REMOVE_FROM_COLLECTION;
        return authorizedUserDao.isAllowedTo(user, collection, permission);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.AuthorizationService#getRightsResolverFor(org.tdar.core.bean.resource.HasAuthorizedUsers,
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.dao.external.auth.InternalTdarRights)
     */
    @Override
    @Transactional(readOnly = true)
    public RightsResolver getRightsResolverFor(HasAuthorizedUsers resource, TdarUser actor, InternalTdarRights rights) {
        RightsResolver resolver = new RightsResolver();
        if (can(rights, actor)) {
            resolver.setAdmin(true);
            return resolver;
        }

        return RightsResolver.evaluate(authorizedUserDao.checkSelfEscalation(actor, resource, rights, null));
    }

}
