package org.tdar.core.dao.entity;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.Permissions;

public class UserPermissionCacheKey {

    public enum CacheResult {
        TRUE, FALSE, NOT_FOUND;

        public Boolean getBooleanEquivalent() {
            switch (this) {
                case TRUE:
                    return Boolean.TRUE;
                case FALSE:
                    return Boolean.FALSE;
                default:
                    return null;
            }
        }
    }

    private int key;

    public UserPermissionCacheKey(Person person, Permissions permission, Collection<Long> collectionIds) {
        HashCodeBuilder hcb = new HashCodeBuilder(39, 5);
        // forcing to HashSet so we can deal with sets or lists being sent in
        this.key = hcb.append(person.getId()).append(permission).append(new HashSet<Long>(collectionIds)).toHashCode();
    }

    public int getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return key == obj.hashCode();
    }

}
