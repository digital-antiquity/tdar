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
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.MessageHelper;

/**
 * @author Adam Brin
 *         The actual, and effective permissions between a user and a resource or collection. Each permission has a name, a class association, and a numeric
 *         equivalent. The numerical equivalent is additive, hence someone with a 500 level permission can do a 100 level action. The numeric permissions should
 *         be faster to query / index in the database
 */
public enum GeneralPermissions implements HasLabel, Localizable {
    VIEW_ALL("View and Download", 100, Resource.class, SharedCollection.class),
    MODIFY_METADATA("Modify Metadata", 400, Resource.class, SharedCollection.class),
    MODIFY_RECORD("Modify Files & Metadata", 500, Resource.class, SharedCollection.class),
    ADD_TO_COLLECTION("Add to Collection", 40, ListCollection.class),
    REMOVE_FROM_COLLECTION("Remove from Collection", 50, ListCollection.class),
    ADMINISTER_GROUP("Add/Remove Items from Collection", 80,ListCollection.class),
    ADD_TO_SHARE("Add to Share", 4000,SharedCollection.class),
    REMOVE_FROM_SHARE("Remove from Share", 4500, SharedCollection.class),
    ADMINISTER_SHARE("Add/Remove Items from Share", 5000,SharedCollection.class);

    private Integer effectivePermissions;
    private String label;
    private List<Class<? extends Persistable>> contexts;

    GeneralPermissions(String label, Integer effectivePermissions) {
        this.setLabel(label);
        this.setEffectivePermissions(effectivePermissions);
    }

    GeneralPermissions(String label, Integer effectivePermissions, Class<? extends Persistable> ... contexts) {
        this.setLabel(label);
        this.setEffectivePermissions(effectivePermissions);
        this.setContexts(Arrays.asList(contexts));
    }

    /**
     * @param label
     *            the label to set
     */
    private void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
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
        return permissions;
    }

    public List<Class<? extends Persistable>> getContexts() {
        return contexts;
    }

    public void setContexts(List<Class<? extends Persistable>> contexts) {
        this.contexts = contexts;
    }

    public static <P extends Persistable> List<GeneralPermissions> getAvailablePermissionsFor(Class<P> persistableClass) {
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
        return toReturn;
    }
}
