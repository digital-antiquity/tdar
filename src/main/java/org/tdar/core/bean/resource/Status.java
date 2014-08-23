package org.tdar.core.bean.resource;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Status for resources within tDAR, controls permissions and how they're indexed.
 * 
 * @author abrin
 * 
 */
public enum Status implements HasLabel, Localizable {
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
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public boolean isFlaggedForBilling() {
        return equals(FLAGGED_ACCOUNT_BALANCE);
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

    public boolean isDraft() {
        return this == DRAFT;
    }
}
