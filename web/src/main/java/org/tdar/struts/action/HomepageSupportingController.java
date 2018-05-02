package org.tdar.struts.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.HomepageDetails;
import org.tdar.web.service.HomepageService;

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
@Results({
        @Result(name = "authenticated", type = TdarActionSupport.TDAR_REDIRECT, location = "/")
})
// FIXME: better name
public class HomepageSupportingController extends AbstractAuthenticatableAction {
    private static final long serialVersionUID = -9216882130992021384L;

    private Project featuredProject;

    private List<Resource> featuredResources = new ArrayList<Resource>();
    private ResourceCollection featuredCollection;

    private String sitemapFile = "sitemap_index.xml";
    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient HomepageService homepageService;

    private Boolean prefixHost = false;
    private List<SyndEntry> rssEntries;
    private HomepageDetails homepageGraphs;

    @Actions({
            @Action(value = "page-not-found", results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP,
                    location = "/WEB-INF/content/errors/page-not-found.ftl", params = { "status", "404" }) }),
            @Action(value = "not-found",
                    results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/page-not-found.ftl",
                            params = { "status", "404" }) }),
            @Action(value = "gone",
                    results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/resource-deleted.ftl",
                            params = { "status", "410" }) }),
            // used by the AuthenticationInterceptor which seems to not be able to work with the FreemarkerHttpResult properly
            @Action(value = TdarActionSupport.UNAUTHORIZED, results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP,
                    location = "/WEB-INF/content/errors/unauthorized.ftl",
                    params = { "status", "401" }) }),
            @Action(value = "access-denied",
                    results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/access-denied.ftl",
                            params = { "status", "403" }) }),
            @Action(value = "invalid-token",
                    results = { @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/double-submit.ftl",
                            params = { "status", "500" }) })
    })
    public String error() {
        return ERROR;
    }

    @HttpsOnly
    @Actions({
            @Action(value = "robots", results = {
                    @Result(name = SUCCESS, location = "robots.ftl", type = FREEMARKER, params = { "contentType", "text/plain" })
            })
    })
    @SkipValidation
    public String robots() {
        File file = new File(getTdarConfiguration().getSitemapDir(), sitemapFile);
        if (!file.exists()) {
            setSitemapFile("sitemap1.xml.gz");
        }

        return SUCCESS;
    }

    @HttpsOnly
    @Actions(value = {
            @Action(value = "terms", results = { @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = "${tosUrl}") }),
            @Action(value = "opensearch", results = {
                    @Result(name = SUCCESS, location = "opensearch.ftl", type = FREEMARKER, params = { "contentType", "application/xml" })
            }),
            @Action("credit"),
            @Action("contact")
    })
    @Override
    @SkipValidation
    public String execute() {
        return SUCCESS;
    }

    @Action(value = "map", results = { @Result(name = SUCCESS, location = "map.ftl", type = FREEMARKER, params = { "contentType", "text/html" }) })
    @SkipValidation
    public String worldMap() {
        setupMapGraphs();

        return SUCCESS;
    }

    @Action(value = "featured", results = { @Result(name = SUCCESS, location = "featured.ftl", type = FREEMARKER,
            params = { "contentType", "text/html" }) })
    @SkipValidation
    public String featuredItems() {
        featuredResources = new ArrayList<>(homepageService.featuredItems(getAuthenticatedUser()));
        featuredResources.forEach(r -> {
            if (r.getFirstLatitudeLongitudeBox() != null) {
                getGenericService().markReadOnly(r.getFirstActiveLatitudeLongitudeBox());
                r.getFirstLatitudeLongitudeBox().obfuscate();
            }
        });

        return SUCCESS;
    }

    @Action(value = "featuredCollection", results = { @Result(name = SUCCESS, location = "featuredCollection.ftl", type = FREEMARKER,
            params = { "contentType", "text/html" }) })
    public String featuredCollection() {
        setFeaturedCollection(resourceCollectionService.getRandomFeaturedCollection());
        return SUCCESS;
    }

    @Action(value = "resourceGraph", results = { @Result(name = SUCCESS, location = "resourceGraph.ftl", type = FREEMARKER,
            params = { "contentType", "text/html" }) })
    @SkipValidation
    public String resourceStats() {
        setupMapGraphs();
        return SUCCESS;
    }

    private void setupMapGraphs() {
        setHomepageGraphs(homepageService.getHomepageGraphs(getAuthenticatedUser(), null, isBot(), this));
    }

    public Project getFeaturedProject() {
        return featuredProject;
    }

    public void setFeaturedProject(Project featuredProject) {
        this.featuredProject = featuredProject;
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
        return false;
    }

    public HomepageDetails getHomepageGraphs() {
        return homepageGraphs;
    }

    public void setHomepageGraphs(HomepageDetails homepageGraphs) {
        this.homepageGraphs = homepageGraphs;
    }

    public Boolean getPrefixHost() {
        return prefixHost;
    }

    public void setPrefixHost(Boolean prefixHost) {
        this.prefixHost = prefixHost;
    }
}
