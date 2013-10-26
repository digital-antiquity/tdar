package org.tdar.core.bean.resource;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.HasLabel;

public enum Status implements HasLabel {
    DRAFT("Draft"),
    ACTIVE("Active"),
    FLAGGED("Flagged"),
    FLAGGED_ACCOUNT_BALANCE("Flagged: Overage"),
    DUPLICATE("Duplicate"),
    DELETED("Deleted");

    private final String label;

    private Status(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static Status fromString(String string) {
        if (StringUtils.isBlank(string)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to ResourceType enum.. unfortunately valueOf only throws RuntimeExceptions.
        try {
            return Status.valueOf(string);
        } catch (Exception exception) {
            return null;
        }
    }

}
