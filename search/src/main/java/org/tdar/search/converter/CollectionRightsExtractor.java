package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.PersistableUtils;

public class CollectionRightsExtractor {

    private ResourceCollection collection;

    public CollectionRightsExtractor(ResourceCollection collection) {
        this.collection = collection;
    }
    
    /*
     * Convenience Method that provides a list of users that match the permission
     */
    public static Set<TdarUser> getUsersWhoCan(ResourceCollection collection_, GeneralPermissions permission, boolean recurse) {
        Set<TdarUser> people = new HashSet<>();
        for (AuthorizedUser user : collection_.getAuthorizedUsers()) {
            if (user.getEffectiveGeneralPermission() >= permission.getEffectivePermissions()) {
                people.add(user.getUser());
            }
        }
        if (collection_ instanceof SharedCollection) {
            SharedCollection shared = (SharedCollection)collection_;
            if ((shared.getParent() != null) && recurse) {
            people.addAll(getUsersWhoCan(shared.getParent(), permission, recurse));
        }
        }
        return people;
    }

    

    /*
     * used for populating the Lucene Index with users that have appropriate rights to modify things in the collection
     */
    public List<Long> getUsersWhoCanModify() {
        return toUserList(GeneralPermissions.MODIFY_RECORD);
    }

    private List<Long> toUserList(GeneralPermissions permission) {
        ArrayList<Long> users = new ArrayList<>();
        HashSet<TdarUser> writable = new HashSet<>();
        writable.add(collection.getOwner());
        writable.addAll(getUsersWhoCan(collection, permission, true));
        for (TdarUser p : writable) {
            if (PersistableUtils.isNullOrTransient(p)) {
                continue;
            }
            users.add(p.getId());
        }
        return users;
    }

    public List<Long> getUsersWhoCanAdminister() {
        if (collection instanceof ListCollection) {
            return toUserList(GeneralPermissions.ADMINISTER_GROUP);
        } 
        return toUserList(GeneralPermissions.ADMINISTER_SHARE);
    }

    public List<Long> getUsersWhoCanView() {
        return toUserList(GeneralPermissions.VIEW_ALL);
    }
}
