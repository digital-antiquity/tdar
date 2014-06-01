package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/dashboard")
@Component
@Scope("prototype")
public class DashboardController extends AuthenticationAware.Base implements DataTableResourceDisplay {

    private static final long serialVersionUID = -2959809512424441740L;
    private List<Resource> recentlyEditedResources = new ArrayList<Resource>();
    private List<Project> emptyProjects = new ArrayList<Project>();
    private List<Resource> bookmarkedResources;
    private Long activeResourceCount = 0l;
    private int maxRecentResources = 5;
    private List<Resource> filteredFullUserProjects;
    private List<Resource> fullUserProjects;
    private Map<ResourceType, Map<Status, Long>> resourceCountAndStatusForUser = new HashMap<ResourceType, Map<Status, Long>>();
    private List<ResourceCollection> allResourceCollections = new ArrayList<ResourceCollection>();
    private List<ResourceCollection> sharedResourceCollections = new ArrayList<ResourceCollection>();
    private Map<ResourceType, Long> resourceCountForUser = new HashMap<ResourceType, Long>();
    private Map<Status, Long> statusCountForUser = new HashMap<Status, Long>();
    private Set<Account> accounts = new HashSet<Account>();
    private Set<Account> overdrawnAccounts = new HashSet<Account>();
    private List<InformationResource> resourcesWithErrors;
    
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private transient InformationResourceFileService informationResourceFileService;
    @Autowired
    private transient AccountService accountService;
    @Autowired
    private transient EntityService entityService;
    @Autowired
    private transient ResourceService resourceService;
    private List<Project> allSubmittedProjects;


    // remove when we track down what exactly the perf issue is with the dashboard;
    // toggles let us turn off specific queries / parts of homepage

    @Override
    @Action("dashboard")
    public String execute() {
        getLogger().trace("find recently edited resources");
        setRecentlyEditedResources(projectService.findRecentlyEditedResources(getAuthenticatedUser(), maxRecentResources));
        getLogger().trace("find empty projects");
        setEmptyProjects(projectService.findEmptyProjects(getAuthenticatedUser()));
        getLogger().trace("counts for graphs");
        setResourceCountAndStatusForUser(resourceService.getResourceCountAndStatusForUser(getAuthenticatedUser(), Arrays.asList(ResourceType.values())));
        setupResourceCollectionTreesForDashboard();
        setResourcesWithErrors(informationResourceFileService.findInformationResourcesWithFileStatus(getAuthenticatedUser(),
                Arrays.asList(Status.ACTIVE, Status.DRAFT), Arrays.asList(FileStatus.PROCESSING_ERROR, FileStatus.PROCESSING_WARNING)));
        getAccounts().addAll(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        for (Account account : getAccounts()) {
            if (account.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                overdrawnAccounts.add(account);
            }
        }
        prepareProjectStuff();
        setupBookmarks();
        activeResourceCount += getStatusCountForUser().get(Status.ACTIVE);
        activeResourceCount += getStatusCountForUser().get(Status.DRAFT);

        return SUCCESS;
    }

    private void setupResourceCollectionTreesForDashboard() {
        getLogger().trace("parent/ owner collections");
        getAllResourceCollections().addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));
        getLogger().trace("accessible collections");
        getSharedResourceCollections().addAll(entityService.findAccessibleResourceCollections(getAuthenticatedUser()));
        List<Long> collectionIds = Persistable.Base.extractIds(getAllResourceCollections());
        collectionIds.addAll(Persistable.Base.extractIds(getSharedResourceCollections()));
        getLogger().trace("reconcile tree1");
        resourceCollectionService.reconcileCollectionTree(getAllResourceCollections(), getAuthenticatedUser(), collectionIds);
        getLogger().trace("reconcile tree2");
        resourceCollectionService.reconcileCollectionTree(getSharedResourceCollections(), getAuthenticatedUser(), collectionIds);

        getLogger().trace("removing duplicates");
        getSharedResourceCollections().removeAll(getAllResourceCollections());
        getLogger().trace("sorting");
        Collections.sort(allResourceCollections);
        Collections.sort(sharedResourceCollections);
        getLogger().trace("done sort");
    }

    /**
     * @param activeResourceCount
     *            the activeResourceCount to set
     */
    public void setActiveResourceCount(Long activeResourceCount) {
        this.activeResourceCount = activeResourceCount;
    }

    /**
     * @return the activeResourceCount
     */
    public Long getActiveResourceCount() {
        if (activeResourceCount < 1) {
            activeResourceCount += getStatusCountForUser().get(Status.ACTIVE);
            activeResourceCount += getStatusCountForUser().get(Status.DRAFT);
        }
        return activeResourceCount;
    }

    /**
     * @param recentlyEditedResources
     *            the recentlyEditedResources to set
     */
    public void setRecentlyEditedResources(List<Resource> recentlyEditedResources) {
        this.recentlyEditedResources = recentlyEditedResources;
    }

    /**
     * @return the recentlyEditedResources
     */
    public List<Resource> getRecentlyEditedResources() {
        return recentlyEditedResources;
    }

    /**
     * @param emptyProjects
     *            the emptyProjects to set
     */
    public void setEmptyProjects(List<Project> emptyProjects) {
        this.emptyProjects = emptyProjects;
    }

    /**
     * @return the emptyProjects
     */
    public List<Project> getEmptyProjects() {
        return emptyProjects;
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
            getAuthenticationAndAuthorizationService().applyTransientViewableFlag(res, getAuthenticatedUser());
        }
    }

    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public void setFullUserProjects(List<Resource> projects) {
        fullUserProjects = projects;
    }

    public void setAllSubmittedProjects(List<Project> projects) {
        allSubmittedProjects = projects;
    }

    public void setFilteredFullUserProjects(List<Resource> projects) {
        filteredFullUserProjects = projects;
    }

    public void setEditableProjects(Set<Resource> projects) {
        editableProjects = projects;
    }

    public List<Resource> getFilteredFullUserProjects() {
        return filteredFullUserProjects;
    }

    private Set<Resource> editableProjects = new HashSet<>();

    private void prepareProjectStuff() {
        boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        editableProjects = new TreeSet<Resource>(projectService.findSparseTitleIdProjectListByPerson(
                getAuthenticatedUser(), canEditAnything));

        filteredFullUserProjects = new ArrayList<Resource>(getFullUserProjects());
        filteredFullUserProjects.removeAll(getAllSubmittedProjects());

        if (fullUserProjects == null) {
            fullUserProjects = new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
            Collections.sort(fullUserProjects);
            fullUserProjects.removeAll(getAllSubmittedProjects());
        }

        allSubmittedProjects = projectService.findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
    }
    
    public Set<Resource> getEditableProjects() {
        return editableProjects;
    }

    public void prepare() {
    }

    public Map<ResourceType, Map<Status, Long>> getResourceCountAndStatusForUser() {
        return resourceCountAndStatusForUser;
    }

    public Map<ResourceType, Long> getResourceCountForUser() {
        if (CollectionUtils.isEmpty(resourceCountForUser.keySet())) {
            for (ResourceType type : getResourceCountAndStatusForUser().keySet()) {
                Long count = 0L;
                for (Status status : getResourceCountAndStatusForUser().get(type).keySet()) {
                    if (getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser())
                            && status == Status.DELETED) {
                        continue;
                    }
                    if (getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser())
                            && status == Status.FLAGGED) {
                        continue;
                    }
                    count += getResourceCountAndStatusForUser().get(type).get(status);
                }
                resourceCountForUser.put(type, count);
            }
        }
        return resourceCountForUser;
    }

    public Map<Status, Long> getStatusCountForUser() {
        if (CollectionUtils.isEmpty(statusCountForUser.keySet())) {
            for (Status status : Status.values()) {
                Long count = 0L;
                if (getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser())
                        && status == Status.DELETED) {
                    continue;
                }
                if (getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser())
                        && status == Status.FLAGGED) {
                    continue;
                }
                if ((!TdarConfiguration.getInstance().isPayPerIngestEnabled() ||
                        getAuthenticationAndAuthorizationService().cannot(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser()))
                        && status == Status.FLAGGED_ACCOUNT_BALANCE) {
                    continue;
                }

                for (ResourceType type : getResourceCountAndStatusForUser().keySet()) {
                    if (getResourceCountAndStatusForUser().get(type).containsKey(status)) {
                        count += getResourceCountAndStatusForUser().get(type).get(status);
                    }
                }
                statusCountForUser.put(status, count);

            }
        }
        return statusCountForUser;
    }

    public void setResourceCountAndStatusForUser(Map<ResourceType, Map<Status, Long>> resourceCountAndStatusForUser) {
        this.resourceCountAndStatusForUser = resourceCountAndStatusForUser;
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(getAuthenticationAndAuthorizationService().getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public List<ResourceType> getResourceTypes() {
        return resourceService.getAllResourceTypes();
    }

    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    @DoNotObfuscate(reason = "not needed / performance test")
    public List<ResourceCollection> getAllResourceCollections() {
        return allResourceCollections;
    }

    public void setAllResourceCollections(List<ResourceCollection> resourceCollections) {
        this.allResourceCollections = resourceCollections;
    }

    /**
     * @return the sharedResourceCollections
     */
    @DoNotObfuscate(reason = "not needed / performance test")
    public List<ResourceCollection> getSharedResourceCollections() {
        return sharedResourceCollections;
    }

    /**
     * @param sharedResourceCollections
     *            the sharedResourceCollections to set
     */
    public void setSharedResourceCollections(List<ResourceCollection> sharedResourceCollections) {
        this.sharedResourceCollections = sharedResourceCollections;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public Set<Account> getOverdrawnAccounts() {
        return overdrawnAccounts;
    }

    public void setOverdrawnAccounts(Set<Account> overdrawnAccounts) {
        this.overdrawnAccounts = overdrawnAccounts;
    }

    public List<InformationResource> getResourcesWithErrors() {
        return resourcesWithErrors;
    }

    public void setResourcesWithErrors(List<InformationResource> resourcesWithErrors) {
        this.resourcesWithErrors = resourcesWithErrors;
    }

}
