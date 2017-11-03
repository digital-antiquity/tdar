package org.tdar.search.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

public class CollectionSearchQueryObject implements Serializable {

    private static final long serialVersionUID = -7985022894011278341L;

    private Long id;
    private Operator operator = Operator.AND;
    
    private boolean limitToTopLevel = false;
    private List<String> allFields = new ArrayList<>();
    private GeneralPermissions permission;
    private List<String> titles = new ArrayList<>();
    private CollectionType type;

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

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> title) {
        this.titles = title;
    }

    public CollectionType getType() {
        return type;
    }

    public void setType(CollectionType type) {
        this.type = type;
    }

}
