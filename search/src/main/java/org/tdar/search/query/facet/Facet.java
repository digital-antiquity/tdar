package org.tdar.search.query.facet;

import java.io.Serializable;

public class Facet implements Serializable {

	private static final long serialVersionUID = -2689381884158539009L;

	private String label;
	private Long count = 0L;
	private String raw;
	private String detailUrl;
	private Class<?> className;

    public Object getCount;

	public Facet() {
	}

	public Facet(String raw, String label, Long value, Class<?> facetClass) {
		this.setRaw(raw);
		this.label = label;
		if (value!= null) {
		    this.count = value;
		}
		this.className = facetClass;
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


	public Class<?> getClassName() {
		return className;
	}

	public String getSimpleName() {
		if (className == null) {
			return "";
		}
		return className.getSimpleName();
	}
	
	public void setClassName(Class<?> className) {
		this.className = className;
	}

    public String getUniqueKey() {
        return String.format("%s-%s", className.getSimpleName(), label);
    }

    public void incrementCountBy(Long add) {
        if (add != null) {
            count += add;
        }
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
