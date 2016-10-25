package org.tdar.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.Persistable;

public class DeleteIssue implements Serializable {

    private static final long serialVersionUID = 1360310557259881380L;

    private String issue;
    private List<Persistable> relatedItems = new ArrayList<>();

    public List<Persistable> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<Persistable> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }
}
