package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Entity
@Indexed
@Table(name = "sensory_data")
@XStreamAlias("sensoryData")
public class SensoryData extends InformationResource {

    /**
     * 
     */
    private static final long serialVersionUID = -568320714686809099L;
    
    /** toplevel metadata fields **/
    @Column(name="monument_number")
    private String monumentNumber;
    
    @Column(name="survey_location")
    private String surveyLocation; //FIXME: remove this field
    
    @Column(name="survey_date_begin")
    private Date surveyDateBegin;
    
    @Column(name="survey_date_end")
    private Date surveyDateEnd;
    
    @Column(name="survey_conditions")
    private String surveyConditions;
    
    @Column(name="scanner_details")
    private String scannerDetails;
    
    @Column(name="company_name")
    private String companyName;
    
    @Column(name="turntable_used", nullable = false)
    private boolean turntableUsed;
    
    @Column(name="rgb_data_capture_info")
    private String rgbDataCaptureInfo; 
    
    @Column(name="estimated_data_resolution")
    private Double estimatedDataResolution;
    
    @Column(name="total_scans_in_project")
    private Long totalScansInProject;
    
    @Column(name="final_dataset_description")
    private String finalDatasetDescription;
    
    @Column(name="additional_project_notes")
    private String additionalProjectNotes;
    
    @Column(name="planimetric_map_filename")
    private String planimetricMapFilename;

    @Column(name="control_data_filename")
    private String controlDataFilename;
    
    
    /** registration metadata **/ 
    //TODO: determine if this is actually one-to-many relationship.  the xls from angie suggests this, but only one registration record is present in any of the sampple projects
    @Column(name="registered_dataset_name")
    private String registeredDatasetName;

    @Column(name="scans_used")
    private Integer scansUsed;
    
    @Column(name="scans_total_acquired")
    private Integer scansTotalAcquired;
    
    @Column(name="registration_error_units")
    private Double registrationErrorUnits;
    
    @Column(name="final_registration_points")
    private Long finalRegistrationPoints;
    
    @Column(name="registration_method")
    private String registrationMethod;
    
    
    /** mesh metadata **/
    //premeshing metadata
    @Column(name="premesh_dataset_name")
    private String preMeshDatasetName;
    
    @Column(name="premesh_points")
    private Long preMeshPoints;
    
    @Column(name="premesh_overlap_reduction", nullable = false)
    private boolean premeshOverlapReduction;
    
    @Column(name="premesh_smoothing", nullable = false)
    private boolean premeshSmoothing;
    
    @Column(name="premesh_subsampling", nullable = false)
    private boolean premeshSubsampling;
    
    @Column(name="premesh_color_editions", nullable = false)
    private boolean premeshColorEditions;

    @Column(name="point_deletion_summary")
    private String pointDeletionSummary;
    
    
    //polygonal mesh metadata
    @Column(name="mesh_dataset_name")
    private String meshDatasetName;
    
    @Column(name="mesh_holes_filled", nullable = false)
    private boolean meshHolesFilled;
    
    @Column(name="mesh_smoothing", nullable = false)
    private boolean meshSmoothing;
    
    @Column(name="mesh_color_editions", nullable = false)
    private boolean meshColorEditions;
    
    @Column(name="mesh_healing_despiking", nullable = false)
    private boolean meshHealingDespiking;
    
    @Column(name="mesh_triangle_count")
    private Long meshTriangleCount;
    
    @Column(name="mesh_rgb_included", nullable = false)
    private boolean meshRgbIncluded;
    
    @Column(name="mesh_data_reduction", nullable = false)
    private boolean meshdataReduction;
  
    @Column(name="mesh_adjustment_matrix")
    private String meshAdjustmentMatrix;
    
    @Column(name="mesh_processing_notes")
    private String meshProcessingNotes;
    
    
    //decimated mesh metadata
    @Column(name="decimated_mesh_dataset")
    private String decimatedMeshDataset;

    @Column(name="decimated_mesh_original_triangle_count")
    private Long decimatedMeshOriginalTriangleCount;
    
    @Column(name="decimated_mesh_triangle_count")
    private Long decimatedMeshTriangleCount;
    
    @Column(name="rgb_preserved_from_original", nullable = false)
    private boolean rgbPreservedFromOriginal;
    
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy="resource", orphanRemoval = true)
    private List<SensoryDataScan> sensoryDataScans = new ArrayList<SensoryDataScan>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy="resource", orphanRemoval = true)
    private List<SensoryDataImage> sensoryDataImages = new ArrayList<SensoryDataImage>();
    

    public SensoryData() {
        setResourceType(ResourceType.SENSORY_DATA);
    }
    
    public String getMonumentNumber() {
        return monumentNumber;
    }


    public void setMonumentNumber(String monumentNumber) {
        this.monumentNumber = monumentNumber;
    }


    public String getSurveyLocation() {
        return surveyLocation;
    }


    public void setSurveyLocation(String surveyLocation) {
        this.surveyLocation = surveyLocation;
    }


    public String getSurveyConditions() {
        return surveyConditions;
    }


    public void setSurveyConditions(String surveyConditions) {
        this.surveyConditions = surveyConditions;
    }


    public String getScannerDetails() {
        return scannerDetails;
    }


    public void setScannerDetails(String scannerDetails) {
        this.scannerDetails = scannerDetails;
    }


    public String getCompanyName() {
        return companyName;
    }


    public boolean isTurntableUsed() {
        return turntableUsed;
    }


    public void setTurntableUsed(boolean isTurntableUsed) {
        this.turntableUsed = isTurntableUsed;
    }


    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }


    public String getRgbDataCaptureInfo() {
        return rgbDataCaptureInfo;
    }


    public void setRgbDataCaptureInfo(String rgbDataCaptureInfo) {
        this.rgbDataCaptureInfo = rgbDataCaptureInfo;
    }


    public Double getEstimatedDataResolution() {
        return estimatedDataResolution;
    }


    public void setEstimatedDataResolution(Double estimatedDataResolution) {
        this.estimatedDataResolution = estimatedDataResolution;
    }


    public Long getTotalScansInProject() {
        return totalScansInProject;
    }


    public void setTotalScansInProject(Long totalScansInProject) {
        this.totalScansInProject = totalScansInProject;
    }


    public String getFinalDatasetDescription() {
        return finalDatasetDescription;
    }


    public void setFinalDatasetDescription(String finalDatasetDescription) {
        this.finalDatasetDescription = finalDatasetDescription;
    }


    public String getAdditionalProjectNotes() {
        return additionalProjectNotes;
    }


    public void setAdditionalProjectNotes(String additionalProjectNotes) {
        this.additionalProjectNotes = additionalProjectNotes;
    }


    public String getRegisteredDatasetName() {
        return registeredDatasetName;
    }


    public void setRegisteredDatasetName(String registeredDatasetName) {
        this.registeredDatasetName = registeredDatasetName;
    }


    public Integer getScansUsed() {
        return scansUsed;
    }


    public void setScansUsed(Integer scansUsed) {
        this.scansUsed = scansUsed;
    }


    public Integer getScansTotalAcquired() {
        return scansTotalAcquired;
    }


    public void setScansTotalAcquired(Integer scansTotalAcquired) {
        this.scansTotalAcquired = scansTotalAcquired;
    }


    public Double getRegistrationErrorUnits() {
        return registrationErrorUnits;
    }


    public void setRegistrationErrorUnits(Double registrationErrorUnits) {
        this.registrationErrorUnits = registrationErrorUnits;
    }


    public Long getFinalRegistrationPoints() {
        return finalRegistrationPoints;
    }


    public void setFinalRegistrationPoints(Long finalRegistrationPoints) {
        this.finalRegistrationPoints = finalRegistrationPoints;
    }


    public String getPreMeshDatasetName() {
        return preMeshDatasetName;
    }


    public void setPreMeshDatasetName(String preMeshDatasetName) {
        this.preMeshDatasetName = preMeshDatasetName;
    }


    public Long getPreMeshPoints() {
        return preMeshPoints;
    }


    public void setPreMeshPoints(Long preMeshPoints) {
        this.preMeshPoints = preMeshPoints;
    }


    public String getMeshDatasetName() {
        return meshDatasetName;
    }


    public void setMeshDatasetName(String meshDatasetName) {
        this.meshDatasetName = meshDatasetName;
    }


    public Long getMeshTriangleCount() {
        return meshTriangleCount;
    }


    public void setMeshTriangleCount(Long meshTriangleCount) {
        this.meshTriangleCount = meshTriangleCount;
    }

    public String getMeshProcessingNotes() {
        return meshProcessingNotes;
    }


    public void setMeshProcessingNotes(String meshProcessingNotes) {
        this.meshProcessingNotes = meshProcessingNotes;
    }


    public String getDecimatedMeshDataset() {
        return decimatedMeshDataset;
    }


    public void setDecimatedMeshDataset(String decimatedMeshDataset) {
        this.decimatedMeshDataset = decimatedMeshDataset;
    }


    public Long getDecimatedMeshOriginalTriangleCount() {
        return decimatedMeshOriginalTriangleCount;
    }


    public void setDecimatedMeshOriginalTriangleCount(Long decimatedMeshOriginalTriangleCount) {
        this.decimatedMeshOriginalTriangleCount = decimatedMeshOriginalTriangleCount;
    }


    public Long getDecimatedMeshTriangleCount() {
        return decimatedMeshTriangleCount;
    }


    public void setDecimatedMeshTriangleCount(Long decimatedMeshTringleCount) {
        this.decimatedMeshTriangleCount = decimatedMeshTringleCount;
    }


    public Date getSurveyDateBegin() {
        return surveyDateBegin;
    }


    public void setSurveyDateBegin(Date surveyDate) {
        this.surveyDateBegin = surveyDate;
    }


    public Date getSurveyDateEnd() {
        return surveyDateEnd;
    }

    public void setSurveyDateEnd(Date surveyDateEnd) {
        this.surveyDateEnd = surveyDateEnd;
    }

    public boolean isPremeshOverlapReduction() {
        return premeshOverlapReduction;
    }


    public void setPremeshOverlapReduction(boolean premeshOverlapReduction) {
        this.premeshOverlapReduction = premeshOverlapReduction;
    }


    public boolean isPremeshSmoothing() {
        return premeshSmoothing;
    }


    public void setPremeshSmoothing(boolean premeshSmoothing) {
        this.premeshSmoothing = premeshSmoothing;
    }

    public boolean isMeshColorEditions() {
        return meshColorEditions;
    }


    public void setMeshColorEditions(boolean meshColorEditions) {
        this.meshColorEditions = meshColorEditions;
    }


    public boolean isMeshHealingDespiking() {
        return meshHealingDespiking;
    }


    public void setMeshHealingDespiking(boolean meshHealingDespiking) {
        this.meshHealingDespiking = meshHealingDespiking;
    }

    public boolean isPremeshSubsampling() {
        return premeshSubsampling;
    }


    public void setPremeshSubsampling(boolean premeshSubSampling) {
        this.premeshSubsampling = premeshSubSampling;
    }


    public boolean isPremeshColorEditions() {
        return premeshColorEditions;
    }


    public void setPremeshColorEditions(boolean premeshColorEditions) {
        this.premeshColorEditions = premeshColorEditions;
    }

    
    public String getPointDeletionSummary() {
        return pointDeletionSummary;
    }


    public void setPointDeletionSummary(String pointDeletionSummary) {
        this.pointDeletionSummary = pointDeletionSummary;
    }


    public boolean isMeshHolesFilled() {
        return meshHolesFilled;
    }


    public void setMeshHolesFilled(boolean meshHolesFilled) {
        this.meshHolesFilled = meshHolesFilled;
    }


    public boolean isMeshSmoothing() {
        return meshSmoothing;
    }


    public void setMeshSmoothing(boolean meshSmoothing) {
        this.meshSmoothing = meshSmoothing;
    }


    public boolean isMeshRgbIncluded() {
        return meshRgbIncluded;
    }


    public void setMeshRgbIncluded(boolean meshRgbIncluded) {
        this.meshRgbIncluded = meshRgbIncluded;
    }


    public boolean isMeshdataReduction() {
        return meshdataReduction;
    }


    public void setMeshdataReduction(boolean meshdataReduction) {
        this.meshdataReduction = meshdataReduction;
    }


    public String getMeshAdjustmentMatrix() {
        return meshAdjustmentMatrix;
    }


    public void setMeshAdjustmentMatrix(String meshAdjustmentMatrix) {
        this.meshAdjustmentMatrix = meshAdjustmentMatrix;
    }


    public boolean isRgbPreservedFromOriginal() {
        return rgbPreservedFromOriginal;
    }


    public void setRgbPreservedFromOriginal(boolean rgbPreservedFromOriginal) {
        this.rgbPreservedFromOriginal = rgbPreservedFromOriginal;
    }

    public List<SensoryDataScan> getSensoryDataScans() {
        return sensoryDataScans;
    }

    public void setSensoryDataScans(List<SensoryDataScan> sensoryDataScans) {
        this.sensoryDataScans = sensoryDataScans;
    }

    public List<SensoryDataImage> getSensoryDataImages() {
        return sensoryDataImages;
    }

    public void setSensoryDataImages(List<SensoryDataImage> sensoryDataImages) {
        this.sensoryDataImages = sensoryDataImages;
    }

	public String getPlanimetricMapFilename() {
		return planimetricMapFilename;
	}

	public void setPlanimetricMapFilename(String planimetricMapFilename) {
		this.planimetricMapFilename = planimetricMapFilename;
	}

	public String getControlDataFilename() {
		return controlDataFilename;
	}

	public void setControlDataFilename(String controlDataFilename) {
		this.controlDataFilename = controlDataFilename;
	}

    public String getRegistrationMethod() {
        return registrationMethod;
    }

    public void setRegistrationMethod(String registrationMethod) {
        this.registrationMethod = registrationMethod;
    }


}
