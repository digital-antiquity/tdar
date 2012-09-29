package org.tdar.utils.sensorydata.enums;

import java.util.HashMap;
import java.util.Map;

import org.tdar.utils.sensorydata.AdsImportException;

public enum MeshField {

	PREMESH_NAME_OF_PREMESH_DATASET("Name of Pre-Mesh Dataset")
	,PREMESH_NUMBER_OF_POINTS_IN_FILE("Number of Points in File")
	,PREMESH_OVERLAP_REDUCTION("Overlap Reduction")
	,PREMESH_SMOOTHING("Smoothing")
	,PREMESH_SUBSAMPLING("Subsampling")
	,PREMESH_COLOR_EDITIONS("Color Editions")
	,PREMESH_POINT_DELETION_SUMMARY("Point Deletion Summary")
	,MESH_NAME_OF_MESH_DATASET("Name of Mesh Dataset")
	,MESH_HOLES_FILLED("Holes Filled")
	,MESH_SMOOTHING("Smoothing")
	,MESH_COLOR_EDITIONS("Color Editions")
	,MESH_HEALING_DESPIKING("Healing/despiking")
	,MESH_TOTAL_TRIANGLE_COUNT("Total Triangle Count (post editing, predecimation)")
	,MESH_RGB_COLOR_INCLUDED("RGB Color Included")
	,MESH_DATA_REDUCTION("Data Reduction")
	,MESH_COORDINATE_SYSTEM_ADJUSTMENT("Coordinate System Adjustment")
	,MESH_CS_ADJUSTMENT_MATRIX("CS Adjustment Matrix ")
	,MESH_ADDITIONAL_PROCESSING_NOTES("Additional processing notes")
	,DECIMATED_NAME_OF_DECIMATED_MESH_DATASET("Name of Decimated Mesh Dataset")
	,DECIMATED_TOTAL_ORIGINAL_TRIANGLE_COUNT("Total Original Triangle Count")
	,DECIMATED_DECIMATED_TRIANGLE_COUNT("Decimated Triangle Count")
	,DECIMATED_RGB_COLOR_PRESERVED_FROM_ORIGINAL_DATASET("RGB Color Preserved from original dataset");	
	
    public final String label;
    
    private static Map<String, MeshField> labelsToEnums;
    static {
        labelsToEnums = new HashMap<String, MeshField>();
        for(MeshField field : MeshField.values()) {
            labelsToEnums.put(field.label, field);
        }
    }
    
    private MeshField(String label) {
        this.label = label;
    }
    
    public static MeshField fromLabel(String label) {
        MeshField field = labelsToEnums.get(label);
//        if(field==null) throw new AdsImportException("Label not recognized:" + label);
    	return labelsToEnums.get(label);
    }
	
	
}
