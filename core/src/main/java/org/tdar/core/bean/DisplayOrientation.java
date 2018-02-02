package org.tdar.core.bean;

import java.util.Arrays;
import java.util.List;

import org.tdar.utils.MessageHelper;

/**
 * Controls the display type for a collection, project, or any resource list.
 * 
 * @author abrin
 * 
 */
public enum DisplayOrientation implements HasLabel, Localizable {
    LIST("List"), LIST_FULL("List (Full)"), GRID("Grid"), MAP("Map");

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

    public static List<DisplayOrientation> getOrientationsFor(String string) {
        // weird bi-directional dependency for LookupSource
        switch (string) {
            case "COLLECTION":
                return getCommonOrientations();
            default:
                return Arrays.asList(values());
        }
    }

    public static List<DisplayOrientation> getCommonOrientations() {
        return Arrays.asList(LIST, LIST_FULL, GRID);
    }

    public String getSvg() {
        switch (this) {
            case GRID:
                return "gallery";
            case LIST:
            case LIST_FULL:
                return "list";
            case MAP:
                return "map";
        }
        return "";
    }
}