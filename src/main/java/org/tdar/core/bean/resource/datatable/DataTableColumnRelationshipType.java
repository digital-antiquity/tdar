package org.tdar.core.bean.resource.datatable;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum DataTableColumnRelationshipType implements HasLabel {

    /*
     * NB these were not really types of relationship
     * FOREIGN_KEY("Foreign Key"),
     * PRIMARY_KEY("Primary Key");
     */
    ONE_TO_MANY(MessageHelper.getMessage("dataTableColumnRelationshipType.one_to_many")),
    MANY_TO_ONE(MessageHelper.getMessage("dataTableColumnRelationshipType.many_to_many")),
    ONE_TO_ONE(MessageHelper.getMessage("dataTableColumnRelationshipType.one_to_one"));

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
