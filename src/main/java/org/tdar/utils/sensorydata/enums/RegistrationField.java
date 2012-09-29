package org.tdar.utils.sensorydata.enums;

import java.util.HashMap;
import java.util.Map;

public enum RegistrationField {

    NAME_OF_REGISTERED_DATASET("Name of Registered Dataset"),
    GLOBAL_REGISTRATION_ERROR("Global Registration Error in units"),
    TOTAL_NUMBER_OF_POINTS("Total number of points in final registration");
    
    public final String label;
    private static Map<String, RegistrationField> labelsToEnums;
    static {
        labelsToEnums = new HashMap<String, RegistrationField>();
        for(RegistrationField field : RegistrationField.values()) {
            labelsToEnums.put(field.label, field);
        }
    }    
    private RegistrationField(String label) {
        this.label = label;
    }
    
    public static RegistrationField fromLabel(String label) {
    	return labelsToEnums.get(label);
    }
    
}
