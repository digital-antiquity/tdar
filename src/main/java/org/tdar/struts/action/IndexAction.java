package org.tdar.struts.action;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageFeaturedItemCache;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.RssService;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.sun.syndication.feed.synd.SyndEntry;

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
    private List<Resource> featuredResources = new ArrayList<Resource>();

    @Autowired
    private RssService rssService;

    private List<SyndEntry> rssEntries;

    private String sitemapFile = "sitemap_index.xml";

    
    @HttpOnlyIfUnauthenticated
    @Actions({
        @Action(value = "page-not-found", results = { @Result(name = ERROR, location = "errors/page-not-found.ftl") }),
        @Action(value = "not-found", results = { @Result(name = ERROR, location = "errors/page-not-found.ftl") }),
        @Action(value = "gone", results = { @Result(name = ERROR, location = "errors/resource-deleted.ftl") }),
        @Action(value = "unauthorized", results = { @Result(name = ERROR, location = "errors/unauthorized.ftl") }),
        @Action(value = "access-denied", results = { @Result(name = ERROR, location = "errors/access-denied.ftl") }),
        @Action(value = "invalid-token", results = { @Result(name = ERROR, location = "errors/double-submit.ftl") })
    })
    public String error() {
        return ERROR;
    }

    @HttpOnlyIfUnauthenticated
    @Override
    @Actions({
            @Action("terms"),
            @Action("contact"),
            @Action("credit"),
            @Action(value = "opensearch", results = {
                    @Result(name = SUCCESS, location = "opensearch.ftl", type = "freemarker", params = { "contentType", "application/xml" })
            }),
            @Action(value = "robots", results = {
                    @Result(name = SUCCESS, location = "robots.ftl", type = "freemarker", params = { "contentType", "text/plain" })
            })
    })
    public String execute() {
        File file = new File(getTdarConfiguration().getSitemapDir(), sitemapFile);
        if (!file.exists()) {
            setSitemapFile("sitemap1.xml.gz");
        }
        return SUCCESS;
    }

    @Actions({
            @Action("about"),
            @Action(results = {
                    @Result(name = SUCCESS, location = "about.ftl")
            }),
            @Action(value = "featured", results = { @Result(name = SUCCESS, location = "featured.ftl", type = "freemarker", params = { "contentType",
                    "text/html" }) }),
            @Action(value = "map", results = { @Result(name = SUCCESS, location = "map.ftl", type = "freemarker", params = { "contentType", "text/html" }) })
    })
    @HttpOnlyIfUnauthenticated
    public String about() {
        setGeographicKeywordCache(getGenericService().findAll(HomepageGeographicKeywordCache.class));
        setHomepageResourceCountCache(getGenericService().findAll(HomepageResourceCountCache.class));
        try {
            setRssEntries(rssService.parseFeed(new URL(getTdarConfiguration().getNewsRssFeed())));
        } catch (Exception e) {
            getLogger().warn("RssParsingException happened", e);
        }
        try {
            for (HomepageFeaturedItemCache cache : getGenericService().findAll(HomepageFeaturedItemCache.class)) {
                Resource key = cache.getKey();
                if (key instanceof InformationResource) {
                    getAuthenticationAndAuthorizationService().applyTransientViewableFlag((InformationResource) key, null);
                }
                if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
                    getObfuscationService().obfuscate(key, getAuthenticatedUser());
                }
                getFeaturedResources().add(key);
            }
        } catch (IndexOutOfBoundsException ioe) {
            getLogger().debug("no featured resources found");
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
    @HttpsOnly
    public String login() {
        if (isAuthenticated()) {
            return TdarActionSupport.AUTHENTICATED;
        }
        return SUCCESS;

    }

    @Action(value = "logout",
            results = {
                    @Result(name = SUCCESS, type = "redirect", location = "/")
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

    public List<Resource> getFeaturedResources() {
        return featuredResources;
    }

    public void setFeaturedResources(List<Resource> featuredResources) {
        this.featuredResources = featuredResources;
    }

    public List<SyndEntry> getRssEntries() {
        return rssEntries;
    }

    public void setRssEntries(List<SyndEntry> rssEntries) {
        this.rssEntries = rssEntries;
    }

    public String getSitemapFile() {
        return sitemapFile;
    }

    public void setSitemapFile(String sitemapFile) {
        this.sitemapFile = sitemapFile;
    }

}
