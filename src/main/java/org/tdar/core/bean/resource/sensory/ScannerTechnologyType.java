package org.tdar.core.bean.resource.sensory;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;
public enum ScannerTechnologyType implements HasLabel {
    NONE(MessageHelper.getMessage("scannerTechnologyType.none")),
    TIME_OF_FLIGHT(MessageHelper.getMessage("scannerTechnologyType.none")),
    PHASE_BASED(MessageHelper.getMessage("scannerTechnologyType.phase_based")),
    TRIANGULATION(MessageHelper.getMessage("scannerTechnologyType.triangulation")),
    COMBINED(MessageHelper.getMessage("scannerTechnologyType.combined"));

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
    public String getLabel() {
        return label;
    }

    public static List<ScannerTechnologyType> activeValues() {
        return activeValues;
    }

}
