package org.tdar.core.dao;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class DirSummary extends DirSummaryPart implements Serializable {

    private static final long serialVersionUID = -8869755171578509330L;

    public DirSummary() {
        super(null);
    }

    public Set<DirSummaryPart> getParts() {
        return parts;
    }

    public void setParts(Set<DirSummaryPart> parts) {
        this.parts = parts;
    }

    private Set<DirSummaryPart> parts = new LinkedHashSet<>();
    
    public DirSummaryPart addPart(Object[] row) {
        DirSummaryPart part = new DirSummaryPart(row);
        setAdded(getAdded() + part.getAdded());
        setCurated(getCurated() + part.getCurated());
        setResource(getResource() + part.getResource());
        setExternalReviewed(getExternalReviewed() + part.getExternalReviewed());
        setInitialReviewed(getInitialReviewed() + part.getInitialReviewed());
        setReviewed(getReviewed() + part.getReviewed());
        parts.add(part);
        return part;
    }
}
