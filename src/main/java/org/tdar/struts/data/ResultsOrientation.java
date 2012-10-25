package org.tdar.struts.data;

import org.tdar.core.bean.HasLabel;

public enum ResultsOrientation implements HasLabel {
    LIST("List"),
    GRID("Grid"),
    MAP("Map");
    
    private String label;
    
    private ResultsOrientation(String label) {
    this.setLabel(label);    
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}