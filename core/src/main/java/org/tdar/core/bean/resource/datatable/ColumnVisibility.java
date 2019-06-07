package org.tdar.core.bean.resource.datatable;

public enum ColumnVisibility {
    HIDDEN,
    CONFIDENTIAL,
    VISIBLE;

    public boolean isVisible() {
        return this == VISIBLE;
    }
}
