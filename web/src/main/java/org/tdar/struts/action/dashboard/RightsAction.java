package org.tdar.struts.action.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.UserNotificationService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TitleSortComparator;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata
 * (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/dashboard")
@Component
@Scope("prototype")
public class RightsAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 5576550365349636811L;
    private TreeSet<SharedCollection> allResourceCollections = new TreeSet<>(new TitleSortComparator());
    

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient EntityService entityService;
    @Autowired
    private transient UserNotificationService userNotificationService;

    private List<UserNotification> currentNotifications;
    private String statusData;
    private String resourceTypeData;

    @Override
    public void validate() {
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            addActionError(getText("dashboardController.user_must_login"));
        }
        super.validate();
    }

    @Override
    @Action(value = "rights", results = { @Result(name = SUCCESS, location = "rights.ftl") })
    public String execute() throws SolrServerException, IOException {
        getLogger().trace("done");
        return SUCCESS;
    }

    @SuppressWarnings("Duplicates")
    private void setupResourceCollectionTreesForDashboard() {
        getLogger().trace("parent/ owner collections");
        for (SharedCollection rc : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser(),
                SharedCollection.class)) {
            if (rc.isTopCollection()) {
                getAllResourceCollections().add((SharedCollection) rc);
            }
        }
        getLogger().trace("accessible collections");
        for (ResourceCollection rc : entityService.findAccessibleResourceCollections(getAuthenticatedUser())) {
            if (rc instanceof SharedCollection) {
                getAllResourceCollections().add((SharedCollection) rc);
            }
        }
        getLogger().trace("done ");
    }

    private List<TdarUser> findUsersSharedWith = new ArrayList<>();

    public void prepare() {
        setCurrentNotifications(userNotificationService.getCurrentNotifications(getAuthenticatedUser()));
        getLogger().trace("begin collection tree");
        setupResourceCollectionTreesForDashboard();
        getLogger().trace("begin find shared with");
        setFindUsersSharedWith(resourceCollectionService.findUsersSharedWith(getAuthenticatedUser()));
        getLogger().trace("done");
    }


    @DoNotObfuscate(reason = "not needed / performance test")
    public Set<SharedCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(TreeSet<SharedCollection> resourceCollections) {
        this.allResourceCollections = resourceCollections;
    }

 
    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }

    public String getResourceTypeData() {
        return resourceTypeData;
    }

    public void setResourceTypeData(String resourceTypeData) {
        this.resourceTypeData = resourceTypeData;
    }

    public String getStatusData() {
        return statusData;
    }

    public void setStatusData(String statusData) {
        this.statusData = statusData;
    }


    @Override
    public boolean isRightSidebar() {
        return true;
    }

    public List<TdarUser> getFindUsersSharedWith() {
        return findUsersSharedWith;
    }

    public void setFindUsersSharedWith(List<TdarUser> findUsersSharedWith) {
        this.findUsersSharedWith = findUsersSharedWith;
    }

}
