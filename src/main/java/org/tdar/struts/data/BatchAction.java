package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.Status;

public class BatchAction implements Serializable {

    private static final long serialVersionUID = 8434507061461095484L;
    private List<Long> resourceIds = new ArrayList<>();
    private Status status;
    private Long accountId = -1L;
    private Long collectionId = -1L;
    private Long projectId = -1L;
    private BatchActionType type = BatchActionType.NONE;

    public enum BatchActionType {
        NONE,
        CHANGE_STATUS,
        CHANGE_PROJECT,
        ADD_COLLECTION,
        CHANGE_ACCOUNT;
    }

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BatchActionType getType() {
        return type;
    }

    public void setType(BatchActionType type) {
        this.type = type;
    }

}
