package org.tdar.core.service.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.service.RightsResolver;
import org.tdar.utils.PersistableUtils;

/**
 * This is a utility class to try and simplify collection rights comparisons
 * 
 * @author abrin
 *
 */
public class CollectionRightsComparator {

    private static final Logger logger = LoggerFactory.getLogger(CollectionRightsComparator.class);
    private Set<AuthorizedUser> currentUsers = new HashSet<>();
    private Set<AuthorizedUser> incomingUsers = new HashSet<>();

    private List<AuthorizedUser> additions = new ArrayList<>();
    private List<AuthorizedUser> deletions = new ArrayList<>();
    private List<AuthorizedUser> changes = new ArrayList<>();

    public CollectionRightsComparator(Set<AuthorizedUser> currentUsers, Collection<AuthorizedUser> incomingUsers) {
        if (CollectionUtils.isNotEmpty(currentUsers)) {
            logger.debug("current users {}", currentUsers);
            this.currentUsers.addAll(currentUsers);
        }
        if (CollectionUtils.isNotEmpty(incomingUsers)) {
            this.incomingUsers.addAll(normalizeAuthorizedUsers(incomingUsers));
        }
    }

    /**
     * Remove entries from provided list of AuthorizedUsers that contain duplicate User values. Retained
     * AuthorizedUsers will always have equal or greater permissions relative to the removed duplicate items.
     * 
     * @param authorizedUsers
     */
    public Set<AuthorizedUser> normalizeAuthorizedUsers(final Collection<AuthorizedUser> authorizedUsers_) {
        logger.debug("normalizing authorized Users:{}", authorizedUsers_);
        Set<AuthorizedUser> authorizedUsers = new HashSet<>();
        if (CollectionUtils.isEmpty(authorizedUsers_)) {
            return authorizedUsers;
        }
        logger.trace("incoming " + authorizedUsers);
        Map<Long, AuthorizedUser> bestMap = new HashMap<>();
        Iterator<AuthorizedUser> iterator = authorizedUsers_.iterator();
        while (iterator.hasNext()) {
            AuthorizedUser incoming = iterator.next();
            if ((incoming == null) || (incoming.getUser() == null)) {
                continue;
            }
            Long user = incoming.getUser().getId();

            AuthorizedUser existing = bestMap.get(user);
            logger.trace(incoming + " <==>" + existing);
            if (existing != null) {
                if (existing.getGeneralPermission().getEffectivePermissions() >= incoming.getGeneralPermission().getEffectivePermissions()) {
                    continue;
                }
            }
            bestMap.put(user, incoming);
        }

        authorizedUsers.addAll(bestMap.values());
        logger.debug("outgoing" + authorizedUsers);
        return authorizedUsers;
    }

    public boolean rightsDifferent() {
        Map<Long, String> userIdMap = new HashMap<>();
        // iterate through current users, add them to the map
        List<Long> changeUserIds = new ArrayList<>();
        if (logger.isTraceEnabled()) {
            logger.trace("incoming:{}", incomingUsers);
            logger.trace("current:{}", currentUsers);
        }
        for (AuthorizedUser user : currentUsers) {
            if (user == null) {
                continue;
            }
            if (PersistableUtils.isTransient(user.getUser())) {
                logger.debug(">> {}", user);
                return true;
            }
            addRemoveMap(userIdMap, user, true);
        }

        // iterate through the incoming list
        for (AuthorizedUser user : incomingUsers) {
            if (user == null || PersistableUtils.isNullOrTransient(user.getUser())) {
                continue;
            }
            changeUserIds.add(user.getUser().getId());
            addRemoveMap(userIdMap, user, false);
        }

        // if there are no changes and no additions, and the map is empty, then, we're done
        if (MapUtils.isEmpty(userIdMap) && CollectionUtils.isEmpty(getAdditions()) && CollectionUtils.isEmpty(getChanges())) {
            logger.debug("skipping rights section b/c no-changes");
            return false;
        }

        // otherwise, find deletions
        for (AuthorizedUser user : currentUsers) {
            if (user == null || PersistableUtils.isNullOrTransient(user.getUser())) {
                continue;
            }
            for (Long id : userIdMap.keySet()) {
                // skip changes
                if (changeUserIds.contains(id)) {
                    continue;
                }

                if (Objects.equals(user.getUser().getId(), id)) {
                    getDeletions().add(user);
                }
            }
        }
        logger.debug("add: {} ; change: {} ; delete: {}", getAdditions(), getChanges(), getDeletions());
        return true;
    }

    /**
     * Add or remove an entry from the map. If the map is empty at the end, then we have 0 changes.
     * 
     * @param map
     * @param user
     * @param add
     * @return
     */
    private void addRemoveMap(Map<Long, String> map, AuthorizedUser user, boolean add) {
        if (user == null) {
            return;
        }
        Long id = user.getUser().getId();
        String compareKey = getCompareKey(user);
        if (add) {
            // if we're adding, insert into the map
            map.put(id, compareKey);
        } else {
            // try and get the permissions from the map
            String perm = map.get(id);
            if (perm != null) {
                // if we're there, then eitehr a no-op if exact match or a change
                if (Objects.equals(perm, compareKey)) {
                    map.remove(id);
                } else {
                    getChanges().add(user);
                }
            } else {
                // if we're not in the map, we're an addition
                getAdditions().add(user);
            }
        }
    }

    private String getCompareKey(AuthorizedUser user) {
        String key = String.format("%s-%s", user.getGeneralPermission(), user.getDateExpires());
        logger.trace(key);
        return key;
    }

    public List<AuthorizedUser> getAdditions() {
        return additions;
    }

    public void setAdditions(List<AuthorizedUser> additions) {
        this.additions = additions;
    }

    public List<AuthorizedUser> getDeletions() {
        return deletions;
    }

    public void setDeletions(List<AuthorizedUser> deletions) {
        this.deletions = deletions;
    }

    public List<AuthorizedUser> getChanges() {
        return changes;
    }

    public void setChanges(List<AuthorizedUser> changes) {
        this.changes = changes;
    }

    public void makeChanges(RightsResolver rco, HasAuthorizedUsers account, TdarUser authenticatedUser) {

        if (!rco.canModifyUsersOn(account)) {
            rco.logDebug(authenticatedUser, null);
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }

        for (AuthorizedUser user : getDeletions()) {
            account.getAuthorizedUsers().remove(user);
        }

        for (AuthorizedUser user : getAdditions()) {
            if (rco.hasPermissionsEscalation(user)) {
                rco.logDebug(authenticatedUser, user);
                throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
            }

            if (PersistableUtils.isNullOrTransient(user.getCreatedBy())) {
                user.setCreatedBy(authenticatedUser);
            }
            account.getAuthorizedUsers().add(user);
        }
        handleDifferences(account, authenticatedUser, rco);
    }

    public void handleDifferences(HasAuthorizedUsers resource, TdarUser actor, RightsResolver rco) {
        if (CollectionUtils.isNotEmpty(getChanges())) {
            Map<Long, AuthorizedUser> idMap2 = null;

            Map<Long, AuthorizedUser> idMap = PersistableUtils.createIdMap(resource.getAuthorizedUsers());
            for (AuthorizedUser user : getChanges()) {
                AuthorizedUser actual = idMap.get(user.getId());
                if (actual == null) {
                    // it's possible that the authorizedUserId was not passed back from the client
                    // if so, build a secondary map using the TdarUser (authorizedUser.user) id.
                    if (idMap2 == null) {
                        idMap2 = new HashMap<>();
                        for (AuthorizedUser au : resource.getAuthorizedUsers()) {
                            idMap2.put(au.getUser().getId(), au);
                        }
                    }

                    actual = idMap2.get(user.getUser().getId());
                    logger.debug("actual was null, now: {}", actual);
                }
                rco.checkEscalation(actor, user);
                actual.setGeneralPermission(user.getGeneralPermission());
                actual.setDateExpires(user.getDateExpires());
            }
        }
    }
    


}
