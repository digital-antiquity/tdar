package org.tdar.utils.sensorydata.enums;

import java.util.HashMap;
import java.util.Map;

import org.tdar.core.bean.HasLabel;

public enum ScanField implements HasLabel {
    SCAN_FILENAME("Scan Filename")
    ,SCAN_TRANSFORMATION_MATRIX("Scan Transformation Matrix")
    ,MATRIX_APPLIED_TO_SCANS("Matrix Applied to Scans?")
    ,NAME_OF_MONUMENT("Name of monument/object area")
    ,SURVEY_DATE("Survey Date")
    ,NUMBER_OF_POINTS_IN_SCAN("Number of Points in Scan")
    ,ADDITIONAL_SCAN_NOTES("Additional Scan Notes")
    ,SCANNER_TECHNOLOGY("Scanner Technology")
    ,DATA_RESOLUTION("Data Resolution")
    ,LENSE_OR_FOV_DETAILS("Lense or FOV Details (Triangulation scans only)");
    
    public final String label;
    
    //apparently I can do this (http://download.oracle.com/javase/tutorial/java/javaOO/initial.html)
    private static Map<String, ScanField> labelsToEnums;
    static {
        labelsToEnums = new HashMap<String, ScanField>();
        for(ScanField field : ScanField.values()) {
            labelsToEnums.put(field.label, field);
        }
    }
    
    private ScanField(String label) {
        this.label = label;
    }
    
    public static ScanField fromLabel(String label) {
        return labelsToEnums.get(label);
    }

    public String getLabel() {
        return label;
    }
    
}
