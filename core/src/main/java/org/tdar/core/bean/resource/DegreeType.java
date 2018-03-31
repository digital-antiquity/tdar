package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * The type of degree
 * 
 * @author abrin
 * 
 */
public enum DegreeType implements HasLabel, Localizable {
    UNDERGRADUATE("Undergraduate Thesis"),
    MASTERS("Masters Thesis"),
    DOCTORAL("Doctoral Dissertation");
    private String label;

    private DegreeType(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
