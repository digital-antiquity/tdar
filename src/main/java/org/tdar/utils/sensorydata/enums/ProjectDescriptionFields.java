package org.tdar.utils.sensorydata.enums;

public enum ProjectDescriptionFields {
    PROJECT_NAME("Project Name")
    ,NAME_OF_MONUMENT_SURVEY_AREA_OR_OBJECT("Name of monument, survey area, or object")
    ,MONUMENT_OBJECT_NUMBER("Monument/Object Number")
    ,MONUMENT_OBJECT_DESCRIPTION("Monument/Object Description")
    ,SURVEY_LOCATION("Survey Location")
    ,SURVEY_DATES("Survey Date(s)")
    ,SURVEY_CONDITIONS("Survey Conditions")
    ,SCANNER_DETAILS("Scanner Details")
    ,COMPANY_OPERATOR_NAME("Company/Operator Name")
    ,CONTROL_DATA_COLLECTED("Control data collected?")
    ,TURNTABLE_USED("Turntable used?")
    ,RGB_DATA_CAPTURE("RGB data capture?")
    ,ESTIMATED_DATA_RESOLUTION("Estimated Data Resolution")
    ,TOTAL_NUMBER_OF_SCANS_IN_PROJECT("Total Number of Scans in Project")
    ,DESCRIPTION_OF_FINAL_DATASETS_FOR_ARCHIVE("Description of final datasets for archive")
    ,PLANIMETRIC_MAP_OF_SCAN_COVERAGE_AREAS("Planimetric map of scan coverage areas")
    ,ADDITIONAL_PROJECT_NOTES("Additional project notes")
    ,IMAGES_FROM_SURVEY("Images from survey");
    
    public final String label;
    
    private ProjectDescriptionFields(String label) {
        this.label = label;
    }
    
}
