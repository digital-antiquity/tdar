package org.tdar.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DirSummary extends DirSummaryPart implements Serializable {

    private static final long serialVersionUID = -8869755171578509330L;

    public DirSummary() {
        super(null);
    }

    public List<DirSummaryPart> getParts() {
        return parts;
    }

    public void setParts(List<DirSummaryPart> parts) {
        this.parts = parts;
    }

    private List<DirSummaryPart> parts = new ArrayList<>();
    
    public DirSummaryPart addPart(Object[] row) {
        DirSummaryPart part = new DirSummaryPart(row);
        setCurated(getCurated() + part.getCurated());
        setResource(getResource() + part.getResource());
        setExternalReviewed(getExternalReviewed() + part.getExternalReviewed());
        setInitialReviewed(getInitialReviewed() + part.getInitialReviewed());
        setReviewed(getReviewed() + part.getReviewed());
        parts.add(part);
        return part;
    }
}
