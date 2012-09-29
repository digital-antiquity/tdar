package org.tdar.utils.sensorydata.enums;

public enum RegistrationFields {

    NAME_OF_REGISTERED_DATASET("Name of Registered Dataset"),
    GLOBAL_REGISTRATION_ERROR("Global Registration Error in units"),
    TOTAL_NUMBER_OF_POINTS("Total number of points in final registration");
    
    public final String label;
    
    private RegistrationFields(String label) {
        this.label = label;
    }
    
    
}
