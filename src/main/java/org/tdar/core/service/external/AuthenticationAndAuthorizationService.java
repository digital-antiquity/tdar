package org.tdar.core.service.external;

import java.util.Arrays;
import java.util.Collection;
import java.util.WeakHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.AbstractConfigurableService;
import org.tdar.core.service.external.auth.AuthenticationProvider;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.core.service.external.auth.TdarGroup;

@Service
public class AuthenticationAndAuthorizationService extends AbstractConfigurableService<AuthenticationProvider> {

    private final WeakHashMap<Person, TdarGroup> groupMembershipCache = new WeakHashMap<Person, TdarGroup>();
    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private AuthorizedUserDao authorizedUserDao;

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

    private synchronized boolean checkAndUpdateCache(Person person, TdarGroup requestedPermissionsGroup) {
        TdarGroup greatestPermissionGroup = groupMembershipCache.get(person);
        if (greatestPermissionGroup == null) {
            greatestPermissionGroup = findGroupWithGreatestPermissions(person);
            groupMembershipCache.put(person, greatestPermissionGroup);
        }
        return greatestPermissionGroup.hasGreaterPermissions(requestedPermissionsGroup);
    }

    private TdarGroup findGroupWithGreatestPermissions(Person person) {
        if (person == null) {
            return TdarGroup.UNAUTHORIZED;
        }
        String email = person.getEmail();
        if (StringUtils.isBlank(email)) {
            return TdarGroup.UNAUTHORIZED;
        }
        TdarGroup greatestPermissionGroup = TdarGroup.UNAUTHORIZED;
        String[] groups = getProvider().findGroupMemberships(email);
        logger.debug("Found " + Arrays.asList(groups) + " memberships for " + email);
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

    public <E> void removeIfNotAllowed(Collection<E> list, E item, InternalTdarRights permission, Person person, boolean override) {
        if (override) {
            return;
        } else {
            removeIfNotAllowed(list, item, permission, person);
        }
    }

}
