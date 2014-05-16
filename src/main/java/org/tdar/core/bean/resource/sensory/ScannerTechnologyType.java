package org.tdar.core.bean.resource.sensory;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * The type of scan controlls the type of metadata that we display
 * 
 * @author abrin
 * 
 */
public enum ScannerTechnologyType implements HasLabel, Localizable {
    NONE("Not Specified"),
    TIME_OF_FLIGHT("Time of Flight"),
    PHASE_BASED("Phase-based"),
    TRIANGULATION("Triangulation"),
    COMBINED("Combined");

    private final String label;
    private final boolean active;

    private static List<ScannerTechnologyType> activeValues = new ArrayList<>();
    static {
        for (ScannerTechnologyType val : ScannerTechnologyType.values()) {
            if (val.active) {
                activeValues.add(val);
            }
        }
    }

    private ScannerTechnologyType(String label) {
        this(label, true);
    }

    // use this initializer if we ever want to 'deprecate' enum values from picklists but retain them for legacy entries
    private ScannerTechnologyType(String label, boolean active) {
        this.label = label;
        this.active = active;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static List<ScannerTechnologyType> activeValues() {
        return activeValues;
    }

}
