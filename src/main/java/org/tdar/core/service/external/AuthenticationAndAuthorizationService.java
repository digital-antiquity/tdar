package org.tdar.core.service.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.AbstractConfigurableService;
import org.tdar.core.service.external.auth.AuthenticationProvider;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.core.service.external.auth.TdarGroup;
import org.tdar.struts.action.search.ReservedSearchParameters;

@Service
public class AuthenticationAndAuthorizationService extends AbstractConfigurableService<AuthenticationProvider> implements Accessible {

    private final WeakHashMap<Person, TdarGroup> groupMembershipCache = new WeakHashMap<Person, TdarGroup>();
    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private AuthorizedUserDao authorizedUserDao;

    @Autowired
    private PersonDao personDao;

    @Override
    public boolean isServiceRequired() {
        return true;
    };

    public boolean isMember(Person person, TdarGroup group) {
        return checkAndUpdateCache(person, group);
    }

    public boolean isAdministrator(Person person) {
        return checkAndUpdateCache(person, TdarGroup.TDAR_ADMIN);
    }

    public boolean isEditor(Person person) {
        return checkAndUpdateCache(person, TdarGroup.TDAR_EDITOR);
    }

    public boolean isMemberOfAny(Person person, TdarGroup... groups) {
        if (person == null || groups == null) {
            return false;
        }
        for (TdarGroup group : groups) {
            if (isMember(person, group)) {
                return true;
            }
        }
        return false;
    }

    public void updateUsername(Person person, String newUsername, String password) {
        if (personDao.findByUsername(newUsername.toLowerCase()) != null) {
            throw new TdarRecoverableRuntimeException(String.format("Username %s already exists", newUsername));
        }

        String[] groupNames = getProvider().findGroupMemberships(person);
        List<TdarGroup> groups = new ArrayList<TdarGroup>();
        for (String groupName : groupNames) {
            groups.add(TdarGroup.valueOf(groupName));
        }
        getProvider().deleteUser(person);
        person.setUsername(newUsername.toLowerCase());
        getProvider().addUser(person, password, groups.toArray(new TdarGroup[0]));
    }

    private synchronized boolean checkAndUpdateCache(Person person, TdarGroup requestedPermissionsGroup) {
        TdarGroup greatestPermissionGroup = groupMembershipCache.get(person);
        if (greatestPermissionGroup == null) {
            greatestPermissionGroup = findGroupWithGreatestPermissions(person);
            groupMembershipCache.put(person, greatestPermissionGroup);
        }
        return greatestPermissionGroup.hasGreaterPermissions(requestedPermissionsGroup);
    }

    // return all of the resource statuses that a user is allowed to view in a search.
    public Set<Status> getAllowedSearchStatuses(Person person) {
        // assumption: ACTIVE always allowed.
        Set<Status> allowed = new HashSet<Status>(Arrays.asList(Status.ACTIVE));
        if (person == null)
            return allowed;

        allowed.add(Status.DRAFT);
        if (can(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, person)) {
            allowed.add(Status.DELETED);
        }

        if (can(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, person)) {
            allowed.add(Status.FLAGGED);
        }
        return allowed;
    }

    public void initializeReservedSearchParameters(ReservedSearchParameters reservedSearchParameters, Person user) {
        reservedSearchParameters.setAuthenticatedUser(user);
        reservedSearchParameters.setTdarGroup(findGroupWithGreatestPermissions(user));
        Set<Status> allowedSearchStatuses = getAllowedSearchStatuses(user);

        if (CollectionUtils.isEmpty(reservedSearchParameters.getStatuses())) {
            reservedSearchParameters.setStatuses(new ArrayList<Status>(Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        }

        reservedSearchParameters.getStatuses().retainAll(allowedSearchStatuses);
        if (reservedSearchParameters.getStatuses().isEmpty()) {
            throw (new TdarRecoverableRuntimeException("You are not allowed to search for resources with the selected status"));
        }

    }

    public TdarGroup findGroupWithGreatestPermissions(Person person) {
        if (person == null) {
            return TdarGroup.UNAUTHORIZED;
        }
        String login = person.getUsername();
        if (StringUtils.isBlank(login)) {
            return TdarGroup.UNAUTHORIZED;
        }
        TdarGroup greatestPermissionGroup = TdarGroup.UNAUTHORIZED;
        String[] groups = getProvider().findGroupMemberships(person);
        logger.trace("Found " + Arrays.asList(groups) + " memberships for " + login);
        for (String groupString : groups) {
            TdarGroup group = TdarGroup.fromString(groupString);
            if (group.hasGreaterPermissions(greatestPermissionGroup)) {
                greatestPermissionGroup = group;
            }
        }
        return greatestPermissionGroup;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return getProvider();
    }

    public synchronized void clearPermissionsCache() {
        logger.debug("Clearing group membership cache of all entries: " + groupMembershipCache);
        groupMembershipCache.clear();
    }

    public synchronized void clearPermissionsCache(Person person) {
        logger.debug("Clearing group membership cache of entry for : " + person);
        groupMembershipCache.remove(person);
    }

    /**
     * Returns true iff
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has view privileges
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
    public boolean canViewResource(Person person, Resource resource) {
        // is the request valid
        if (person == null)
            return false;
        if (resource == null)
            return false;

        // does the user have special privileges to edit resources in any status?
        if (can(InternalTdarRights.VIEW_ANYTHING, person))
            return true;

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.VIEW_ALL);
    }

    /**
     * Returns true iff
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
    public boolean canEditResource(Person person, Resource resource) {
        // is the request valid
        if (person == null)
            return false;
        if (resource == null)
            return false;

        // does the user have special privileges to edit resources in any status?
        if (can(InternalTdarRights.EDIT_ANY_RESOURCE, person))
            return true;

        // finally, check if user has been granted permission
        // FIXME: technically the dao layer is doing some stuff that we should be, but I don't want to mess w/ it right now.
        return authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.MODIFY_RECORD);
    }

    /**
     * @param authenticatedUser
     * @param persistable
     * @return
     */
    @Transactional(readOnly = false)
    public boolean canEditCollection(Person authenticatedUser, ResourceCollection persistable) {
        if (authenticatedUser == null) {
            logger.debug("person is null");
            return false;
        }

        if (can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser) || authenticatedUser.equals(persistable.getOwner())) {
            return true;
        }

        return authorizedUserDao.isAllowedTo(authenticatedUser, GeneralPermissions.ADMINISTER_GROUP, persistable);
    }

    public boolean can(InternalTdarRights rights, Person person) {
        if (person == null || rights == null) {
            return false;
        }
        if (isMemberOfAny(person, rights.getPermittedGroups())) {
            return true;
        }
        return false;
    }

    public boolean cannot(InternalTdarRights rights, Person person) {
        return !can(rights, person);
    }

    /*
     * A generic helper function that allows us to manage lists
     */
    public <E> void removeIfNotAllowed(Collection<E> list, E item, InternalTdarRights permission, Person person) {
        // NOTE: this will FAIL if you use Arrays.asList because that collection is immutable
        if (CollectionUtils.isEmpty(list))
            return;
        if (cannot(permission, person)) {
            list.remove(item);
        }
    }

    @Override
    public boolean canEdit(Person authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canEditResource(authenticatedUser, (Resource) item);
        } else if (item instanceof ResourceCollection) {
            return canEditCollection(authenticatedUser, (ResourceCollection) item);
        } else {
            return can(InternalTdarRights.EDIT_ANYTHING, authenticatedUser);
        }
    }

    @Override
    public boolean canView(Person authenticatedUser, Persistable item) {
        if (item instanceof Resource) {
            return canViewResource(authenticatedUser, (Resource) item);
        } else if (item instanceof ResourceCollection) {
            return canViewCollection((ResourceCollection) item, authenticatedUser);
        } else {
            return can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser);
        }
    }

    /**
     * Returns true if the person is privileged or is a member of the full team read access.
     * 
     * @param person
     * @return
     */
    public boolean canViewConfidentialInformation(Person person, Resource resource) {
        // This function used to pre-test on the resource, but it doesn't have to and is now more granular
        if (resource == null)
            return false;

        if (person == null) {
            logger.trace("person is null");
            return false;
        }

        if (ObjectUtils.equals(resource.getSubmitter(), person)) {
            logger.debug("person was submitter");
            return true;
        }

        if (can(InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO, person)) {
            return true;
        }

        if (authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.VIEW_ALL)) {
            logger.debug("person is an authorized user");
            return true;
        }

        logger.debug("returning false... access denied");
        return false;
    }

    /**
     * This is a fairly expensive operation.
     * 
     * Returns true iff
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has curator privileges (signified in crowd)
     * <li>the person has full user privileges on the resource
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
    // @Deprecated
    // public boolean canEditResource(Person person, Resource resource) {
    // return person != null
    // && resource != null
    // && (resource.getSubmitter().equals(person) || authenticationService.can(InternalTdarRights.EDIT_RESOURCES, person) || authorizedUserDao
    // .isAllowedTo(person, resource,
    // GeneralPermissions.MODIFY_RECORD));
    // }

    public boolean canDownload(InformationResourceFileVersion irFileVersion, Person person) {
        if (irFileVersion == null)
            return false;
        return canDownload(irFileVersion.getInformationResourceFile(), person);
    }

    public boolean canDownload(InformationResourceFile irFile, Person person) {
        if (irFile == null)
            return false;
        if (!irFile.isPublic() && !canViewConfidentialInformation(person, irFile.getInformationResource())) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canViewCollection(ResourceCollection collection, Person person) {
        if (collection.isShared() && collection.isVisible())
            return true;
        return authorizedUserDao.isAllowedTo(person, GeneralPermissions.VIEW_ALL, collection);
    }

    public void applyTransientViewableFlag(Indexable p, Person authenticatedUser) {
        /*
         * If the Persistable supports the "Viewable" interface, then inject the
         * permissions into the transient property
         */
        if (p instanceof Viewable) {
            boolean viewable = false; // by default -- not allowed to view
            Viewable item = (Viewable) p;
            if (item instanceof HasStatus) { // if we have status, then work off that
                HasStatus status = ((HasStatus) item);
                if (!status.isActive()) { // if not active, check other permissions
                    if (can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser)) {
                        viewable = true;
                    }
                } else {
                    viewable = true;
                }
            }
            if (item instanceof ResourceCollection) {
                if (((ResourceCollection) item).isShared() && !((ResourceCollection) item).isInternal()) {
                    viewable = true;
                }
            }

            if (!viewable && canEdit(authenticatedUser, p)) {
                viewable = true;
            }
            item.setViewable(viewable);
        }
    }

    // normalize username to abide by our business rules
    // TODO: replace calls to username.toLowerCase() where appropriate
    public String normalizeUsername(String userName) {
        // for now, we just lowercase it.
        String normalizedUsername = userName.toLowerCase();
        return normalizedUsername;
    }

    public void setTransientViewableStatus(InformationResource ir, Person p) {
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            boolean viewable = canDownload(irf, p);

            irf.setViewable(viewable);
            for (InformationResourceFileVersion irfv : irf.getInformationResourceFileVersions()) {
                irfv.setViewable(viewable);
            }
        }
    }

}
