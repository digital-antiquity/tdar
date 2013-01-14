package org.tdar.core.bean.resource.sensory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.SensoryData;

@Entity
@Table(name = "sensory_data_scan")
public class SensoryDataScan extends Persistable.Sequence<SensoryDataScan> implements HasResource<SensoryData> {

    private static final long serialVersionUID = -310445034386268598L;

    @Column(nullable = false)
    private String filename;

    @Column(name = "transformation_matrix")
    private String transformationMatrix;

    @Column(name = "monument_name")
    private String monumentName;

    @Column(name = "points_in_scan")
    private Long pointsInScan;

    @Column(name = "scan_notes")
    private String scanNotes;

    @Column(name = "scanner_technology")
    @Enumerated(EnumType.STRING)
    private ScannerTechnologyType scannerTechnology;

    @Column(name = "triangulation_details")
    private String triangulationDetails;

    @Column
    private Double resolution;

    @Column(name = "tof_return")
    private String tofReturn;

    @Column(name = "phase_frequency_settings")
    private String phaseFrequencySettings;

    @Column(name = "phase_noise_settings")
    private String phaseNoiseSettings;

    @Column(name = "camera_exposure_settings")
    private String cameraExposureSettings;

    @Column(name = "scan_date")
    private Date scanDate;

    @Column(name = "matrix_applied", nullable = false)
    private boolean matrixApplied;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTransformationMatrix() {
        return transformationMatrix;
    }

    public void setTransformationMatrix(String transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }

    public String getMonumentName() {
        return monumentName;
    }

    public void setMonumentName(String monumentName) {
        this.monumentName = monumentName;
    }

    public Long getPointsInScan() {
        return pointsInScan;
    }

    public void setPointsInScan(Long pointsInScan) {
        this.pointsInScan = pointsInScan;
    }

    public String getScanNotes() {
        return scanNotes;
    }

    public void setScanNotes(String scanNotes) {
        this.scanNotes = scanNotes;
    }

    public ScannerTechnologyType getScannerTechnology() {
        return scannerTechnology;
    }

    public void setScannerTechnology(ScannerTechnologyType scannerTechnology) {
        this.scannerTechnology = scannerTechnology;
    }

    public Double getResolution() {
        return resolution;
    }

    public void setResolution(Double resolution) {
        this.resolution = resolution;
    }

    public String getTriangulationDetails() {
        return triangulationDetails;
    }

    public void setTriangulationDetails(String triangulationDetails) {
        this.triangulationDetails = triangulationDetails;
    }

    public String getTofReturn() {
        return tofReturn;
    }

    public void setTofReturn(String tofReturn) {
        this.tofReturn = tofReturn;
    }

    public String getPhaseFrequencySettings() {
        return phaseFrequencySettings;
    }

    public void setPhaseFrequencySettings(String phaseFrequencySettings) {
        this.phaseFrequencySettings = phaseFrequencySettings;
    }

    public String getPhaseNoiseSettings() {
        return phaseNoiseSettings;
    }

    public void setPhaseNoiseSettings(String phaseNoiseSettings) {
        this.phaseNoiseSettings = phaseNoiseSettings;
    }

    public String getCameraExposureSettings() {
        return cameraExposureSettings;
    }

    public void setCameraExposureSettings(String cameraExposureSettings) {
        this.cameraExposureSettings = cameraExposureSettings;
    }

    @Override
    @XmlTransient
    public boolean isValid() {
        return StringUtils.isNotBlank(filename);
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public boolean isMatrixApplied() {
        return matrixApplied;
    }

    public void setMatrixApplied(boolean matrixApplied) {
        this.matrixApplied = matrixApplied;
    }

    @Override
    @XmlTransient
    public boolean isValidForController() {
        return true;
    }

    @Override
    public String toString() {
        return filename + " (" + getId() + " )";
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(getId());
    }

}
