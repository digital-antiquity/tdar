package org.tdar.core.bean.resource.sensory;

import java.util.HashMap;
import java.util.Map;

public enum ScannerTechnologyType {
    //Time of Flight, Phase-based, Triangulation
    TIME_OF_FLIGHT("Time of Flight"),
    PHASE_BASED("Phase-based"),
    TRIANGULATION("Triangulation");

    private final String label;
    
    private static Map<String, ScannerTechnologyType> labelsToEnums = new HashMap<String, ScannerTechnologyType>();
    static {
        for(ScannerTechnologyType type : ScannerTechnologyType.values()) {
            labelsToEnums.put(type.label.toLowerCase(), type);
        }
        
        //hack: adding some values that might appear in  ads templates. helpful for importing.
        labelsToEnums.put("tele", TRIANGULATION);
        labelsToEnums.put("tof", TIME_OF_FLIGHT);
        labelsToEnums.put("phase", PHASE_BASED);
    }
    
    private ScannerTechnologyType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static ScannerTechnologyType fromLabel(String label) {
        return labelsToEnums.get(label.toLowerCase());
    }
    

}
