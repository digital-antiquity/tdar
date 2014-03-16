package org.tdar.core.dao.entity;

import java.util.Collection;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

public class UserPermissionCacheKey {

    public enum CacheResult {
        TRUE,
        FALSE,
        NOT_FOUND;
        
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

    public UserPermissionCacheKey(Person person, GeneralPermissions permission, Collection<Long> collectionIds) {
        HashCodeBuilder hcb = new HashCodeBuilder(39, 5);
        this.key = hcb.append(person.getId()).append(permission).append(collectionIds).toHashCode();
    }

    public int getKey() {
        return key;
    }

}
