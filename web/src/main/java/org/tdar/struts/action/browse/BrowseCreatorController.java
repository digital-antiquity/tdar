package org.tdar.struts.action.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
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
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.bean.statistics.CreatorViewStatistic;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.search.bean.SearchFieldType;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchPaginationException;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.SlugViewAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

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
@HttpsOnly
@Results(value = { @Result(location = "../view-creator.ftl"),
        @Result(name = TdarActionSupport.BAD_SLUG, type = TdarActionSupport.TDAR_REDIRECT,
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
    private String logoUrl;
    private Creator creator;
    private Long viewCount = 0L;
    private List<String> groups = new ArrayList<String>();

    private String creatorXml;
    private List<BillingAccount> accounts = new ArrayList<BillingAccount>();
    Map<String, SearchFieldType> searchFieldLookup = new HashMap<>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    private Map<String, Facet> creatorFacetMap = new HashMap<>();
    private Map<String, Facet> keywordFacetMap = new HashMap<>();
    private String slug = "";
    private String slugSuffix = "";
    private boolean redirectBadSlug;
    private List<ResourceCollection> collections;

    @Autowired
    private transient BillingAccountService accountService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    public transient SerializationService serializationService;

    @Autowired
    public transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient AuthenticationService authenticationService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceSearchService resourceSearchService;

    @Autowired
    private transient ResourceService resourceService;
    private String schemaOrgJsonLD;
    private Set<ResourceCollection> ownerCollections = new HashSet<>();

    public Creator getAuthorityForDup() {
        return entityService.findAuthorityFromDuplicate(creator);
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
            setRedirectBadSlug(true);
        } else {
            prepareLuceneQuery();
        }

        if (isEditor() && getPersistable() instanceof TdarUser) {
            getOwnerCollections().addAll(resourceCollectionService.findParentOwnerCollections((TdarUser) getPersistable(), SharedCollection.class));
            getOwnerCollections().addAll(entityService.findAccessibleResourceCollections((TdarUser) getPersistable()));

        }

        if (isLogoAvailable()) {
            setLogoUrl(UrlService.creatorLogoUrl(creator));
        }
        
        if(creator instanceof Institution){
        	people.addAll(entityService.findPersonsByInstitution((Institution) creator));
        }
    }

    @Actions(value = {
            @Action(value = "{id}"),
            @Action(value = "{id}/{slug}")
    })
    @SkipValidation
    public String browseCreators() throws ParseException, TdarActionException {
        if (isRedirectBadSlug()) {
            return BAD_SLUG;
        }

        try {
            setSchemaOrgJsonLD(entityService.getSchemaOrgJson(getCreator(), logoUrl));
        } catch (Exception e) {
            getLogger().error("error converting to json-ld", e);
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
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatisticsForUser(Arrays.asList(getId()), null));
            } catch (Exception e) {
                getLogger().error("unable to set resource access statistics", e);
            }
//            setViewCount(entityService.getCreatorViewCount(creator));
        }

        if (!isEditor() && !PersistableUtils.isEqual(creator, getAuthenticatedUser())) {
            CreatorViewStatistic cvs = new CreatorViewStatistic(new Date(), creator, isBot());
            getGenericService().saveOrUpdate(cvs);
        }

        // reset fields which can be broken by the searching hydration obfuscating things
        creator = getGenericService().find(Creator.class, getId());
        return SUCCESS;
    }
    
    private List<Person> people = new ArrayList<Person>();

    @SuppressWarnings("unchecked")
    private void prepareLuceneQuery() throws TdarActionException {
        setMode("browseCreators");
        setSortField(SortOption.RESOURCE_TYPE);
        if (PersistableUtils.isNotNullOrTransient(creator)) {
            String descr = getText("browseController.all_resource_from", creator.getProperName());
            setSearchDescription(descr);
            setSearchTitle(descr);
            setRecordsPerPage(50);
            try {
                setProjectionModel(ProjectionModel.RESOURCE_PROXY);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, CultureKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_INVESTIGATION_TYPES, InvestigationType.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS, MaterialKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, TemporalKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_OTHER_KEYWORDS, OtherKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS, SiteTypeKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS, GeographicKeyword.class);
                getFacetWrapper().facetBy(QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, SiteNameKeyword.class);
                List<String> roles = new ArrayList<>();
                List<String> kwds = Arrays.asList(QueryFieldNames.ACTIVE_CULTURE_KEYWORDS, QueryFieldNames.ACTIVE_INVESTIGATION_TYPES,
                        QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS,
                        QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS, QueryFieldNames.ACTIVE_OTHER_KEYWORDS, QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS,
                        QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS, QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS);
                for (ResourceCreatorRole role : ResourceCreatorRole.getResourceCreatorRolesForProfilePage(creator.getCreatorType())) {
                    roles.add(role.name());
                    getFacetWrapper().facetBy(role.name(), Creator.class);
                }
                // 10 per facet group should be plenty
                getFacetWrapper().setMaxFacetLimit(10);
                resourceSearchService.generateQueryForRelatedResources(creator, getAuthenticatedUser(), this, this);
                List<Long> ignoreIds = new ArrayList<>();
                ignoreIds.add(creator.getId());
                ignoreIds.addAll(PersistableUtils.extractIds(creator.getSynonyms()));
                summarizeFacets(getCreatorFacetMap(), roles, ignoreIds);
                summarizeFacets(getKeywordFacetMap(), kwds, null);

                bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());

            } catch (SearchPaginationException spe) {
                throw new TdarActionException(StatusCode.NOT_FOUND, spe);
            } catch (TdarRecoverableRuntimeException tdre) {
                getLogger().warn("search parse exception", tdre);
                addActionError(tdre.getMessage());
            } catch (SearchException e) {
                getLogger().warn("search parse exception", e);
            } catch (Throwable e) {
                getLogger().warn("search parse exception", e);
            }

        }
    }

    private void summarizeFacets(Map<String, Facet> facetMap, List<String> roles, List<Long> ignoreIds) {
        if (getFacetWrapper() == null || MapUtils.isEmpty(getFacetWrapper().getFacetResults())) {
            return;
        }
        for (String role : roles) {
            if (CollectionUtils.isEmpty(getFacetWrapper().getFacetResults().get(role))) {
                continue;
            }
            for (Facet facet : getFacetWrapper().getFacetResults().get(role)) {
                // skip things in the ignoreId
                if (NumberUtils.isNumber(facet.getRaw()) && CollectionUtils.isNotEmpty(ignoreIds) &&ignoreIds.contains(Long.parseLong(facet.getRaw()))) {
                    continue;
                }
                Facet stored = facetMap.get(facet.getUniqueKey());
                if (stored != null) {
                    stored.incrementCountBy(facet.getCount());
                } else {
                    facetMap.put(facet.getUniqueKey(), facet);
                }
            }
        }
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Addressable getPersistable() {
        return creator;
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

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
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

    @Override
    public boolean isRightSidebar() {
        if (MapUtils.isEmpty(creatorFacetMap) && MapUtils.isEmpty(keywordFacetMap)) {
            return false;
        }
        return true;
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
        return checkLogoAvailable(FilestoreObjectType.CREATOR, getId(), VersionType.WEB_SMALL);
    }

    @Override
    public boolean isRedirectBadSlug() {
        return redirectBadSlug;
    }

    public void setRedirectBadSlug(boolean redirectBadSlug) {
        this.redirectBadSlug = redirectBadSlug;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getSchemaOrgJsonLD() {
        return schemaOrgJsonLD;
    }

    public void setSchemaOrgJsonLD(String schemaOrgJsonLD) {
        this.schemaOrgJsonLD = schemaOrgJsonLD;
    }

    public List<ResourceCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<ResourceCollection> collections) {
        this.collections = collections;
    }

    public Set<ResourceCollection> getOwnerCollections() {
        return ownerCollections;
    }

    public void setOwnerCollections(Set<ResourceCollection> ownerCollections) {
        this.ownerCollections = ownerCollections;
    }

    public Map<String, Facet> getCreatorFacetMap() {
        return creatorFacetMap;
    }

    public void setCreatorFacetMap(Map<String, Facet> creatorFacetMap) {
        this.creatorFacetMap = creatorFacetMap;
    }

    public Map<String, Facet> getKeywordFacetMap() {
        return keywordFacetMap;
    }

    public void setKeywordFacetMap(Map<String, Facet> keywordFacetMap) {
        this.keywordFacetMap = keywordFacetMap;
    }
    
    public List<Person> getPeople(){
    	return people;
    }
  
    public void setPeople(List<Person> peopleList ){
    	people = peopleList;
    }

}
