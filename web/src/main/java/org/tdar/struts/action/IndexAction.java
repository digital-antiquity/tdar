package org.tdar.struts.action;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.HomepageService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.RssService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.service.query.ResourceSearchService;
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
public class IndexAction extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = -4095866074424122972L;

    private Project featuredProject;

    private List<Resource> featuredResources = new ArrayList<>();

    private ResourceCollection featuredCollection;

    private String sitemapFile = "sitemap_index.xml";

    @Autowired
    private RssService rssService;
    
    @Autowired
    private HomepageService homepageService;
    @Autowired
    private ResourceSearchService resourceSearchService;
    
    @Autowired
    private ResourceCollectionService resourceCollectionService;

    private List<SyndEntry> rssEntries;

    private String mapJson;

    private String homepageResourceCountCache;


    @Actions(value={
            @Action(value = "", results = { @Result(name = SUCCESS, location = "about.ftl") }),
            @Action(value = "about", results = { @Result(name = SUCCESS, location = "about.ftl") }),

    })
    @SkipValidation
    @HttpOnlyIfUnauthenticated
    public String about() {
        AdvancedSearchQueryObject advancedSearchQueryObject = new AdvancedSearchQueryObject();
        SearchResult<Resource> result = new SearchResult<>();
        result.setFacetWrapper(new FacetWrapper());
        result.getFacetWrapper().setMapFacet(true);
        result.setRecordsPerPage(0);
        try {
            resourceSearchService.buildAdvancedSearch(advancedSearchQueryObject, getAuthenticatedUser(), result, this);
            setMapJson(result.getFacetWrapper().getFacetPivotJson());
        } catch (SolrServerException | IOException | ParseException e1) {
            setMapJson(homepageService.getMapJson());
            getLogger().error("issue generating map json");
        }
        
        featuredResources = new ArrayList<>(homepageService.featuredItems(getAuthenticatedUser()));
        setHomepageResourceCountCache(homepageService.getResourceCountsJson());
        try {
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

    public String getHomepageResourceCountCache() {
        return homepageResourceCountCache;
    }

    public void setHomepageResourceCountCache(String homepageResourceCountCache) {
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
        return checkLogoAvailable(FilestoreObjectType.COLLECTION, getFeaturedCollection().getId(), VersionType.WEB_SMALL);
    }

    @Override
    public boolean isNavSearchBoxVisible() {
        return false;
    }
    
    public boolean isHomepage() {
        return true;
    }


    public String getMapJson() {
        return mapJson;
    }


    public void setMapJson(String mapJson) {
        this.mapJson = mapJson;
    }
    
    @Override
    public boolean isSubnavEnabled() {
        return false;
    }
}
