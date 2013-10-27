package org.tdar.core.bean.resource;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum Status implements HasLabel {
    DRAFT(MessageHelper.getMessage("status.draft")),
    ACTIVE(MessageHelper.getMessage("status.active")),
    FLAGGED(MessageHelper.getMessage("status.flagged")),
    FLAGGED_ACCOUNT_BALANCE(MessageHelper.getMessage("status.flagged_account")),
    DUPLICATE(MessageHelper.getMessage("status.duplicate")),
    DELETED(MessageHelper.getMessage("status.deleted"));

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
