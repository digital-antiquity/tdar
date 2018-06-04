package org.tdar.core.dao;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.file.TdarDir;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class DirSummaryPart implements Serializable {

    private static final long serialVersionUID = 7076404625739185355L;
    TdarDir dir;
    private Long id;
    Integer resource = 0;
    Integer curated = 0;
    Integer initialReviewed = 0;
    Integer reviewed = 0;
    Integer externalReviewed = 0;
    private String dirPath;
    private Set<Long> children = new HashSet<>();

    public DirSummaryPart(Object[] row) {
        if (row == null) {
            return;
        }
        if (row[0] != null) {
            this.id = ((Number) row[0]).longValue();
        }
        this.add(getIntValue(row[1]), getIntValue(row[2]), getIntValue(row[3]), getIntValue(row[4]), getIntValue(row[5]));
    }

    private void add(Integer resource, Integer curated, Integer initialReviewed, Integer reviewed, Integer externalReviewed) {
        this.resource += getIntValue(resource);
        this.curated += getIntValue(curated);
        this.initialReviewed += getIntValue(initialReviewed);
        this.reviewed += getIntValue(reviewed);
        this.externalReviewed += getIntValue(externalReviewed);

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

    public TdarDir getDir() {
        return dir;
    }

    public void setDir(TdarDir parent) {
        this.dir = parent;
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

    public Long getId() {
        return id;
    }

    public void setId(Long parentId) {
        this.id = parentId;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public Set<Long> getChildren() {
        return children;
    }

    public void setChildren(Set<Long> children) {
        this.children = children;
    }

    public void addAll(Set<Long> allChildren, Map<Long, DirSummaryPart> parentPartMap) {
        for (Long cid : allChildren) {
            DirSummaryPart part = parentPartMap.get(cid);
            add(part.getResource(), part.getCurated(), part.getInitialReviewed(), part.getReviewed(), part.getExternalReviewed());
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.id, ((DirSummaryPart)obj).getId());
    }
    
    @Override
    public int hashCode() {
        // since we typically get called from instance method it's unlikely persistable will be null, but lets play safe...
        if (id == null) {
            LoggerFactory.getLogger(Persistable.class).trace("0 b/c 'a' is null");
            return System.identityHashCode(this);
        }
        HashCodeBuilder builder = new HashCodeBuilder(23, 37);
        builder.append(id);
        int hashCode = builder.toHashCode();
        return hashCode;
    }
}
