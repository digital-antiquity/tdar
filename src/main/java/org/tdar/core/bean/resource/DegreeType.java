package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;

public enum DegreeType implements HasLabel {
    UNDERGRADUATE("Undergraduate Thesis"), MASTERS("Masters Thesis"), DOCTORAL("Doctoral Dissertation");
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
