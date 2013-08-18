package org.tdar.core.bean;

public enum DisplayOrientation implements HasLabel {
    LIST("List"),
    LIST_FULL("List (Full)"),
    GRID("Grid"),
    MAP("Map");

    private String label;

    private DisplayOrientation(String label) {
        this.setLabel(label);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}