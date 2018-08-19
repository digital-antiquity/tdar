package org.tdar.core.dao;

import java.io.Serializable;

import org.tdar.core.bean.file.TdarFile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class FileStatsProxy implements Serializable {

    private static final long serialVersionUID = 7901872597063970731L;
    private Long id;
    private String name;
    private Long parentId;

    private boolean created;
    private boolean resource;
    private boolean curated;
    private boolean initialReviewed;
    private boolean reviewed;
    private boolean externalReviewed;

    public FileStatsProxy(TdarFile f) {
        this.id = f.getId();
        this.name = f.getName();
        this.parentId = f.getParentId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public boolean isResource() {
        return resource;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    public boolean isCurated() {
        return curated;
    }

    public void setCurated(boolean curated) {
        this.curated = curated;
    }

    public boolean isInitialReviewed() {
        return initialReviewed;
    }

    public void setInitialReviewed(boolean initialReviewed) {
        this.initialReviewed = initialReviewed;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isExternalReviewed() {
        return externalReviewed;
    }

    public void setExternalReviewed(boolean externalReviewed) {
        this.externalReviewed = externalReviewed;
    }

}
