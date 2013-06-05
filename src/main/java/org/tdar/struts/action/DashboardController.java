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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ActivityManager;
import org.tdar.search.query.SortOption;
import org.tdar.utils.activity.Activity;

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
public class DashboardController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -2959809512424441740L;
    private List<Resource> recentlyEditedResources = new ArrayList<Resource>();
    private List<Project> emptyProjects = new ArrayList<Project>();
    private List<Resource> bookmarkedResources;
    private Long activeResourceCount = 0l;
    private int maxRecentResources = 5;
    private List<Resource> filteredFullUserProjects;
    private List<Resource> fullUserProjects;
    private Map<ResourceType, Map<Status, Long>> resourceCountAndStatusForUser = new HashMap<ResourceType, Map<Status, Long>>();
    private List<ResourceCollection> resourceCollections = new ArrayList<ResourceCollection>();
    private List<ResourceCollection> sharedResourceCollections = new ArrayList<ResourceCollection>();
    private Map<ResourceType, Long> resourceCountForUser = new HashMap<ResourceType, Long>();
    private Map<Status, Long> statusCountForUser = new HashMap<Status, Long>();
    private Set<Account> accounts = new HashSet<Account>();
    private Set<Account> overdrawnAccounts = new HashSet<Account>();

    @Override
    @Action("dashboard")
    public String execute() {
        setRecentlyEditedResources(getProjectService().findRecentlyEditedResources(getAuthenticatedUser(), maxRecentResources));
        setEmptyProjects(getProjectService().findEmptyProjects(getAuthenticatedUser()));
        setResourceCountAndStatusForUser(getResourceService().getResourceCountAndStatusForUser(getAuthenticatedUser(), Arrays.asList(ResourceType.values())));
        getResourceCollections().addAll(getResourceCollectionService().findParentOwnerCollections(getAuthenticatedUser()));
        getSharedResourceCollections().addAll(getEntityService().findAccessibleResourceCollections(getAuthenticatedUser()));
        // removing duplicates
        try {
        Activity indexingTask = ActivityManager.getInstance().getIndexingTask();
        if (isEditor() && indexingTask != null) {
            String properName = "unknown user";
            try {
                indexingTask.getUser().getProperName();
            } catch (Exception e) {
                logger.warn("reindexing user could not be determined");
            }
            String msg = String.format("%s is RE-INDEXING %s (%s)", properName, getSiteAcronym(), indexingTask.getStartDate());
            addActionMessage(msg);
        }
        } catch (Throwable t) {
            logger.error("what???", t);
        }
        getSharedResourceCollections().removeAll(getResourceCollections());
        Collections.sort(resourceCollections);
        Collections.sort(sharedResourceCollections);
        getAccounts().addAll(getAccountService().listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        for (Account account : getAccounts()) {
            if (account.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                overdrawnAccounts.add(account);
            }
        }
        activeResourceCount += getStatusCountForUser().get(Status.ACTIVE);
        activeResourceCount += getStatusCountForUser().get(Status.DRAFT);
        logger.trace("{}", resourceCollections);
        return SUCCESS;
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
        if (bookmarkedResources == null) {
            bookmarkedResources = getBookmarkedResourceService().findResourcesByPerson(getAuthenticatedUser(), Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }

        for (Resource res : bookmarkedResources) {
            getAuthenticationAndAuthorizationService().applyTransientViewableFlag(res, getAuthenticatedUser());
        }
        return bookmarkedResources;
    }

    public List<Project> getAllSubmittedProjects() {
        List<Project> allSubmittedProjects = getProjectService().findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        return allSubmittedProjects;
    }

    public List<Resource> getFullUserProjects() {
        if (fullUserProjects == null) {
            boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            fullUserProjects = new ArrayList<Resource>(getProjectService().findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
            Collections.sort(fullUserProjects);
            fullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return fullUserProjects;
    }

    public List<Resource> getFilteredFullUserProjects() {
        if (filteredFullUserProjects == null) {
            filteredFullUserProjects = new ArrayList<Resource>(getFullUserProjects());
            filteredFullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return filteredFullUserProjects;
    }

    public Set<Resource> getEditableProjects() {
        boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        SortedSet<Resource> findSparseTitleIdProjectListByPerson = new TreeSet<Resource>(getProjectService().findSparseTitleIdProjectListByPerson(
                getAuthenticatedUser(), canEditAnything));
        return findSparseTitleIdProjectListByPerson;
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
        List<ResourceType> toReturn = new ArrayList<ResourceType>();
        toReturn.addAll(Arrays.asList(ResourceType.values()));
        return toReturn;
    }

    public List<SortOption> getResourceDatatableSortOptions() {
        return SortOption.getOptionsForContext(Resource.class);
    }

    public List<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    public void setResourceCollections(List<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    /**
     * @return the sharedResourceCollections
     */
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

}
