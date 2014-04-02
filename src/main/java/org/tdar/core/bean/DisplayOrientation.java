package org.tdar.core.bean;

import org.tdar.utils.MessageHelper;

/**
 * Controls the display type for a collection, project, or any resource list.
 * 
 * @author abrin
 * 
 */
public enum DisplayOrientation implements HasLabel, Localizable {
    LIST("List"),
    LIST_FULL("List (Full)"),
    GRID("Grid"),
    MAP("Map");

    private String label;

    private DisplayOrientation(String label) {
        this.setLabel(label);
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