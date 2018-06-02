package org.tdar.core.dao;

import java.io.Serializable;

import org.tdar.core.bean.file.TdarDir;

public class DirSummaryPart implements Serializable {

    private static final long serialVersionUID = 7076404625739185355L;
    TdarDir parent;
    Integer resource = 0;
    Integer curated = 0;
    Integer initialReviewed = 0;
    Integer reviewed = 0;
    Integer externalReviewed = 0;
    
    public DirSummaryPart(Object[] row) {
        this.parent = (TdarDir) row[0];
        this.resource += getIntValue(row[1]);
        this.curated += getIntValue(row[2]);
        this.initialReviewed += getIntValue(row[3]);
        this.reviewed += getIntValue(row[4]);
        this.externalReviewed += getIntValue(row[5]);
    }

    private Integer getIntValue(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return 0;
    }

    public TdarDir getParent() {
        return parent;
    }

    public void setParent(TdarDir parent) {
        this.parent = parent;
    }

    public Integer getResource() {
        return resource;
    }

    public void setResource(Integer resource) {
        this.resource = resource;
    }

    public Integer getCurated() {
        return curated;
    }

    public void setCurated(Integer curated) {
        this.curated = curated;
    }

    public Integer getInitialReviewed() {
        return initialReviewed;
    }

    public void setInitialReviewed(Integer initialReviewed) {
        this.initialReviewed = initialReviewed;
    }

    public Integer getReviewed() {
        return reviewed;
    }

    public void setReviewed(Integer reviewed) {
        this.reviewed = reviewed;
    }

    public Integer getExternalReviewed() {
        return externalReviewed;
    }

    public void setExternalReviewed(Integer externalReviewed) {
        this.externalReviewed = externalReviewed;
    }
    
}
