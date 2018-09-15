/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.entity.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.MessageHelper;

/**
 * @author Adam Brin
 *         The actual, and effective permissions between a user and a resource or collection. Each permission has a name, a class association, and a numeric
 *         equivalent. The numerical equivalent is additive, hence someone with a 500 level permission can do a 100 level action. The numeric permissions should
 *         be faster to query / index in the database
 */
@SuppressWarnings("unchecked")
public enum Permissions implements HasLabel, Localizable {
    NONE(-1000),
    VIEW_ALL(100, Resource.class, ResourceCollection.class),
    MODIFY_METADATA(400, Resource.class, ResourceCollection.class),
    MODIFY_RECORD(500,
            Resource.class, ResourceCollection.class),
    ADD_TO_COLLECTION(4000, ResourceCollection.class),
    REMOVE_FROM_COLLECTION(4500,
            ResourceCollection.class),
    ADMINISTER_COLLECTION(5000,
            ResourceCollection.class),
    USE_ACCOUNT(10_000, BillingAccount.class),
    ADMINISTER_ACCOUNT(20_000, BillingAccount.class),
    EDIT_INTEGRATION(2000, DataIntegrationWorkflow.class);

    private Integer effectivePermissions;
    private List<Class<? extends Persistable>> contexts;

    Permissions(Integer effectivePermissions) {
        this.setEffectivePermissions(effectivePermissions);
    }

    Permissions(Integer effectivePermissions, Class<? extends Persistable>... contexts) {
        this.setEffectivePermissions(effectivePermissions);
        this.setContexts(Arrays.asList(contexts));
    }

    /**
     * @return the label
     */
    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    /**
     * @param effectivePermissions
     *            the effectivePermissions to set
     */
    private void setEffectivePermissions(Integer effectivePermissions) {
        this.effectivePermissions = effectivePermissions;
    }

    /**
     * @return the effectivePermissions
     */
    public Integer getEffectivePermissions() {
        return effectivePermissions;
    }

    public List<Permissions> getLesserAndEqualPermissions() {
        List<Permissions> permissions = new ArrayList<>();
        for (Permissions permission : Permissions.values()) {
            if (permission.getEffectivePermissions() <= getEffectivePermissions()) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public boolean matches(Permissions p) {
        if (p == null) {
            return false;
        }

        if (p.getEffectivePermissions() > getEffectivePermissions()) {
            return true;
        }

        return false;
    }

    public static List<Permissions> resourcePermissions() {
        List<Permissions> permissions = new ArrayList<>(Arrays.asList(Permissions.values()));
        // permissions.remove(GeneralPermissions.ADD_TO_COLLECTION);
        // permissions.remove(GeneralPermissions.REMOVE_FROM_COLLECTION);
        permissions.remove(Permissions.ADMINISTER_COLLECTION);
        permissions.remove(Permissions.ADD_TO_COLLECTION);
        permissions.remove(Permissions.REMOVE_FROM_COLLECTION);
        permissions.remove(Permissions.USE_ACCOUNT);
        permissions.remove(Permissions.ADMINISTER_ACCOUNT);
        permissions.remove(Permissions.EDIT_INTEGRATION);
        permissions.remove(Permissions.NONE);
        return permissions;
    }

    public List<Class<? extends Persistable>> getContexts() {
        return contexts;
    }

    private void setContexts(List<Class<? extends Persistable>> contexts) {
        this.contexts = contexts;
    }

    public static <P extends Persistable> List<Permissions> getAvailablePermissionsFor(Class<P> persistableClass_) {
        Class<P> persistableClass = persistableClass_;

        List<Permissions> toReturn = new ArrayList<>();
        for (Permissions perm : Permissions.values()) {
            if (perm == NONE) {
                continue;
            }
            if (CollectionUtils.isEmpty(perm.getContexts())) {
                toReturn.add(perm);
                continue;
            }
            for (Class<? extends Persistable> cls : perm.getContexts()) {
                if (ClassUtils.isAssignable(persistableClass, cls)) {
                    toReturn.add(perm);
                    continue;
                }
            }
        }

        return toReturn;
    }

    public static Permissions getEditPermissionFor(HasAuthorizedUsers account) {
        if (account instanceof BillingAccount) {
            return Permissions.ADMINISTER_ACCOUNT;
        }
        if (account instanceof DataIntegrationWorkflow) {
            return Permissions.EDIT_INTEGRATION;
        }
        if (account instanceof ResourceCollection) {
            return Permissions.ADMINISTER_COLLECTION;
        }
        if (account instanceof Resource) {
            return Permissions.MODIFY_RECORD;
        }
        return null;
    }
}
