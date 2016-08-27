package org.tdar.struts.action.collection.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@Namespace("/collection/admin/batch")
public class CollectionBatchAction extends AbstractCollectionAdminAction implements Preparable, Validateable {

    private static final long serialVersionUID = 1L;

    private Long accountId;
    private BillingAccount account;

    private Long projectId;
    private Project project;
    private Long collectionId;
    private ResourceCollection collectionToAdd;

    private List<Long> ids = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private List<String> descriptions = new ArrayList<>();
    private List<Integer> dates = new ArrayList<>();
    private List<Resource> fullUserProjects;
    private List<Project> allSubmittedProjects;
    private List<Resource> resources = new ArrayList<>();
    @Autowired
    private GenericService genericService;
    @Autowired
    private BillingAccountService billingAccountService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private ResourceService resourceService;

    private List<BillingAccount> availableAccounts;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        // COMMENTED OUT UNTIL WE FIGURE OUT What sort of collection should support this
//        setResources(new ArrayList<>(getCollection().getResources()));
        Collections.sort(resources , new Comparator<Resource>() {
            @Override
            public int compare(Resource o1, Resource o2) {
                return PersistableUtils.compareIds(o1, o2);
            }
        });
        setAllSubmittedProjects(projectService.findBySubmitter(getAuthenticatedUser()));
        Collections.sort(getAllSubmittedProjects());
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        setFullUserProjects(new ArrayList<Resource>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), canEditAnything)));
        getFullUserProjects().removeAll(getAllSubmittedProjects());
        setAvailableAccounts(billingAccountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));

        if (PersistableUtils.isNotNullOrTransient(projectId)) {
            project = genericService.find(Project.class, projectId);
        }
        if (PersistableUtils.isNotNullOrTransient(accountId)) {
            account = billingAccountService.find(accountId);
        }
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
            collectionToAdd = genericService.find(ResourceCollection.class, collectionId);
        }
    }

    @Override
    public void validate() {
        if (PersistableUtils.isNotNullOrTransient(project) && !authorizationService.canEdit(getAuthenticatedUser(), project)) {
            addActionError("error.permission_denied");
        }
        if (PersistableUtils.isNotNullOrTransient(account) && !authorizationService.canEdit(getAuthenticatedUser(), account)) {
            addActionError("error.permission_denied");
        }
        if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !authorizationService.canAdministerCollection(getAuthenticatedUser(), collectionToAdd)) {
            addActionError("error.permission_denied");
        }
        super.validate();
    }
    
    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "../batch.ftl"),
    })
    public String execute() throws Exception {

        return SUCCESS;
    }

    @Action(value = "save",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = REDIRECT, location = "${collection.detailUrl}"),
            })
    @PostOnly
    @WriteableSession
    public String save() throws Exception {
        resourceService.updateBatch(project, account, collectionToAdd, ids, dates, titles, descriptions, getAuthenticatedUser());
        return SUCCESS;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<Integer> getDates() {
        return dates;
    }

    public void setDates(List<Integer> dates) {
        this.dates = dates;
    }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public void setFullUserProjects(List<Resource> fullUserProjects) {
        this.fullUserProjects = fullUserProjects;
    }

    public List<Project> getAllSubmittedProjects() {
        return allSubmittedProjects;
    }

    public void setAllSubmittedProjects(List<Project> allSubmittedProjects) {
        this.allSubmittedProjects = allSubmittedProjects;
    }

    public List<BillingAccount> getAvailableAccounts() {
        return availableAccounts;
    }

    public void setAvailableAccounts(List<BillingAccount> availableAccounts) {
        this.availableAccounts = availableAccounts;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public ResourceCollection getCollectionToAdd() {
        return collectionToAdd;
    }

    public void setCollectionToAdd(ResourceCollection collectionToAdd) {
        this.collectionToAdd = collectionToAdd;
    }

}
