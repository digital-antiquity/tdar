package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.util.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.HomepageResourceCountCache;

/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/")
@ParentPackage("default")
@Component
@Scope("prototype")
@Results({
        @Result(name = "authenticated", type = "redirect", location = "/")
})
public class IndexAction extends AuthenticationAware.Base {
    private static final long serialVersionUID = -9216882130992021384L;

    private Project featuredProject;

    private List<HomepageGeographicKeywordCache> geographicKeywordCache = new ArrayList<HomepageGeographicKeywordCache>();
    private List<HomepageResourceCountCache> homepageResourceCountCache = new ArrayList<HomepageResourceCountCache>();
    private InformationResource featuredResource;

    @Override
    @Action(results = {
            @Result(name = "success", location = "about.ftl")
    })
    public String execute() {
        return about();
    }

    @Actions({
            @Action("terms"),
            @Action("contact"),
            @Action(value = "page-not-found", results = { @Result(name = SUCCESS, location = "errors/page-not-found.ftl") }),
            @Action(value = "access-denied", results = { @Result(name = SUCCESS, location = "errors/access-denied.ftl") })
    })
    public String passThrough() {
        return SUCCESS;
    }

    @Action("about")
    public String about() {
        // setFeaturedProject(getGenericService().findRandom(Project.class, 1).get(0));
        getGeographicKeywordCache().addAll(getGenericService().findAll(HomepageGeographicKeywordCache.class));
        getHomepageResourceCountCache().addAll(getGenericService().findAll(HomepageResourceCountCache.class));
        // setFeaturedResource((InformationResource) getInformationResourceService().findRandomFeaturedResource(true, 1).get(0));
        return SUCCESS;
    }

    @Action("login")
    public String login() {
        if (isAuthenticated()) {
            return TdarActionSupport.AUTHENTICATED;
        }
        return SUCCESS;

    }

    @Action(value = "logout",
            results = {
                    @Result(name = "success", type = "redirect", location = "/")
            })
    public String logout() {
        if (getSessionData().isAuthenticated()) {
            clearAuthenticationToken();
            getAuthenticationAndAuthorizationService().getAuthenticationProvider().logout(getServletRequest(), getServletResponse());
        }
        return SUCCESS;
    }

    public Project getFeaturedProject() {
        return featuredProject;
    }

    public void setFeaturedProject(Project featuredProject) {
        this.featuredProject = featuredProject;
    }

    public InformationResource getFeaturedResource() {
        return featuredResource;
    }

    public void setFeaturedResource(InformationResource featuredResource) {
        this.featuredResource = featuredResource;
    }

    public List<HomepageGeographicKeywordCache> getGeographicKeywordCache() {
        return geographicKeywordCache;
    }

    public void setGeographicKeywordCache(List<HomepageGeographicKeywordCache> geographicKeywordCache) {
        this.geographicKeywordCache = geographicKeywordCache;
    }

    public List<HomepageResourceCountCache> getHomepageResourceCountCache() {
        return homepageResourceCountCache;
    }

    public void setHomepageResourceCountCache(List<HomepageResourceCountCache> homepageResourceCountCache) {
        this.homepageResourceCountCache = homepageResourceCountCache;
    }

}
