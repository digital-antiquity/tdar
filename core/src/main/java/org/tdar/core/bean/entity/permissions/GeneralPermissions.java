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
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
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
public enum GeneralPermissions implements HasLabel, Localizable {
    NONE(-1000),
    VIEW_ALL( 100, Resource.class, SharedCollection.class),
    MODIFY_METADATA(400, Resource.class, SharedCollection.class),
    MODIFY_RECORD(500, Resource.class, SharedCollection.class),
    ADD_TO_COLLECTION(40, ListCollection.class),
    REMOVE_FROM_COLLECTION(50, ListCollection.class),
    ADMINISTER_GROUP(80,ListCollection.class),
    ADD_TO_SHARE(4000,SharedCollection.class),
    REMOVE_FROM_SHARE(4500, SharedCollection.class),
    ADMINISTER_SHARE( 5000,SharedCollection.class),
    EDIT_ACCOUNT( 10000,BillingAccount.class),
    EDIT_INTEGRATION( 2000,DataIntegrationWorkflow.class);

    private Integer effectivePermissions;
    private List<Class<? extends Persistable>> contexts;

    GeneralPermissions(Integer effectivePermissions) {
        this.setEffectivePermissions(effectivePermissions);
    }

    GeneralPermissions(Integer effectivePermissions, Class<? extends Persistable> ... contexts) {
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

    public List<GeneralPermissions> getLesserAndEqualPermissions() {
        List<GeneralPermissions> permissions = new ArrayList<>();
        for (GeneralPermissions permission : GeneralPermissions.values()) {
            if (permission.getEffectivePermissions() <= getEffectivePermissions()) {
                permissions.add(permission);
            }
        }
        return permissions;
    }



    public static List<GeneralPermissions> resourcePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<>(Arrays.asList(GeneralPermissions.values()));
        permissions.remove(GeneralPermissions.ADMINISTER_GROUP);
        permissions.remove(GeneralPermissions.ADD_TO_COLLECTION);
        permissions.remove(GeneralPermissions.REMOVE_FROM_COLLECTION);
        permissions.remove(GeneralPermissions.ADMINISTER_SHARE);
        permissions.remove(GeneralPermissions.ADD_TO_SHARE);
        permissions.remove(GeneralPermissions.REMOVE_FROM_SHARE);
        permissions.remove(GeneralPermissions.EDIT_ACCOUNT);
        permissions.remove(GeneralPermissions.EDIT_INTEGRATION);
        return permissions;
    }

    public List<Class<? extends Persistable>> getContexts() {
        return contexts;
    }

    public void setContexts(List<Class<? extends Persistable>> contexts) {
        this.contexts = contexts;
    }

    public static <P extends Persistable> List<GeneralPermissions> getAvailablePermissionsFor(Class<P> persistableClass_) {
        Class<P> persistableClass = persistableClass_;
        if (persistableClass.equals(HierarchicalCollection.class)) {
            persistableClass = (Class<P>) SharedCollection.class;
        }

        List<GeneralPermissions> toReturn = new ArrayList<>();
        for (GeneralPermissions perm : GeneralPermissions.values()) {
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
        toReturn.remove(NONE);
        return toReturn;
    }

    public static GeneralPermissions getEditPermissionFor(HasAuthorizedUsers account) {
        if (account instanceof BillingAccount) {
            return GeneralPermissions.EDIT_ACCOUNT;
        }
        if (account instanceof DataIntegrationWorkflow) {
            return GeneralPermissions.EDIT_INTEGRATION;
        }
        if (account instanceof SharedCollection) {
            return GeneralPermissions.ADMINISTER_SHARE;
        }
        if (account instanceof ListCollection) {
            return GeneralPermissions.ADMINISTER_GROUP;
        }
        if (account instanceof Resource) {
            return GeneralPermissions.MODIFY_RECORD;
        }
        return null;
    }
}
