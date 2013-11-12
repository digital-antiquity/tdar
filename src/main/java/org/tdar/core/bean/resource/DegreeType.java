package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

public enum DegreeType implements HasLabel {
    UNDERGRADUATE(MessageHelper.getMessage("degreeType.undergraduate")), 
    MASTERS(MessageHelper.getMessage("degreeType.masters")), 
    DOCTORAL(MessageHelper.getMessage("degreeType.doctoral"));
    
    private String label;

    private DegreeType(String label) {
        this.setLabel(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
