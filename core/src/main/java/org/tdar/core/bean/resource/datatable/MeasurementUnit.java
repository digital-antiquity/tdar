package org.tdar.core.bean.resource.datatable;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Represents a measurement unit
 * 
 * @author abrin
 * 
 */
public enum MeasurementUnit implements HasLabel, Localizable {
    KILOGRAM("kilogram", "kg"), GRAM("gram", "g"), MILLIGRAM("milligram", "mg"), MICROGRAM("microgram", "mcg"), KILOMETER("kilometer", "km"), METER("meter",
            "m"), CENTIMETER("centimeter", "cm"), MILLIMETER("millimeter", "mm"), SQUAR_METER("square meter", "m2"), HECTARE("hectare",
                    "ha"), SQUARE_KM("square kilometer", "km2"), MILLILITER("milliliter", "ml"), CUBIC_CM("cubic centimeter", "cc"), LITRE("liter",
                            "l"), DEGREE_LATITUDE("degree lat/long", "deg"), PARTS_PER_MILLION("parts per million", "ppm"), YEAR("year", "year");

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

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
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
