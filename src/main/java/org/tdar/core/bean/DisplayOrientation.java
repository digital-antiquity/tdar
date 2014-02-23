package org.tdar.core.bean;


/**
 * Controls the display type for a collection, project, or any resource list.
 * @author abrin
 *
 */
public enum DisplayOrientation implements HasLabel {
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

    public void setLabel(String label) {
        this.label = label;
    }
}