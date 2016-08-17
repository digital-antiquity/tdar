package org.tdar.core.bean.collection;

public enum CollectionType {
    INTERNAL("Internal"), SHARED("Shared"), LIST("List");

    private String label;

    private CollectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
