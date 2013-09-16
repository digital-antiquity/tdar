package org.tdar.struts.action.search;

import org.tdar.core.bean.HasLabel;

public enum SearchFieldGroup implements HasLabel {

    BASIC_FIELDS("Basic Fields"),
    CONTROLLED_KEYWORDS("Controlled Keywords"),
    FREEFORM_KEYWORDS("Freeform Keywords"),
    EXPLORE("Explore the site");

    private String label;

    private SearchFieldGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
