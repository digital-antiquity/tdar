/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.entity.permissions;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;

/**
 * @author Adam Brin
 *         The actual, and effective permissions between a user and a resource or collection. Each permission has a name, a class association, and a numeric
 *         equivalent. The numerical equivalent is additive, hence someone with a 500 level permission can do a 100 level action. The numeric permissions should
 *         be faster to query / index in the database
 */
public enum GeneralPermissions implements HasLabel {
    VIEW_ALL("View All", 100),
    MODIFY_RECORD("Modify Record", 500),
    ADMINISTER_GROUP("Administer Collection", ResourceCollection.class, 5000);

    private Integer effectivePermissions;
    private String label;
    private Class<? extends Persistable> context;

    private GeneralPermissions(String label, Integer effectivePermissions) {
        this.setLabel(label);
        this.setEffectivePermissions(effectivePermissions);
    }

    private GeneralPermissions(String label, Class<? extends Persistable> context, Integer effectivePermissions) {
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
    public String getLabel() {
        return label;
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
    public void setContext(Class<? extends Persistable> context) {
        this.context = context;
    }

    /**
     * @return the context
     */
    public Class<? extends Persistable> getContext() {
        return context;
    }
}
