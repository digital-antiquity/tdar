package org.tdar.core.bean;

import org.tdar.utils.MessageHelper;

public enum DisplayOrientation implements HasLabel {
    LIST(MessageHelper.getMessage("displayOrientation.list")),
    LIST_FULL(MessageHelper.getMessage("displayOrientation.list_full")),
    GRID(MessageHelper.getMessage("displayOrientation.grid")),
    MAP(MessageHelper.getMessage("displayOrientation.map"));

    private String label;

    private DisplayOrientation(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}