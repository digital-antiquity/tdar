package org.tdar.core.dao;

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class DirSummary extends DirSummaryPart implements Serializable {

    private static final long serialVersionUID = -8869755171578509330L;

    public DirSummary() {
        super(null);
    }

    public SortedSet<DirSummaryPart> getParts() {
        return parts;
    }

    public void setParts(SortedSet<DirSummaryPart> parts) {
        this.parts = parts;
    }

    private SortedSet<DirSummaryPart> parts = new TreeSet<>( new Comparator<DirSummaryPart>() {

        @Override
        public int compare(DirSummaryPart o1, DirSummaryPart o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return StringUtils.compare(o1.getDirPath(), o2.getDirPath());
        }
    });
    
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
