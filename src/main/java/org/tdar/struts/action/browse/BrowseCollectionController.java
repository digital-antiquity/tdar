package org.tdar.struts.action.browse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.FileSystemResourceService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.search.SearchFieldType;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

/**
 * $Id$
 * 
 * Controller for browsing resources.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
@Namespace("/browse")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpOnlyIfUnauthenticated
public class BrowseCollectionController extends AbstractLookupController {

    public static final String FOAF_XML = ".foaf.xml";
    public static final String SLASH = "/";
    public static final String XML = ".xml";
    public static final String CREATORS = "creators";
    public static final String COLLECTIONS = "collections";
    public static final String EXPLORE = "explore";

    private static final long serialVersionUID = -128651515783098910L;
    private Creator creator;
    private Persistable persistable;
    private Long viewCount = 0L;
    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private List<String> groups = new ArrayList<String>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    private List<Account> accounts = new ArrayList<Account>();
    Map<String, SearchFieldType> searchFieldLookup = new HashMap<>();

    private transient InputStream inputStream;
    private Long contentLength;

    @Autowired
    private transient AccountService accountService;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient AuthenticationService authenticationService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient FileSystemResourceService fileSystemResourceService;

    @Autowired
    private transient ResourceService resourceService;

    public Creator getAuthorityForDup() {
        return entityService.findAuthorityFromDuplicate(creator);
    }

    @Action(COLLECTIONS)
    public String browseCollections() throws TdarActionException {
        performLuceneQuery();

        if (isEditor()) {
            setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(null, null,
                    Persistable.Base.extractIds(resourceCollectionService.findDirectChildCollections(getId(), null, CollectionType.SHARED)), null,
                    Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        }

        return SUCCESS;
    }

    private void performLuceneQuery() throws TdarActionException {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart<CollectionType>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.COLLECTION_HIDDEN, Boolean.FALSE));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, Boolean.TRUE));
        setMode("browseCollections");
        setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        try {
            handleSearch(qb);
        } catch (SearchPaginationException spe) {
            throw new TdarActionException(StatusCode.NOT_FOUND, spe);
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception", tdre);
            addActionError(tdre.getMessage());
        } catch (ParseException e) {
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

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
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

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
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
