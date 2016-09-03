package org.tdar.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceTypeStatusInfo;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UserNotificationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.service.query.SearchService;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

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
@Namespace("")
@Component
@Scope("prototype")
public class DashboardController extends AbstractAuthenticatableAction implements DataTableResourceDisplay {

	private static final long serialVersionUID = -2959809512424441740L;
	private List<Resource> recentlyEditedResources = new ArrayList<Resource>();
	private List<Project> emptyProjects = new ArrayList<Project>();
	private List<Resource> bookmarkedResources;
	private Integer activeResourceCount = 0;
	private int maxRecentResources = 5;
	private List<Resource> filteredFullUserProjects;
	private List<Resource> fullUserProjects;
	private List<ResourceCollection> allResourceCollections = new ArrayList<ResourceCollection>();
	private List<ResourceCollection> sharedResourceCollections = new ArrayList<ResourceCollection>();
	private Set<BillingAccount> accounts = new HashSet<BillingAccount>();
	private Set<BillingAccount> overdrawnAccounts = new HashSet<BillingAccount>();
	private List<InformationResource> resourcesWithErrors;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;
	@Autowired
	private transient ProjectService projectService;
	@Autowired
	private transient BookmarkedResourceService bookmarkedResourceService;
	@Autowired
	private transient InformationResourceFileService informationResourceFileService;
	@Autowired
	private transient BillingAccountService accountService;
	@Autowired
	private transient SearchService<?> searchService;
	@Autowired
	private transient EntityService entityService;
	@Autowired
	private transient ResourceService resourceService;
	@Autowired
	private transient SerializationService serializationService;
	@Autowired
	private transient UserNotificationService userNotificationService;

	private List<Project> allSubmittedProjects;
	private List<Resource> featuredResources = new ArrayList<Resource>();
	private List<Resource> recentResources = new ArrayList<Resource>();
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

	// remove when we track down what exactly the perf issue is with the
	// dashboard;
	// toggles let us turn off specific queries / parts of homepage

	private void setupRecentResources() throws SolrServerException, IOException {
		int count = 10;
		try {
			getFeaturedResources().addAll(searchService.findMostRecentResources(count, getAuthenticatedUser(), this));
		} catch (ParseException pe) {
			getLogger().debug("parse exception", pe);
		}
		// note, in rare occasions, a cache hit might mean that the resource's
		// status has changed
		getFeaturedResources().addAll(resourceService.getWeeklyPopularResources(count));

	}

	@Override
	@Action(value = "dashboard", results = { @Result(name = SUCCESS, location = "dashboard/dashboard.ftl") })
	public String execute() throws SolrServerException, IOException {
		setupRecentResources();
		setCurrentNotifications(userNotificationService.getCurrentNotifications(getAuthenticatedUser()));
		getLogger().trace("find recently edited resources");
		setRecentlyEditedResources(
				projectService.findRecentlyEditedResources(getAuthenticatedUser(), maxRecentResources));
		getLogger().trace("find empty projects");
		setEmptyProjects(projectService.findEmptyProjects(getAuthenticatedUser()));
		getLogger().trace("counts for graphs");
		setupResourceCollectionTreesForDashboard();
		setResourcesWithErrors(informationResourceFileService.findInformationResourcesWithFileStatus(
				getAuthenticatedUser(), Arrays.asList(Status.ACTIVE, Status.DRAFT),
				Arrays.asList(FileStatus.PROCESSING_ERROR, FileStatus.PROCESSING_WARNING)));
		getAccounts().addAll(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE,
				Status.FLAGGED_ACCOUNT_BALANCE));
		for (BillingAccount account : getAccounts()) {
			if (account.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
				overdrawnAccounts.add(account);
			}
		}

		prepareProjectStuff();
		setupBookmarks();
		initCounts();

		return SUCCESS;
	}

	private void setupResourceCollectionTreesForDashboard() {
		getLogger().trace("parent/ owner collections");
		getAllResourceCollections().addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));
		getLogger().trace("accessible collections");
		getSharedResourceCollections().addAll(entityService.findAccessibleResourceCollections(getAuthenticatedUser()));
		List<Long> collectionIds = PersistableUtils.extractIds(getAllResourceCollections());
		collectionIds.addAll(PersistableUtils.extractIds(getSharedResourceCollections()));
		getLogger().trace("reconcile tree1");
		resourceCollectionService.reconcileCollectionTree(getAllResourceCollections(), getAuthenticatedUser(),
				collectionIds);
		getLogger().trace("reconcile tree2");
		resourceCollectionService.reconcileCollectionTree(getSharedResourceCollections(), getAuthenticatedUser(),
				collectionIds);

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
	public void setActiveResourceCount(Integer activeResourceCount) {
		this.activeResourceCount = activeResourceCount;
	}

	/**
	 * @return the activeResourceCount
	 */
	public Integer getActiveResourceCount() {
		return activeResourceCount;
	}

	private void initCounts() {
		ResourceTypeStatusInfo info = resourceService.getResourceCountAndStatusForUser(getAuthenticatedUser(),
				Arrays.asList(ResourceType.values()));
		activeResourceCount = info.getTotal();
		try {
			setStatusData(serializationService.convertToJson(info.getStatusData()));
			setResourceTypeData(serializationService.convertToJson(info.getResourceTypeData()));
		} catch (IOException e) {
			getLogger().error("e", e);
		}

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
			authorizationService.applyTransientViewableFlag(res, getAuthenticatedUser());
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
		boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
		editableProjects = new TreeSet<Resource>(
				projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));

		fullUserProjects = new ArrayList<Resource>(
				projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything));
		Collections.sort(fullUserProjects);
		allSubmittedProjects = projectService.findBySubmitter(getAuthenticatedUser());
		Collections.sort(allSubmittedProjects);
		fullUserProjects.removeAll(getAllSubmittedProjects());
		filteredFullUserProjects = new ArrayList<Resource>(getFullUserProjects());
		filteredFullUserProjects.removeAll(getAllSubmittedProjects());

	}

	public Set<Resource> getEditableProjects() {
		return editableProjects;
	}

	public void prepare() {
	}

	public List<Status> getStatuses() {
		return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
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

	public Set<BillingAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<BillingAccount> accounts) {
		this.accounts = accounts;
	}

	public Set<BillingAccount> getOverdrawnAccounts() {
		return overdrawnAccounts;
	}

	public void setOverdrawnAccounts(Set<BillingAccount> overdrawnAccounts) {
		this.overdrawnAccounts = overdrawnAccounts;
	}

	public List<InformationResource> getResourcesWithErrors() {
		return resourcesWithErrors;
	}

	public void setResourcesWithErrors(List<InformationResource> resourcesWithErrors) {
		this.resourcesWithErrors = resourcesWithErrors;
	}

	public List<Resource> getRecentResources() {
		return recentResources;
	}

	public void setRecentResources(List<Resource> recentResources) {
		this.recentResources = recentResources;
	}

	public List<Resource> getFeaturedResources() {
		return featuredResources;
	}

	public void setFeaturedResources(List<Resource> featuredResources) {
		this.featuredResources = featuredResources;
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

	public List<Person> getUserSuggestions() {
	    return entityService.findSimilarPeople(getAuthenticatedUser());
	}

	@Override
	public boolean isRightSidebar() {
	    return true;
	}
}
