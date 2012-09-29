package org.tdar.struts.action;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceRevisionLog;

/**
 * $Id$
 * 
 * Administrative actions (that shouldn't be available for wide use).
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured-admin")
@Namespace("/admin")
// class-wide results
@Results({
    
})
@Component
@Scope("prototype")
public class AdminController extends AuthenticationAware.Base {

    private static final long serialVersionUID = 4385039298623767568L;
    
    private List<Project> allProjects;
    
    private List<ContributorRequest> pendingContributorRequests;
    
    private List<ResourceRevisionLog> resourceRevisionLogs;
    
    @Actions({
        @Action("contributors"),
        @Action("internal"),
        @Action("activity")
    })
    public String execute() {
        return SUCCESS;
    }
    
    public List<Project> getAllProjects() {
        if (allProjects == null) {
            allProjects = getProjectService().findAllSorted();
        }
        return allProjects; 
    }

    public List<ContributorRequest> getPendingContributorRequests() {
        if (pendingContributorRequests == null) {
            pendingContributorRequests = getEntityService().findAllPendingContributorRequests();
        }
        return pendingContributorRequests;
    }

    public List<ResourceRevisionLog> getResourceRevisionLogs() {
        if (resourceRevisionLogs == null) {
            resourceRevisionLogs = getGenericService().findAllSorted(ResourceRevisionLog.class, "timestamp desc");
        }
        return resourceRevisionLogs;
    }
}
