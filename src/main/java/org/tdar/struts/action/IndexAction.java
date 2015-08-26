package org.tdar.struts.action;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.HomepageService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.RssService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.utils.PersistableUtils;

import com.rometools.rome.feed.synd.SyndEntry;

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
public class IndexAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -4095866074424122972L;

    private Project featuredProject;

    private List<HomepageResourceCountCache> homepageResourceCountCache = new ArrayList<>();
    private List<Resource> featuredResources = new ArrayList<>();
    private HashMap<String, HomepageGeographicKeywordCache> worldMapData = new HashMap<>();
    private ResourceCollection featuredCollection;

    private String sitemapFile = "sitemap_index.xml";

    @Autowired
    private RssService rssService;
    
    @Autowired
    private HomepageService homepageService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private ResourceService resourceService;


    private List<SyndEntry> rssEntries;


    @Actions(value={
            @Action(value = "", results = { @Result(name = SUCCESS, location = "about.ftl") })
    })
    @SkipValidation
    @HttpOnlyIfUnauthenticated
    public String about() {

        try {
            worldMapData = resourceService.setupWorldMap();
            featuredResources = new ArrayList<>(homepageService.featuredItems(getAuthenticatedUser()));
            homepageResourceCountCache = homepageService.resourceStats();
            setFeaturedCollection(resourceCollectionService.getRandomFeaturedCollection());        
        } catch (Exception e) {
            getLogger().error("exception in setting up homepage: {}", e,e);
        }
        try {
            setRssEntries(rssService.parseFeed(new URL(getTdarConfiguration().getNewsRssFeed())));
        } catch (Exception e) {
            getLogger().warn("RssParsingException happened", e);
        }
        return SUCCESS;
    }


    public Project getFeaturedProject() {
        return featuredProject;
    }

    public void setFeaturedProject(Project featuredProject) {
        this.featuredProject = featuredProject;
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

    public HashMap<String, HomepageGeographicKeywordCache> getWorldMapData() {
        return worldMapData;
    }

    public void setWorldMapData(HashMap<String, HomepageGeographicKeywordCache> worldMapData) {
        this.worldMapData = worldMapData;
    }

    public String getSitemapFile() {
        return sitemapFile;
    }

    public void setSitemapFile(String sitemapFile) {
        this.sitemapFile = sitemapFile;
    }

    public ResourceCollection getFeaturedCollection() {
        return featuredCollection;
    }

    public void setFeaturedCollection(ResourceCollection featuredCollection) {
        this.featuredCollection = featuredCollection;
    }

    public boolean isLogoAvailable() {
        if (PersistableUtils.isNullOrTransient(getFeaturedCollection())) {
            return false;
        }
        return checkLogoAvailable(ObjectType.COLLECTION, getFeaturedCollection().getId(), VersionType.WEB_SMALL);
    }

    @Override
    public boolean isNavSearchBoxVisible() {
        return false;
    }
    
    public boolean isHomepage() {
        return true;
    }
    
    @Override
    public boolean isSubnavEnabled() {
        return false;
    }
}
