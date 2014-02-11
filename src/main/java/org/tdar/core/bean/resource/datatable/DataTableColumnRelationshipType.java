package org.tdar.core.bean.resource.datatable;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum DataTableColumnRelationshipType implements HasLabel {

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

    public void setLabel(String label) {
        this.label = label;
    }

}
