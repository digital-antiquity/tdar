package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageFeaturedItemCache;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;

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
    private Resource featuredResource;

    @Override
    @Actions({
            @Action("terms"),
            @Action("contact"),
            @Action(value = "page-not-found", results = { @Result(name = SUCCESS, location = "errors/page-not-found.ftl") }),
            @Action(value = "access-denied", results = { @Result(name = SUCCESS, location = "errors/access-denied.ftl") })
    })
    public String execute() {
        return SUCCESS;
    }
    
//    @Action(value = "oai", results = {
//            @Result(name = SUCCESS_GET_RECORD, location = "getRecord.ftl", type = "freemarker", params = {
//                    "contentType", "text/xml" }),

    @Actions({
            @Action(value="opensearch", results = {
                    @Result(name = "success", location = "opensearch.ftl", type = "freemarker", params = { "contentType", "application/xml" })
            })
    })
    public String emptyText() {
        return SUCCESS;
    }

    @Actions({
            @Action("about"),
            @Action(results = {
                    @Result(name = "success", location = "about.ftl")
            })
    })
    public String about() {
        setGeographicKeywordCache(getGenericService().findAll(HomepageGeographicKeywordCache.class));
        setHomepageResourceCountCache(getGenericService().findAll(HomepageResourceCountCache.class));
        try {
            setFeaturedResource(getGenericService().findAll(HomepageFeaturedItemCache.class).get(0).getKey());
        } catch (IndexOutOfBoundsException ioe) {
            logger.debug("no featured resources found");
        }
        if (getFeaturedResource() instanceof InformationResource) {
            getAuthenticationAndAuthorizationService().setTransientViewableStatus((InformationResource) getFeaturedResource(), null);
        }
        Iterator<HomepageResourceCountCache> iterator = homepageResourceCountCache.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getResourceType().isSupporting()) {
                iterator.remove();
            }
        }
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

    public Resource getFeaturedResource() {
        return featuredResource;
    }

    public void setFeaturedResource(Resource featuredResource) {
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
