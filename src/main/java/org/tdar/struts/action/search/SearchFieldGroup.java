package org.tdar.struts.action.search;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum SearchFieldGroup implements HasLabel {

    BASIC_FIELDS(MessageHelper.getMessage("searchFieldGroup.basic_fields")),
    CONTROLLED_KEYWORDS(MessageHelper.getMessage("searchFieldGroup.controlled_keywords")),
    FREEFORM_KEYWORDS(MessageHelper.getMessage("searchFieldGroup.freeform_keywords")),
    EXPLORE(MessageHelper.getMessage("searchFieldGroup.explore"));

    private String label;

    private SearchFieldGroup(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
