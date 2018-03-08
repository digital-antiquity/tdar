package org.tdar.balk.struts.action;

import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.service.ItemService;

@ParentPackage("default")
@Namespace("/")
@Component
@Scope("prototype")
public class IndexAction extends AbstractAuthenticatedAction {


    private static final long serialVersionUID = 8069489623938108226L;

    @Autowired
    private ItemService itemService;

    private Set<String> topLevelPaths;

    private Set<String> topLevelManagedPaths;
    
    private boolean archived;

    
    @Override
    @Action(value="" , results={@Result(name=SUCCESS, type=FREEMARKER, location="index.ftl")})
    public String execute() throws Exception {
        topLevelPaths = itemService.listTopLevelPaths(archived);
        getLogger().debug("topLevelPaths:{}", topLevelPaths);
        topLevelManagedPaths = itemService.listTopLevelManagedPaths(archived);
        getLogger().debug("topLevelManagedPaths:{}", topLevelManagedPaths);
        return super.execute();
    }


    public Set<String> getTopLevelPaths() {
        return topLevelPaths;
    }


    public void setTopLevelPaths(Set<String> topLevelPaths) {
        this.topLevelPaths = topLevelPaths;
    }


    public Set<String> getTopLevelManagedPaths() {
        return topLevelManagedPaths;
    }


    public void setTopLevelManagedPaths(Set<String> topLevelManagedPaths) {
        this.topLevelManagedPaths = topLevelManagedPaths;
    }


    public boolean isArchived() {
        return archived;
    }


    public void setArchived(boolean archived) {
        this.archived = archived;
    }
    
    
}
