package org.tdar.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
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
public class ListCollectionOrganizeAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -5569349309409607003L;
    private List<Resource> bookmarkedResources;
    private List<ListCollection> allResourceCollections = new ArrayList<>();
    private List<ListCollection> sharedResourceCollections = new ArrayList<>();


    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    private List<UserNotification> currentNotifications;

    @Override
    public void validate() {
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            addActionError(getText("dashboardController.user_must_login"));
        }
        super.validate();
    }

    @Override
    @Action(value = "organize", results = { @Result(name = SUCCESS, location = "dashboard/organize.ftl") })
    public String execute() throws SolrServerException, IOException {

        return SUCCESS;
    }

    private void setupResourceCollectionTreesForDashboard() {
        getLogger().trace("parent/ owner collections");
        for (ListCollection rc : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser(),
                ListCollection.class)) {
            getAllResourceCollections().add((ListCollection) rc);
        }
        getLogger().trace("accessible collections");
        for (ResourceCollection rc : entityService.findAccessibleResourceCollections(getAuthenticatedUser())) {
            if (rc instanceof ListCollection) {
                getSharedResourceCollections().add((ListCollection) rc);
            }
        }
        List<Long> collectionIds = PersistableUtils.extractIds(getAllResourceCollections());
        collectionIds.addAll(PersistableUtils.extractIds(getSharedResourceCollections()));
        getLogger().trace("reconcile tree1");
        resourceCollectionService.reconcileCollectionTree(getAllResourceCollections(), getAuthenticatedUser(),
                collectionIds, ListCollection.class);
        getLogger().trace("reconcile tree2");
        resourceCollectionService.reconcileCollectionTree(getSharedResourceCollections(), getAuthenticatedUser(),
                collectionIds, ListCollection.class);

        getLogger().trace("removing duplicates");
        getSharedResourceCollections().removeAll(getAllResourceCollections());
        getLogger().trace("sorting");
        Collections.sort(allResourceCollections);
        Collections.sort(sharedResourceCollections);
        getLogger().trace("done sort");
    }



    public List<Resource> getBookmarkedResources() {
        return bookmarkedResources;
    }

    public void setBookmarkedResource(List<Resource> bookmarks) {
        this.bookmarkedResources = bookmarks;
    }

    private void setupBookmarks() {
        if (bookmarkedResources == null) {
            bookmarkedResources = bookmarkedResourceService.findBookmarkedResourcesByPerson(getAuthenticatedUser(),
                    Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }

        for (Resource res : bookmarkedResources) {
            authorizationService.applyTransientViewableFlag(res, getAuthenticatedUser());
        }
    }


    @Override
    public void prepare() {
        setupResourceCollectionTreesForDashboard();
        setupBookmarks();
    }


    @DoNotObfuscate(reason = "not needed / performance test")
    public List<ListCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<ListCollection> resourceCollections) {
        this.allResourceCollections = resourceCollections;
    }

    /**
     * @return the sharedResourceCollections
     */
    @DoNotObfuscate(reason = "not needed / performance test")
    public List<ListCollection> getSharedResourceCollections() {
        return sharedResourceCollections;
    }

    /**
     * @param sharedResourceCollections
     *            the sharedResourceCollections to set
     */
    public void setSharedResourceCollections(List<ListCollection> sharedResourceCollections) {
        this.sharedResourceCollections = sharedResourceCollections;
    }

    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }


    @Override
    public boolean isRightSidebar() {
        return true;
    }

    //fixme: a bookmark collection won't have child collections.  However, if we add the ability to bookmark a collection, the idea of BookmarkCollection becomes less viable
    /**
     * Return the current user's bookmarks as though it were a collection (which it isn't, but it's useful to treat it like one for certain tasks)
     * @return
     */
    public ListCollection getBookmarkCollection() {
        return null;
    }

    /***
     * Get the collections we wish to display in the 'summary' section (including shared collections & the bookmarks 'collection')
     * @return
     */
    public List<ListCollection> getSummaryItems() {
            List<ListCollection> items = new ArrayList<>();
//        items.add(getBookmarkCollection());
        //for some reason 'shared' resource list empty
        items.addAll(getAllResourceCollections());

        return items;
    }
}
