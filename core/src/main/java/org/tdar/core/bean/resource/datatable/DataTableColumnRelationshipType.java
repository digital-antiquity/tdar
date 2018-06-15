package org.tdar.core.bean.resource.datatable;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

public enum DataTableColumnRelationshipType implements HasLabel, Localizable {

    /**
     * Maps database relationship types
     * 
     * NB these were not really types of relationship
     * FOREIGN_KEY("Foreign Key"),
     * PRIMARY_KEY("Primary Key");
     */
    ONE_TO_MANY("One-to-Many"),
    MANY_TO_ONE("Many-to-One"),
    ONE_TO_ONE("One-to-One");

    private String label;

    private DataTableColumnRelationshipType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
