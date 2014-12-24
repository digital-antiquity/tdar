package org.tdar.struts.action.browse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.CreatorViewStatistic;
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
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.search.SearchService;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.SlugViewAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.search.SearchFieldType;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.utils.PersistableUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import com.opensymphony.xwork2.Preparable;

import freemarker.ext.dom.NodeModel;

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
@Namespace("/browse/creators")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpOnlyIfUnauthenticated
@Results(value = { @Result(location = "../creators.ftl"),
        @Result(name = TdarActionSupport.BAD_SLUG, type = TdarActionSupport.REDIRECT,
                location = "${creator.id}/${creator.slug}${slugSuffix}", params = { "ignoreParams", "id,slug" })
})
public class BrowseCreatorController extends AbstractLookupController<Resource> implements Preparable, SlugViewAction {

    /**
     * 
     */
    private static final long serialVersionUID = 7004124945674660779L;
    public static final String FOAF_XML = ".foaf.xml";
    public static final String SLASH = "/";
    public static final String XML = ".xml";
    public static final String CREATORS = "creators";
    public static final String EXPLORE = "explore";

    private Creator creator;
    private Persistable persistable;
    private Long viewCount = 0L;
    private List<String> groups = new ArrayList<String>();

    private String creatorXml;
    private List<Account> accounts = new ArrayList<Account>();
    Map<String, SearchFieldType> searchFieldLookup = new HashMap<>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    private transient InputStream inputStream;
    private Long contentLength;
    private Document dom;
    private float keywordMedian = 0;
    private float creatorMedian = 0;
    private float creatorMean = 0;
    private float keywordMean = 0;
    private List<NodeModel> keywords;
    private List<NodeModel> collaborators;
    private String slug = "";
    private String slugSuffix = "";
    private boolean redirectBadSlug;

    @Autowired
    private transient AccountService accountService;

    @Autowired
    private transient AuthorizationService authorizationService;

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

    @Action(value = "creatorRdf", results = {
            @Result(name = TdarActionSupport.SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/rdf+xml",
                            "inputName", "inputStream",
                            "contentLength", "${contentLength}"
                    }
            )
    })
    public String creatorRdf() throws FileNotFoundException {
        try {
            FileStoreFile object = new FileStoreFile(ObjectType.CREATOR, VersionType.METADATA, getId(), getId() + FOAF_XML);
            File file = getTdarConfiguration().getFilestore().retrieveFile(ObjectType.CREATOR, object);
            if (file.exists()) {
                setInputStream(new FileInputStream(file));
                setContentLength(file.length());
                return SUCCESS;
            }
        } catch (FileNotFoundException fnf) {
            return NOT_FOUND;
        } catch (Exception e) {
            return ERROR;
        }
        return ERROR;
    }

    public boolean isEditable() {
        if (isEditorOrSelf()) {
            return true;
        }
        if (creator.getCreatorType().isInstitution()) {
            return authorizationService.canEdit(getAuthenticatedUser(), (Institution) creator);
        }
        return false;
    }

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            creator = getGenericService().find(Creator.class, getId());
        } else {
            addActionError(getText("browseCreatorController.creator_does_not_exist"));
        }
        if (PersistableUtils.isNullOrTransient(creator)) {
            getLogger().debug("not found -- {}", creator);
            throw new TdarActionException(StatusCode.NOT_FOUND, "Creator page does not exist");
        }

        if (PersistableUtils.isTransient(getAuthenticatedUser()) && !creator.isBrowsePageVisible() && !Objects.equals(getAuthenticatedUser(), creator)) {
            throw new TdarActionException(StatusCode.UNAUTHORIZED, "Creator page does not exist");
        }
        if (!handleSlugRedirect(creator, this)) {
            redirectBadSlug = true;
        } else {
            prepareLuceneQuery();
        }
    }

    @Actions(value = {
            @Action(value = "{id}"),
            @Action(value = "{id}/{slug}")
    })
    public String browseCreators() throws ParseException, TdarActionException {
        if (redirectBadSlug) {
            return BAD_SLUG;
        }

        if (isEditor()) {
            if ((creator instanceof TdarUser) && StringUtils.isNotBlank(((TdarUser) creator).getUsername())) {
                TdarUser person = (TdarUser) creator;
                try {
                    getGroups().addAll(authenticationService.getGroupMembership(person));
                } catch (Throwable e) {
                    getLogger().error("problem communicating with crowd getting user info for {} {}", creator, e);
                }
                getAccounts().addAll(
                        accountService.listAvailableAccountsForUser(person, Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
            }
            try {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(Arrays.asList(getId()), null, null, null, null));
            } catch (Exception e) {
                getLogger().error("unable to set resource access statistics", e);
            }
            setViewCount(entityService.getCreatorViewCount(creator));
        }

        if (!isEditor() && !PersistableUtils.isEqual(creator, getAuthenticatedUser())) {
            CreatorViewStatistic cvs = new CreatorViewStatistic(new Date(), creator);
            getGenericService().saveOrUpdate(cvs);
        }

        FileStoreFile personInfo = new FileStoreFile(ObjectType.CREATOR, VersionType.METADATA, getId(), getId() + XML);
        try {
            File foafFile = getTdarConfiguration().getFilestore().retrieveFile(ObjectType.CREATOR, personInfo);
            if (foafFile.exists()) {
                dom = fileSystemResourceService.openCreatorInfoLog(foafFile);
                getKeywords();
                getCollaborators();
                NamedNodeMap attributes = dom.getElementsByTagName("creatorInfoLog").item(0).getAttributes();
                // getLogger().info("attributes: {}", attributes);
                setKeywordMedian(Float.parseFloat(attributes.getNamedItem("keywordMedian").getTextContent()));
                setKeywordMean(Float.parseFloat(attributes.getNamedItem("keywordMean").getTextContent()));
                setCreatorMedian(Float.parseFloat(attributes.getNamedItem("creatorMedian").getTextContent()));
                setCreatorMean(Float.parseFloat(attributes.getNamedItem("creatorMean").getTextContent()));
            }
        } catch (FileNotFoundException fnf) {
            getLogger().trace("{} does not exist in filestore", personInfo.getFilename());
        } catch (Exception e) {
            getLogger().debug("error", e);
        }
        // reset fields which can be broken by the searching hydration obfuscating things
        creator = getGenericService().find(Creator.class, getId());
        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private void prepareLuceneQuery() throws TdarActionException {
        QueryBuilder queryBuilder = searchService.generateQueryForRelatedResources(creator, getAuthenticatedUser(), this);
        setPersistable(creator);
        setMode("browseCreators");
        setSortField(SortOption.RESOURCE_TYPE);
        if (PersistableUtils.isNotNullOrTransient(creator)) {
            String descr = getText("browseController.all_resource_from", creator.getProperName());
            setSearchDescription(descr);
            setSearchTitle(descr);
            setRecordsPerPage(50);
            try {
                setProjectionModel(ProjectionModel.RESOURCE_PROXY);
                handleSearch(queryBuilder);
                bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());

            } catch (SearchPaginationException spe) {
                throw new TdarActionException(StatusCode.NOT_FOUND, spe);
            } catch (TdarRecoverableRuntimeException tdre) {
                getLogger().warn("search parse exception", tdre);
                addActionError(tdre.getMessage());
            } catch (ParseException e) {
                getLogger().warn("search parse exception", e);
            }

        }
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
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

    public String getCreatorXml() {
        return creatorXml;
    }

    public void setCreatorXml(String creatorXml) {
        this.creatorXml = creatorXml;
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

    public Map<String, SearchFieldType> getKeywordTypeBySimpleName() {
        if (CollectionUtils.isEmpty(searchFieldLookup.keySet())) {
            for (SearchFieldType type : SearchFieldType.values()) {
                if (type.getAssociatedClass() != null) {
                    searchFieldLookup.put(type.getAssociatedClass().getSimpleName(), type);
                }
            }
        }
        return searchFieldLookup;
    }

    public List<NodeModel> getCollaborators() throws TdarActionException {
        if (collaborators != null) {
            return collaborators;
        }
        try {
            collaborators = fileSystemResourceService.parseCreatorInfoLog("creatorInfoLog/collaborators/*", false, getCreatorMean(), getSidebarValuesToShow(),
                    dom);
        } catch (TdarRecoverableRuntimeException trre) {
            getLogger().warn(trre.getLocalizedMessage());
        }
        return collaborators;
    }

    public List<NodeModel> getKeywords() throws TdarActionException {
        if (keywords != null) {
            return keywords;
        }
        try {
            keywords = fileSystemResourceService.parseCreatorInfoLog("creatorInfoLog/keywords/*", true, getKeywordMean(), getSidebarValuesToShow(), dom);
        } catch (TdarRecoverableRuntimeException trre) {
            getLogger().warn(trre.getLocalizedMessage());
        }
        return keywords;
    }

    public float getKeywordMedian() {
        return keywordMedian;
    }

    public void setKeywordMedian(float keywordMedian) {
        this.keywordMedian = keywordMedian;
    }

    public float getCreatorMedian() {
        return creatorMedian;
    }

    public void setCreatorMedian(float creatorMedian) {
        this.creatorMedian = creatorMedian;
    }

    public float getCreatorMean() {
        return creatorMean;
    }

    public void setCreatorMean(float creatorMean) {
        this.creatorMean = creatorMean;
    }

    public float getKeywordMean() {
        return keywordMean;
    }

    public void setKeywordMean(float keywordMean) {
        this.keywordMean = keywordMean;
    }

    public int getSidebarValuesToShow() {
        int num = getResults().size();
        // start with how many records are being shown on the current page
        if (num > getRecordsPerPage()) {
            num = getRecordsPerPage();
        }
        // if less than 20, then show 20
        if (num < 20) {
            num = 20;
        }
        num = (int) Math.ceil(num / 2.0);
        return num;
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

    public ResourceSpaceUsageStatistic getUploadedResourceAccessStatistic() {
        return uploadedResourceAccessStatistic;
    }

    public void setUploadedResourceAccessStatistic(ResourceSpaceUsageStatistic uploadedResourceAccessStatistic) {
        this.uploadedResourceAccessStatistic = uploadedResourceAccessStatistic;
    }

    public boolean isEditorOrSelf() {
        if (isEditor() || getCreator().equals(getAuthenticatedUser())) {
            return true;
        }
        return false;
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return DEFAULT_RESULT_SIZE;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override
    public String getSlugSuffix() {
        return slugSuffix;
    }

    @Override
    public void setSlugSuffix(String slugSuffix) {
        this.slugSuffix = slugSuffix;
    }

    public boolean isLogoAvailable() {
        return checkLogoAvailable(ObjectType.CREATOR, getId(), VersionType.WEB_SMALL);
    }
}
