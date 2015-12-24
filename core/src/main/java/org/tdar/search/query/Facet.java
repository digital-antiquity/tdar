package org.tdar.search.query;

import java.io.Serializable;

public class Facet implements Serializable {

    private static final long serialVersionUID = -2689381884158539009L;

    private String label;
    private Long count;
    private String raw;

    public Facet() {
    }

    public Facet(String raw, String label, Long value) {
        this.setRaw(raw);
        this.label = label;
        this.count = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long value) {
        this.count = value;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getLabel(), getCount());
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }
}
