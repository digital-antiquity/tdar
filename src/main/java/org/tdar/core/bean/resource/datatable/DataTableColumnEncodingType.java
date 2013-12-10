package org.tdar.core.bean.resource.datatable;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum DataTableColumnEncodingType implements HasLabel {
    UNCODED_VALUE(MessageHelper.getMessage("dataTableColumnEncodingType.uncoded"), false, true, false),
    CODED_VALUE(MessageHelper.getMessage("dataTableColumnEncodingType.coded"), true, true, false),
    MEASUREMENT(MessageHelper.getMessage("dataTableColumnEncodingType.measurement"), false, false, true),
    COUNT(MessageHelper.getMessage("dataTableColumnEncodingType.count"), false, false, false);

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

    /**
     * @param supportsOntology
     *            the supportsOntology to set
     */
    public void setSupportsOntology(boolean supportsOntology) {
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
    public void setSupportsCodingSheet(boolean supportsCodingSheet) {
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
    public void setSupportsMeasurement(boolean supportsMeasurement) {
        this.supportsMeasurement = supportsMeasurement;
    }

    /**
     * @return the supportsMeasurement
     */
    public boolean isSupportsMeasurement() {
        return supportsMeasurement;
    }
}
