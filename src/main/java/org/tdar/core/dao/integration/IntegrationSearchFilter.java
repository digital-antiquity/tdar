package org.tdar.core.dao.integration;

import java.io.Serializable;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;

/**
 * Simple pojo for storing Integration search parameters
 */
public abstract class IntegrationSearchFilter implements Serializable{

    private static final long serialVersionUID = -6417523572672914499L;
    private Project project;
    private ResourceCollection collection;
    private Boolean isBookmarked = false;
    private Boolean isAbleToIntegrate = false;
    private TdarUser authorizedUser;

    public Boolean getIsBookmarked() {
        return isBookmarked;
    }

    public void setIsBookmarked(Boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    public Boolean getIsAbleToIntegrate() {
        return isAbleToIntegrate;
    }

    public void setIsAbleToIntegrate(Boolean isAbleToIntegrate) {
        this.isAbleToIntegrate = isAbleToIntegrate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

    public TdarUser getAuthorizedUser() {
        return authorizedUser;
    }

    public void setAuthorizedUser(TdarUser authorizedUser) {
        this.authorizedUser = authorizedUser;
    }


}
