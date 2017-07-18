package org.tdar.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

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
    private List<SharedCollection> allResourceCollections = new ArrayList<>();
    private List<SharedCollection> sharedResourceCollections = new ArrayList<>();
    

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
            getAllResourceCollections().add((SharedCollection) rc);
        }
        getLogger().trace("accessible collections");
        for (ResourceCollection rc : entityService.findAccessibleResourceCollections(getAuthenticatedUser())) {
            if (rc instanceof SharedCollection) {
                getSharedResourceCollections().add((SharedCollection) rc);
            }
        }
        List<Long> collectionIds = PersistableUtils.extractIds(getAllResourceCollections());
        collectionIds.addAll(PersistableUtils.extractIds(getSharedResourceCollections()));
        getLogger().trace("reconcile tree1");
        resourceCollectionService.reconcileCollectionTree(getAllResourceCollections(), getAuthenticatedUser(),
                collectionIds, SharedCollection.class);
        getLogger().trace("reconcile tree2");
        resourceCollectionService.reconcileCollectionTree(getSharedResourceCollections(), getAuthenticatedUser(),
                collectionIds, SharedCollection.class);

        getLogger().trace("removing duplicates");
        getSharedResourceCollections().removeAll(getAllResourceCollections());
        getLogger().trace("sorting");
        Collections.sort(allResourceCollections);
        Collections.sort(sharedResourceCollections);
        getLogger().trace("done sort");
    }

    private List<TdarUser> findUsersSharedWith = new ArrayList<>();

    public void prepare() {
        setCurrentNotifications(userNotificationService.getCurrentNotifications(getAuthenticatedUser()));
        getLogger().trace("begin collection tree");
        setupResourceCollectionTreesForDashboard();
        getLogger().trace("begin find shared with");
        setFindUsersSharedWith(resourceCollectionService.findUsersSharedWith(getAuthenticatedUser()));
//        prepareProjectStuff();
//        internalCollections = resourceCollectionService.findAllInternalCollections(getAuthenticatedUser());
    }


    @DoNotObfuscate(reason = "not needed / performance test")
    public List<SharedCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<SharedCollection> resourceCollections) {
        this.allResourceCollections = resourceCollections;
    }

    /**
     * @return the sharedResourceCollections
     */
    @DoNotObfuscate(reason = "not needed / performance test")
    public List<SharedCollection> getSharedResourceCollections() {
        return sharedResourceCollections;
    }

    /**
     * @param sharedResourceCollections
     *            the sharedResourceCollections to set
     */
    public void setSharedResourceCollections(List<SharedCollection> sharedResourceCollections) {
        this.sharedResourceCollections = sharedResourceCollections;
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
