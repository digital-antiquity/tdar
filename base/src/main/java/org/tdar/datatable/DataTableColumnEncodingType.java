package org.tdar.datatable;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Manages the column type that the user tells us.
 * 
 * @author abrin
 * 
 */
public enum DataTableColumnEncodingType implements HasLabel, Localizable {
    UNCODED_VALUE("Uncoded Value", false, true, false),
    CODED_VALUE("Coded Value", true, true, false),
    MEASUREMENT("Measurement", false, false,
            true),
    COUNT("Count", false, false, false),
    FILENAME("Filename", false, false, false);

    private final String label;
    private boolean supportsOntology;
    private boolean supportsCodingSheet;
    private boolean supportsMeasurement;

    private DataTableColumnEncodingType(String label, boolean supportsCodingSheet, boolean supportsOntology, boolean supportsMeasurement) {
        this.label = label;
        setSupportsCodingSheet(supportsCodingSheet);
        setSupportsMeasurement(supportsMeasurement);
        setSupportsOntology(supportsOntology);
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    /**
     * @param supportsOntology
     *            the supportsOntology to set
     */
    private void setSupportsOntology(boolean supportsOntology) {
        this.supportsOntology = supportsOntology;
    }

    /**
     * @return the supportsOntology
     */
    public boolean isSupportsOntology() {
        return supportsOntology;
    }

    /**
     * @param supportsCodingSheet
     *            the supportsCodingSheet to set
     */
    private void setSupportsCodingSheet(boolean supportsCodingSheet) {
        this.supportsCodingSheet = supportsCodingSheet;
    }

    /**
     * @return the supportsCodingSheet
     */
    public boolean isSupportsCodingSheet() {
        return supportsCodingSheet;
    }

    /**
     * @param supportsMeasurement
     *            the supportsMeasurement to set
     */
    private void setSupportsMeasurement(boolean supportsMeasurement) {
        this.supportsMeasurement = supportsMeasurement;
    }

    /**
     * @return the supportsMeasurement
     */
    public boolean isSupportsMeasurement() {
        return supportsMeasurement;
    }

    public boolean isCount() {
        return this == COUNT;
    }

    public boolean isFilename() {
        return this == FILENAME;
    }
}
