package org.tdar.struts.action.browse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.bean.SearchFieldType;
import org.tdar.search.exception.SearchPaginationException;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * Controller for browsing resources.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/browse")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpOnlyIfUnauthenticated
public class BrowseCollectionController extends AbstractLookupController<ResourceCollection> {

    public static final String COLLECTIONS = "collections";
    public static final String EXPLORE = "explore";

    private static final long serialVersionUID = -128651515783098910L;
    private Persistable persistable;
    private Long viewCount = 0L;
    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private List<String> groups = new ArrayList<String>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    @Autowired
    private CollectionSearchService collectionSearchService;
    
    Map<String, SearchFieldType> searchFieldLookup = new HashMap<>();

    private transient InputStream inputStream;
    private Long contentLength;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient ResourceService resourceService;

    @Action(COLLECTIONS)
    public String browseCollections() throws TdarActionException {
        performLuceneQuery();

        if (isEditor()) {
            List<Long> collectionIds = PersistableUtils.extractIds(resourceCollectionService.findDirectChildCollections(getId(), null, SharedCollection.class));
            setUploadedResourceAccessStatistic(resourceService.getSpaceUsageForCollections(collectionIds, Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        }

        return SUCCESS;
    }

    private void performLuceneQuery() throws TdarActionException {
        setMode("browseCollections");
        try {
            CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
            csqo.setAllFields(null);
            csqo.setLimitToTopLevel(true);
            collectionSearchService.buildResourceCollectionQuery(getAuthenticatedUser(), csqo, this, this);
        } catch (SearchPaginationException spe) {
            throw new TdarActionException(StatusCode.NOT_FOUND, spe);
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception", tdre);
            addActionError(tdre.getMessage());
        } catch (ParseException e) {
            getLogger().warn("search parse exception", e);
        } catch (SolrServerException e) {
            getLogger().warn("search parse exception", e);
        } catch (IOException e) {
            getLogger().warn("search parse exception", e);
        }
        setSearchDescription(getText("browseController.all_tdar_collections"));
        setSearchTitle(getText("browseController.all_tdar_collections"));
    }

    public ResourceSpaceUsageStatistic getUploadedResourceAccessStatistic() {
        return uploadedResourceAccessStatistic;
    }

    public void setUploadedResourceAccessStatistic(ResourceSpaceUsageStatistic uploadedResourceAccessStatistic) {
        this.uploadedResourceAccessStatistic = uploadedResourceAccessStatistic;
    }

    public ResourceSpaceUsageStatistic getTotalResourceAccessStatistic() {
        return totalResourceAccessStatistic;
    }

    public void setTotalResourceAccessStatistic(ResourceSpaceUsageStatistic totalResourceAccessStatistic) {
        this.totalResourceAccessStatistic = totalResourceAccessStatistic;
    }


    public Persistable getPersistable() {
        return persistable;
    }

    public void setPersistable(Persistable persistable) {
        this.persistable = persistable;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }


    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean isShowAdminInfo() {
        return isAuthenticated() && (isEditor() || Objects.equals(getId(), getAuthenticatedUser().getId()));
    }

    public boolean isShowBasicInfo() {
        return isAuthenticated() && (isEditor() || Objects.equals(getId(), getAuthenticatedUser().getId()));
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

}
