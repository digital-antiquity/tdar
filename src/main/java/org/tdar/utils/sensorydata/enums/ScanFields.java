package org.tdar.utils.sensorydata.enums;

public enum ScanFields {
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
    
    private ScanFields(String label) {
        this.label = label;
    }
    
}
