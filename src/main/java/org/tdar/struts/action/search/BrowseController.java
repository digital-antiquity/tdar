package org.tdar.struts.action.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.cache.BrowseDecadeCountCache;
import org.tdar.core.bean.cache.BrowseYearCountCache;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.data.ResourceSpaceUsageStatistic;
import org.tdar.struts.interceptor.HttpOnlyIfUnauthenticated;

import freemarker.ext.dom.NodeModel;

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
@SuppressWarnings("rawtypes")
@Namespace("/browse")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpOnlyIfUnauthenticated
public class BrowseController extends AbstractLookupController {

    public static final String CREATORS = "creators";
    public static final String COLLECTIONS = "collections";
    public static final String EXPLORE = "explore";
    private static final String ALL_TDAR_COLLECTIONS = "All Collections";
    private static final long serialVersionUID = -128651515783098910L;
    private Creator creator;
    private Persistable persistable;
    private List<InvestigationType> investigationTypes = new ArrayList<InvestigationType>();
    private List<CultureKeyword> cultureKeywords = new ArrayList<CultureKeyword>();
    private List<SiteTypeKeyword> siteTypeKeywords = new ArrayList<SiteTypeKeyword>();
    private List<MaterialKeyword> materialTypes = new ArrayList<MaterialKeyword>();
    private List<String> alphabet = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
    private List<BrowseYearCountCache> scholarData;
    private List<BrowseDecadeCountCache> timelineData;
    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private List<String> groups = new ArrayList<String>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;
    private List<HomepageGeographicKeywordCache> geographicKeywordCache = new ArrayList<HomepageGeographicKeywordCache>();
    private List<HomepageResourceCountCache> homepageResourceCountCache = new ArrayList<HomepageResourceCountCache>();
    private String creatorXml;
    private List<Account> accounts = new ArrayList<Account>();

    private transient InputStream inputStream;
    private Long contentLength;
    private NodeModel nodeModel;

    // private Keyword keyword;

    @Action(EXPLORE)
    public String explore() {
        setGeographicKeywordCache(getGenericService().findAll(HomepageGeographicKeywordCache.class));
        setHomepageResourceCountCache(getGenericService().findAll(HomepageResourceCountCache.class));
        setMaterialTypes(getGenericKeywordService().findAllWithCache(MaterialKeyword.class));
        setInvestigationTypes(getGenericKeywordService().findAllWithCache(InvestigationType.class));
        setCultureKeywords(getGenericKeywordService().findAllApprovedWithCache(CultureKeyword.class));
        setSiteTypeKeywords(getGenericKeywordService().findAllApprovedWithCache(SiteTypeKeyword.class));
        setTimelineData(getGenericService().findAll(BrowseDecadeCountCache.class));
        setScholarData(getGenericService().findAll(BrowseYearCountCache.class));
        return SUCCESS;
    }

    public Creator getAuthorityForDup() {
        return getEntityService().findAuthorityFromDuplicate(creator);
    }

    @Action(COLLECTIONS)
    public String browseCollections() throws ParseException {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart<CollectionType>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.COLLECTION_VISIBLE, Boolean.TRUE));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, Boolean.TRUE));
        setMode("browseCollections");
        handleSearch(qb);
        setSearchDescription(ALL_TDAR_COLLECTIONS);
        setSearchTitle(ALL_TDAR_COLLECTIONS);

        if (isEditor()) {
            setUploadedResourceAccessStatistic(getResourceService().getResourceSpaceUsageStatistics(null, null,
                    Persistable.Base.extractIds(getResourceCollectionService().findAllDirectChildCollections(getId(), null, CollectionType.SHARED)), null,
                    Arrays.asList(Status.ACTIVE, Status.DRAFT)));
        }

        return SUCCESS;
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
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            creator = getGenericService().find(Creator.class, getId());
            File file = new File(TdarConfiguration.getInstance().getCreatorFOAFDir() + "/" + getId() + ".foaf.xml");
            setInputStream(new FileInputStream(file));
            setContentLength(file.length());
            return SUCCESS;
        }
        return ERROR;
    }

    @Action(value = CREATORS, results = { @Result(location = "results.ftl") })
    public String browseCreators() throws ParseException, TdarActionException {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            creator = getGenericService().find(Creator.class, getId());
            QueryBuilder queryBuilder = getSearchService().generateQueryForRelatedResources(creator, getAuthenticatedUser());

            try {
            setNodeModel(NodeModel.parse(new File(TdarConfiguration.getInstance().getCreatorFOAFDir() + "/" + getId() + ".xml")));
            } catch (Exception e) {
                logger.error("{}", e);
            }
            if (isEditor()) {
                if (creator instanceof Person && StringUtils.isNotBlank(((Person) creator).getUsername())) {
                    try {
                        getGroups().addAll(getAuthenticationAndAuthorizationService().getGroupMembership((Person) creator));
                    } catch (Throwable e) {
                        logger.error("problem communicating with crowd getting user info for {} ", creator, e);
                    }
                    getAccounts().addAll(
                            getAccountService().listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
                }
                try {
                    setUploadedResourceAccessStatistic(getResourceService().getResourceSpaceUsageStatistics(Arrays.asList(getId()), null, null, null, null));
                } catch (Exception e) {
                    logger.error("error: {}", e);
                }

            }

            setPersistable(creator);
            setMode("browseCreators");
            setSortField(SortOption.RESOURCE_TYPE);
            if (Persistable.Base.isNotNullOrTransient(creator)) {
                String descr = String.format("All Resources from %s", creator.getProperName());
                setSearchDescription(descr);
                setSearchTitle(descr);
                setRecordsPerPage(50);
                try {
                    handleSearch(queryBuilder);
                } catch (SearchPaginationException spe) {
                    throw new TdarActionException(StatusCode.BAD_REQUEST, spe);
                } catch (TdarRecoverableRuntimeException tdre) {
                    logger.warn("search parse exception: {}", tdre.getMessage());
                    addActionError(tdre.getMessage());
                } catch (ParseException e) {
                    logger.warn("search parse exception: {}", e.getMessage());
                }
            }
        }
        // reset fields which can be broken by the searching hydration obfuscating things
        creator = getGenericService().find(Creator.class, getId());
        return SUCCESS;
    }

    // @Action(value = "materials", results = { @Result(location = "results.ftl") })
    // public String browseMaterialTypes() {
    // setResults(getResourceService().findResourceLinkedValues(MaterialKeyword.class));
    // return SUCCESS;
    // }
    //
    // @Action(value = "places", results = { @Result(location = "results.ftl") })
    // public String browseInvestigationTypes() {
    // setResults(getResourceService().findResourceLinkedValues(InvestigationType.class));
    // return SUCCESS;
    // }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public List<SiteTypeKeyword> getSiteTypeKeywords() {
        return siteTypeKeywords;
    }

    public void setSiteTypeKeywords(List<SiteTypeKeyword> siteTypeKeywords) {
        this.siteTypeKeywords = siteTypeKeywords;
    }

    public List<CultureKeyword> getCultureKeywords() {
        return cultureKeywords;
    }

    public void setCultureKeywords(List<CultureKeyword> cultureKeywords) {
        this.cultureKeywords = cultureKeywords;
    }

    public List<InvestigationType> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(List<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public List<MaterialKeyword> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<MaterialKeyword> materialTypes) {
        this.materialTypes = materialTypes;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(List<String> alphabet) {
        this.alphabet = alphabet;
    }

    public List<BrowseDecadeCountCache> getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(List<BrowseDecadeCountCache> list) {
        this.timelineData = list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getProjections() {
        return ListUtils.EMPTY_LIST;
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

    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }

    public List<BrowseYearCountCache> getScholarData() {
        return scholarData;
    }

    public void setScholarData(List<BrowseYearCountCache> scholarData) {
        this.scholarData = scholarData;
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

    public NodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
    }

}
