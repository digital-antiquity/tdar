package org.tdar.core.bean.resource.datatable;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum MeasurementUnit implements HasLabel {
    KILOGRAM(MessageHelper.getMessage("measurementUnit.kilogram"), "kg"),
    GRAM(MessageHelper.getMessage("measurementUnit.gram"), "g"),
    MILLIGRAM(MessageHelper.getMessage("measurementUnit.milligram"), "mg"),
    MICROGRAM(MessageHelper.getMessage("measurementUnit.microgram"), "mcg"),
    KILOMETER(MessageHelper.getMessage("measurementUnit.kilometer"), "km"),
    METER(MessageHelper.getMessage("measurementUnit.meter"), "m"),
    CENTIMETER(MessageHelper.getMessage("measurementUnit.centimeter"), "cm"),
    MILLIMETER(MessageHelper.getMessage("measurementUnit.millimeter"), "mm"),
    SQUAR_METER(MessageHelper.getMessage("measurementUnit.square_meter"), "m2"),
    HECTARE(MessageHelper.getMessage("measurementUnit.hectare"), "ha"),
    SQUARE_KM(MessageHelper.getMessage("measurementUnit.square_km"), "km2"),
    MILLILITER(MessageHelper.getMessage("measurementUnit.milliliter"), "ml"),
    CUBIC_CM(MessageHelper.getMessage("measurementUnit.cubic_cm"), "cc"),
    LITRE(MessageHelper.getMessage("measurementUnit.litre"), "l"),
    PARTS_PER_MILLION(MessageHelper.getMessage("measurementUnit.parts_per_million"), "ppm");

    private String shortName;
    private String fullName;

    private MeasurementUnit(String label, String simple) {
        this.setFullName(label);
        this.setShortName(simple);
    }

    @Override
    public String getLabel() {
        return getFullName();
    }

    private void setFullName(String label) {
        this.fullName = label;
    }

    public String getFullName() {
        return fullName;
    }

    public Float convertTo(MeasurementUnit newUnit, Float currentValue) {
        // FIXME: look for an apache or other conversion tool to take care of this for us
        // http://jscience.org/api/org/jscience/physics/amount/package-summary.html
        return -1f;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    private void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }
}
