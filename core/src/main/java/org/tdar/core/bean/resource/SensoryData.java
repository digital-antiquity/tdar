package org.tdar.core.bean.resource;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.resource.sensory.ScannerTechnologyType;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;

@Entity
// @Indexed
@Table(name = "sensory_data")
@XmlRootElement(name = "sensoryData")
/**
 * Represnts a 3-D scan or sensory-data scan of an object.
 * 
 * @author abrin
 *
 */
public class SensoryData extends Dataset {

    private static final long serialVersionUID = -568320714686809099L;

    /** toplevel metadata fields **/
    @Column(name = "monument_number")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String monumentNumber;

    @Column(name = "survey_location")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String surveyLocation; // FIXME: remove this field

    @Column(name = "survey_date_begin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date surveyDateBegin;

    @Column(name = "survey_date_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date surveyDateEnd;

    @Column(name = "survey_conditions")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String surveyConditions;

    @Column(name = "scanner_details")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String scannerDetails;

    @Column(name = "company_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String companyName;

    @Column(name = "turntable_used", nullable = false)
    private boolean turntableUsed;

    @Column(name = "rgb_data_capture_info")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String rgbDataCaptureInfo;

    @Column(name = "estimated_data_resolution", length = FieldLength.FIELD_LENGTH_254)
    private String estimatedDataResolution;

    @Column(name = "total_scans_in_project")
    private Long totalScansInProject;

    @Column(name = "final_dataset_description")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String finalDatasetDescription;

    @Column(name = "additional_project_notes")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String additionalProjectNotes;

    @Column(name = "planimetric_map_filename")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String planimetricMapFilename;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @Column(name = "control_data_filename")
    private String controlDataFilename;

    public enum RgbCapture {
        NA("None"), INTERNAL("Internal"), EXTERNAL("External");
        String label;

        RgbCapture(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Column(name = "rgb_capture", length = FieldLength.FIELD_LENGTH_255)
    @Enumerated(EnumType.STRING)
    private RgbCapture rgbCapture;

    /** registration metadata **/
    // TODO: determine if this is actually one-to-many relationship. the xls from angie suggests this, but only one registration record is present in any of the
    // sampple projects
    @Column(name = "registered_dataset_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String registeredDatasetName;

    @Column(name = "scans_used")
    private Integer scansUsed;

    @Column(name = "scans_total_acquired")
    private Integer scansTotalAcquired;

    @Column(name = "registration_error_units")
    private Double registrationErrorUnits;

    @Column(name = "final_registration_points")
    private Long finalRegistrationPoints;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @Column(name = "registration_method")
    private String registrationMethod;

    /** mesh metadata **/
    // premeshing metadata
    @Column(name = "premesh_dataset_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String preMeshDatasetName;

    @Column(name = "premesh_points")
    private Long preMeshPoints;

    @Column(name = "premesh_overlap_reduction", nullable = false)
    private boolean premeshOverlapReduction;

    @Column(name = "premesh_smoothing", nullable = false)
    private boolean premeshSmoothing;

    @Column(name = "premesh_subsampling", nullable = false)
    private boolean premeshSubsampling;

    @Column(name = "premesh_color_editions", nullable = false)
    private boolean premeshColorEditions;

    @Column(name = "point_deletion_summary")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String pointDeletionSummary;

    // polygonal mesh metadata
    @Length(max = FieldLength.FIELD_LENGTH_255)
    @Column(name = "mesh_dataset_name")
    private String meshDatasetName;

    @Column(name = "mesh_holes_filled", nullable = false)
    private boolean meshHolesFilled;

    @Column(name = "mesh_smoothing", nullable = false)
    private boolean meshSmoothing;

    @Column(name = "mesh_color_editions", nullable = false)
    private boolean meshColorEditions;

    @Column(name = "mesh_healing_despiking", nullable = false)
    private boolean meshHealingDespiking;

    @Column(name = "mesh_triangle_count")
    private Long meshTriangleCount;

    @Column(name = "mesh_rgb_included", nullable = false)
    private boolean meshRgbIncluded;

    @Column(name = "mesh_data_reduction", nullable = false)
    private boolean meshdataReduction;

    @Column(name = "mesh_adjustment_matrix")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String meshAdjustmentMatrix;

    @Column(name = "mesh_processing_notes")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String meshProcessingNotes;

    // decimated mesh metadata
    @Column(name = "decimated_mesh_dataset")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String decimatedMeshDataset;

    @Column(name = "decimated_mesh_original_triangle_count")
    private Long decimatedMeshOriginalTriangleCount;

    @Column(name = "decimated_mesh_triangle_count")
    private Long decimatedMeshTriangleCount;

    @Column(name = "rgb_preserved_from_original", nullable = false)
    private boolean rgbPreservedFromOriginal;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "sensory_data_id")
    private Set<SensoryDataScan> sensoryDataScans = new LinkedHashSet<SensoryDataScan>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "sensory_data_id")
    private Set<SensoryDataImage> sensoryDataImages = new LinkedHashSet<SensoryDataImage>();

    @Column(name = "scanner_technology", length = FieldLength.FIELD_LENGTH_255)
    @Enumerated(EnumType.STRING)
    private ScannerTechnologyType scannerTechnology;

    @Column(name = "camera_details", length = FieldLength.FIELD_LENGTH_255)
    private String cameraDetails;

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

    public String getEstimatedDataResolution() {
        return estimatedDataResolution;
    }

    public void setEstimatedDataResolution(String estimatedDataResolution) {
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

    @XmlElementWrapper(name = "sensoryDataScans")
    @XmlElement(name = "sensoryDataScan")
    public Set<SensoryDataScan> getSensoryDataScans() {
        return sensoryDataScans;
    }

    public void setSensoryDataScans(LinkedHashSet<SensoryDataScan> sensoryDataScans) {
        this.sensoryDataScans = sensoryDataScans;
    }

    @XmlElementWrapper(name = "sensoryDataImages")
    @XmlElement(name = "sensoryDataImage")
    public Set<SensoryDataImage> getSensoryDataImages() {
        return sensoryDataImages;
    }

    public void setSensoryDataImages(LinkedHashSet<SensoryDataImage> sensoryDataImages) {
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

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }

    public ScannerTechnologyType getScannerTechnology() {
        return scannerTechnology;
    }

    public void setScannerTechnology(ScannerTechnologyType scannerTechnology) {
        this.scannerTechnology = scannerTechnology;
    }

    public RgbCapture getRgbCapture() {
        return rgbCapture;
    }

    public void setRgbCapture(RgbCapture rgbCapture) {
        this.rgbCapture = rgbCapture;
    }

    public String getCameraDetails() {
        return cameraDetails;
    }

    public void setCameraDetails(String cameraDetails) {
        this.cameraDetails = cameraDetails;
    }

    @Override
    @Transient
    public boolean isHasBrowsableImages() {
        return true;
    }
}
