package org.tdar.search.bean;

import java.io.Serializable;
import java.util.List;

public class CollectionSearchQueryObject implements Serializable {

    private static final long serialVersionUID = -7985022894011278341L;

    private Long id;
    private boolean includeHidden = true;
    private boolean limitToTopLevel = false;
    private List<String> allFields;

    public boolean isIncludeHidden() {
        return includeHidden;
    }

    public void setIncludeHidden(boolean includeHidden) {
        this.includeHidden = includeHidden;
    }

    public boolean isLimitToTopLevel() {
        return limitToTopLevel;
    }

    public void setLimitToTopLevel(boolean limitToTopLevel) {
        this.limitToTopLevel = limitToTopLevel;
    }

    public List<String> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<String> allFields) {
        this.allFields = allFields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
