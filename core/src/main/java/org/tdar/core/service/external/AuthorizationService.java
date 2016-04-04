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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.InformationResource;
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
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.PersistableUtils;

/*
 * This service is designed to hide the complexity of users and permissions from the rest of tDAR.  It handles a number different functions including:
 * (a) hiding access to different external authentication systems
 * (b) caching permissions and group memberships
 * (c) getting file access and resource access permissions
 */
@Service
public class AuthorizationService implements Accessible {

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

    @Override
    public List<Resource> findEditableResources(TdarUser person, boolean isAdmin, List<ResourceType> resourceTypes) {
        return authorizedUserDao.findEditableResources(person, resourceTypes, isAdmin);
    }

    /**
     * Group Permissions tend to be hierarchical, hence, you may want to know if a user is a member of any of the nested hierarchy. Eg. EDITOR is a subset of
     * ADMIN
     */
    @Transactional(readOnly=true)
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

    /**
     * TdarGroups are represented in the external auth systems, but enable global permissions in tDAR; Admins, Billing Administrators, etc.
     */
    @Transactional(readOnly=true)
    public boolean isMember(TdarUser person, TdarGroup group) {
        return authenticationService.isMember(person, group);
    }

    @Transactional(readOnly=true)
    public boolean isAdministrator(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_ADMIN);
    }

    @Transactional(readOnly=true)
    public boolean isBillingManager(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_BILLING_MANAGER);
    }

    @Transactional(readOnly=true)
    public boolean isEditor(TdarUser person) {
        return isMember(person, TdarGroup.TDAR_EDITOR);
    }

    /*
     * @return all of the resource statuses that a user is allowed to view in a search. Different users have different search permissions in different contexts.
     * A user
     * should be able to see their own DRAFTs, but never DELETED statuss unless they're an admin, for example
     */
    @Transactional(readOnly=true)
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

    /**
     * Checks whether a @link Person has the rights to view a given resource. First, checking whether the person's @link TdarGroup permissions grant them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.VIEW_ALL or greater
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has view privileges
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has read permissions on resource according to the above policies, false otherwise.
     */
    @Transactional(readOnly=true)
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

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.VIEW_ALL);
    }

    /**
     * Checks whether a @link Person has the rights to edit a given resource. First, checking whether the person's @link TdarGroup permissions grant them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.MODIFY_METADATA or greater
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has edit privileges
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
    @Transactional(readOnly=true)
    public boolean canEditResource(TdarUser person, Resource resource, GeneralPermissions basePermission) {
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

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, basePermission);
    }

    /**
     * Avoid using in general, but this allows us to ask whether a User has the "inherited" rights to do something as opposed to being granted direct rights to
     * it.
     * 
     * @param person
     * @param permission
     * @param ids
     * @return
     */
    @Transactional(readOnly=true)
    public boolean isAllowedToEditInherited(TdarUser person, Resource resource) {
        GeneralPermissions permission = GeneralPermissions.MODIFY_METADATA;
        List<Long> ids = new ArrayList<>();
        for (ResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            ids.addAll(collection.getParentIds());
            ids.add(collection.getId());
        }
        return authorizedUserDao.isAllowedTo(person, permission, ids);
    }

    /**
     * Checks whether a @link Person has the rights to edit a @link ResourceCollection. First, checking whether the person's @link TdarGroup permissions grant
     * them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.ADMINISTER_GROUP or greater
     * 
     * @param authenticatedUser
     * @param persistable
     * @return
     */
    @Transactional(readOnly = true)
    public boolean canEditCollection(TdarUser authenticatedUser, ResourceCollection persistable) {
        if (authenticatedUser == null) {
            logger.trace("person is null");
            return false;
        }

        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser) || authenticatedUser.equals(persistable.getOwner())) {
            return true;
        }

        return authorizedUserDao.isAllowedTo(authenticatedUser, persistable, GeneralPermissions.ADMINISTER_GROUP);
    }

    /**
     * This method checks whether a person's group membership allows them to perform an associated right.
     * 
     * The @link InternalTdarRights enum associates global permissions with a @link TdarGroup or set of Groups. These global permissions allow
     * us to simplify permissions management by associating explicit rights with actions in the code, and managing permissions mappings in the enum(s).
     */
    @Transactional(readOnly=true)
    public boolean can(InternalTdarRights rights, TdarUser person) {
        if ((person == null) || (rights == null)) {
            return false;
        }
        if (isMemberOfAny(person, rights.getPermittedGroups())) {
            return true;
        }
        return false;
    }

    /**
     * This method checks whether a person's group membership denies them to perform an associated right @see #can
     */
    @Transactional(readOnly=true)
    public boolean cannot(InternalTdarRights rights, TdarUser person) {
        return !can(rights, person);
    }

    /**
     * Evaluates an <E> (likely resource) in a list and removes it if the @link Person does not have the specified @link InternalTdarRights to perform the
     * action.
     */
    public <E> void removeIfNotAllowed(Collection<E> list, E item, InternalTdarRights permission, TdarUser person) {
        // NOTE: this will FAIL if you use Arrays.asList because that collection is immutable
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (cannot(permission, person)) {
            list.remove(item);
        }
    }

    /**
     * Checks whether the @link Person can perform the specified edit action.
     * 
     * Part of the contract of @link AbstractPersistableController is to checks whether a @link Person (user) can View, Edit, Delete a @link Persistable prior
     * to rendering the page.
     * 
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.Accessible#canEdit(org.tdar.core.bean.entity.Person, org.tdar.core.bean.Persistable)
     */
    @Override
    @Transactional(readOnly=true)
    public boolean canEdit(TdarUser authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canEditResource(authenticatedUser, (Resource) item, GeneralPermissions.MODIFY_METADATA);
        } else if (item instanceof ResourceCollection) {
            return canEditCollection(authenticatedUser, (ResourceCollection) item);
        } else if (item instanceof Institution) {
            return canEditInstitution(authenticatedUser, (Institution) item);
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

    /**
     * Checks whether the @link Person can perform the specified view action.
     * 
     * Part of the contract of @link AbstractPersistableController is to checks whether a @link Person (user) can View, Edit, Delete a @link Persistable prior
     * to rendering the page.
     * 
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.Accessible#canView(org.tdar.core.bean.entity.Person, org.tdar.core.bean.Persistable)
     */
    @Override
    @Transactional(readOnly=true)
    public boolean canView(TdarUser authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canViewResource(authenticatedUser, (Resource) item);
        } else if (item instanceof ResourceCollection) {
            return canViewCollection((ResourceCollection) item, authenticatedUser);
        } else {
            return can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser);
        }
    }

    /**
     * Returns true if the person is privileged, the resource is not restricted in access, or the person is granted @link GeneralPermissions.VIEW_ALL
     * 
     * @param person
     * @return
     */
    @Transactional(readOnly=true)
    public boolean canViewConfidentialInformation(TdarUser person, Resource resource) {
        if (resource instanceof InformationResource) {
            return ((InformationResource) resource).isPublicallyAccessible()
                    || canDo(person, resource, InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO, GeneralPermissions.VIEW_ALL);
        }
        return true;
    }

    /**
     * Confirms that a @link Person has the rights to edit a @link InformationResource and upload files (@link GeneralPermissions.MODIFY_RECORD (as opposed to
     * 
     * @link GeneralPermissions.MODIFY_METADATA ))
     */
    @Transactional(readOnly=true)
    public boolean canUploadFiles(TdarUser person, Resource resource) {
        return canDo(person, resource, InternalTdarRights.EDIT_ANY_RESOURCE, GeneralPermissions.MODIFY_RECORD);
    }

    /**
     * Pairs the @link InternalTdarRights permission with a @link ResourceCollection's @link GeneralPermission to check whether a user
     * can perform an action. Many of the other checks within this class are reflected as canDo checks or could be refactored as such
     * (a) checks if inputs are NULL
     * (b) checks if user is privileged (admin, etc.)
     * (c) checks if user is allowed to perform action based on @link AuthorizedUser / @link ResourceCollection permissions
     * (d) check's iuf user was submitter
     */
    @Transactional(readOnly=true)
    public boolean canDo(TdarUser person, HasSubmitter resource, InternalTdarRights equivalentAdminRight, GeneralPermissions permission) {
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
        if (authorizedUserDao.isAllowedTo(person, resource, permission)) {
            logger.trace("person is an authorized user");
            return true;
        }

        // ab added:12/11/12
        if (PersistableUtils.isTransient(resource) && (resource.getSubmitter() == null)) {
            logger.trace("resource is transient");
            return true;
        }

        logger.trace("returning false... access denied");
        return false;
    }

    private boolean isAdminOrOwner(TdarUser person, HasSubmitter resource, InternalTdarRights equivalentAdminRight) {
        if (Objects.equals(resource.getSubmitter(), person)) {
            logger.trace("person was submitter");
            return true;
        }

        if (can(equivalentAdminRight, person)) {
            logger.trace("person is admin");
            return true;
        }
		return false;
	}

	/*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFileVersion
     */
    @Transactional(readOnly=true)
    public boolean canDownload(InformationResourceFileVersion irFileVersion, TdarUser person) {
        if (irFileVersion == null) {
            return false;
        }
        return canDownload(irFileVersion.getInformationResourceFile(), person);
    }

    /*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFile
     */
    @Transactional(readOnly=true)
    public boolean canDownload(InformationResourceFile irFile, TdarUser person) {
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
    @Transactional(readOnly=true)
    public boolean canViewCollection(ResourceCollection collection, TdarUser person) {
        if (collection == null) {
            return false;
        }

        if (collection.isShared() && !collection.isHidden()) {
            return true;
        }

        if (can(InternalTdarRights.VIEW_ANYTHING, person)) {
            logger.trace("\tuser is special': {}", person);
            return true;
        }
        return authorizedUserDao.isAllowedTo(person, collection, GeneralPermissions.VIEW_ALL);
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
    @Transactional(readOnly=true)
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
                if (((ResourceCollection) item).isShared() && !((ResourceCollection) item).isHidden()) {
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
            item.setViewable(viewable);
        }
    }

	private boolean setupViewable(TdarUser authenticatedUser, Viewable item) {
		boolean viewable = false;
		if (item instanceof HasStatus) { // if we have status, then work off that
		    logger.trace("item 'has status': {}", item);
		    HasStatus status = ((HasStatus) item);
		    if (!status.isActive()) { // if not active, check other permissions
		        logger.trace("item 'is not active': {}", item);
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
            viewable = canDownload(irf, p);
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
    @Transactional(readOnly=true)
    public boolean canAssignInvoice(Invoice invoice, TdarUser authenticatedUser) {
        if (authenticatedUser.equals(invoice.getTransactedBy()) || authenticatedUser.equals(invoice.getOwner())) {
            return true;
        }
        if (authenticationService.isMember(authenticatedUser, TdarGroup.TDAR_BILLING_MANAGER)) {
            return true;
        }
        return false;
    }

    @Transactional(readOnly=true)
    public <R extends Resource> boolean isResourceViewable(TdarUser authenticatedUser, R resource) {
        if (resource == null) {
            return false;
        }
        if (resource.isActive()
                || can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser) || canView(authenticatedUser, resource)
                || canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_METADATA)) {
            logger.trace("{} is viewable: {}", resource.getId(), resource.getClass().getSimpleName());
            return true;
        }
        return false;
    }

    @Transactional(readOnly=true)
    public boolean isResourceEditable(TdarUser authenticatedUser, Resource resource) {
        return canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_METADATA);
    }

    @Transactional(readOnly=true)
    public void applyTransientViewableFlag(InformationResourceFileVersion informationResourceFileVersion, TdarUser authenticatedUser) {
        boolean visible = false;
        if (informationResourceFileVersion == null) {
            return;
        }
        InformationResourceFile irFile = informationResourceFileVersion.getInformationResourceFile();
        if (irFile.isPublic() || canDownload(irFile, authenticatedUser)) {
            visible = true;
        }
        for (InformationResourceFileVersion vers : irFile.getLatestVersions()) {
            vers.setViewable(visible);
        }
    }

    /**
     * Takes the request, and compares the referrer to the known list of referrers in the DB. If one matches, then we're okay
     * 
     * @param informationResourceFileVersion
     * @param apiKey
     * @param request
     * @return true, if download authorized.
     */
    @Transactional(readOnly = true)
    public boolean checkValidUnauthenticatedDownload(InformationResourceFileVersion informationResourceFileVersion, String apiKey, HttpServletRequest request) {
        String referrer = request.getHeader(HttpHeaders.REFERER);
        // this may be an issue: http://webmasters.stackexchange.com/questions/47405/how-can-i-pass-referrer-header-from-my-https-domain-to-http-domains
        try {
            URL url = new URL(referrer);
            referrer = url.getHost();
        } catch (MalformedURLException e) {
            logger.warn("Referrer invalid,  treating as blank: {}", referrer);
            referrer = "";
        }
        if (StringUtils.isBlank(referrer)) {
            throw new TdarRecoverableRuntimeException("authorizationService.referrer_invalid", Arrays.asList(request.getPathInfo(), referrer, request.getHeader(HttpHeaders.USER_AGENT)));
        }
        List<DownloadAuthorization> authorizations = resourceCollectionDao.getDownloadAuthorizations(informationResourceFileVersion, apiKey, referrer);
        return CollectionUtils.isNotEmpty(authorizations);
    }

    @Transactional(readOnly = true)
    public boolean canEditWorkflow(DataIntegrationWorkflow workflow, TdarUser authenticatedUser) {
        return (isAdministrator(authenticatedUser) ||
                PersistableUtils.isNullOrTransient(workflow) || PersistableUtils.isNotNullOrTransient(workflow)
                && PersistableUtils.isEqual(workflow.getSubmitter(), authenticatedUser));
    }

    @Transactional(readOnly = true)
    public boolean canViewWorkflow(DataIntegrationWorkflow workflow, TdarUser authenticatedUser) {
        return canEditWorkflow(workflow, authenticatedUser);

    }

    @Transactional(readOnly = true)
    public boolean canEditAccount(BillingAccount account, TdarUser authenticatedUser) {
        if (can(InternalTdarRights.EDIT_BILLING_INFO, authenticatedUser)) {
            return true;
        }

        if (authenticatedUser.equals(account.getOwner()) || account.getAuthorizedMembers().contains(authenticatedUser)) {
            return true;
        }
        return false;

    }
    
    @Transactional(readOnly=true)
    public boolean canAdministerAccount(BillingAccount account, TdarUser authenticatedUser) {
        return canEditAccount(account, authenticatedUser);
    }


    @Transactional(readOnly=true)
	public void applyTransientViewableFlag(Resource r_, TdarUser authenticatedUser, Collection<Long> collectionIds) {
        Viewable item = (Viewable) r_;
        boolean viewable = setupViewable(authenticatedUser, item);
        boolean allowedToViewAll = authorizedUserDao.isAllowedTo(authenticatedUser, GeneralPermissions.VIEW_ALL, collectionIds);
		if (viewable) {
        	item.setViewable(true);
        } else if (allowedToViewAll) {
			r_.setViewable(true);
		}

        if (r_ instanceof InformationResource) {
			InformationResource ir = (InformationResource)r_;
			for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
		        if (irFile == null) {
		            continue;
		        }
		        if (irFile.isDeleted() && PersistableUtils.isNullOrTransient(authenticatedUser)) {
		            continue;
		        }
		        if (!isAdminOrOwner(authenticatedUser, ir, InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO) && 
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


}
