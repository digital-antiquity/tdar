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

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.utils.MessageHelper;

/**
 * @author Adam Brin
 *         The actual, and effective permissions between a user and a resource or collection. Each permission has a name, a class association, and a numeric
 *         equivalent. The numerical equivalent is additive, hence someone with a 500 level permission can do a 100 level action. The numeric permissions should
 *         be faster to query / index in the database
 */
public enum GeneralPermissions implements HasLabel, Localizable {
    VIEW_ALL("View and Download", 100),
    MODIFY_METADATA("Modify Metadata", 400),
    MODIFY_RECORD("Modify Files & Metadata", 500),
    ADD_TO_COLLECTION("Add to Collection", ResourceCollection.class,4000),
    REMOVE_FROM_COLLECTION("Remove from Collection", ResourceCollection.class,4500),
    ADMINISTER_GROUP("Add/Remove Items from Collection", ResourceCollection.class, 5000);

    private Integer effectivePermissions;
    private String label;
    private Class<? extends Persistable> context;

    GeneralPermissions(String label, Integer effectivePermissions) {
        this.setLabel(label);
        this.setEffectivePermissions(effectivePermissions);
    }

    GeneralPermissions(String label, Class<? extends Persistable> context, Integer effectivePermissions) {
        this.setLabel(label);
        this.setEffectivePermissions(effectivePermissions);
        this.setContext(context);
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

    /**
     * @param context
     *            the context to set
     */
    private void setContext(Class<? extends Persistable> context) {
        this.context = context;
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

    /**
     * @return the context
     */
    public Class<? extends Persistable> getContext() {
        return context;
    }

    public static List<GeneralPermissions> resourcePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<>(Arrays.asList(GeneralPermissions.values()));
        permissions.remove(GeneralPermissions.ADMINISTER_GROUP);
        permissions.remove(GeneralPermissions.ADD_TO_COLLECTION);
        permissions.remove(GeneralPermissions.REMOVE_FROM_COLLECTION);
        return permissions;
    }
}
