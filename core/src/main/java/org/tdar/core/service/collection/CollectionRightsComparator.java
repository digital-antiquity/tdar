package org.tdar.core.service.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.utils.PersistableUtils;

/**
 * This is a utility class to try and simplify collection rights comparisons
 * 
 * @author abrin
 *
 */
public class CollectionRightsComparator {

    private static final Logger logger = LoggerFactory.getLogger(CollectionRightsComparator.class);
    private Set<AuthorizedUser> currentUsers;
    private Set<AuthorizedUser> incomingUsers;

    private List<AuthorizedUser> additions = new ArrayList<>();
    private List<AuthorizedUser> deletions = new ArrayList<>();
    private List<AuthorizedUser> changes = new ArrayList<>();

    public CollectionRightsComparator(Set<AuthorizedUser> currentUsers, Collection<AuthorizedUser> incomingUsers) {
        this.currentUsers = new HashSet<>(currentUsers);
        this.incomingUsers = new HashSet<>(incomingUsers);
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

        //iterate through the incoming list
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

                if (Objects.equals(user.getUser().getId(),id)) {
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
            //if we're adding, insert into the map
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

}
