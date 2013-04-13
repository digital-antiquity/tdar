package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.cache.BrowseDecadeCountCache;
import org.tdar.core.bean.cache.BrowseYearCountCache;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.struts.data.ResourceSpaceUsageStatistic;
import org.tdar.struts.interceptor.HttpOnlyIfUnauthenticated;

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

    // private Keyword keyword;

    @Action("explore")
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

    @Action("collections")
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
                    null));
        }

        return SUCCESS;
    }

    @Action(value = "creators", results = { @Result(location = "results.ftl") })
    public String browseCreators() throws ParseException, TdarActionException {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            creator = getGenericService().find(Creator.class, getId());
            QueryBuilder queryBuilder = new ResourceQueryBuilder();
            queryBuilder.setOperator(Operator.AND);

            SearchParameters params = new SearchParameters(Operator.OR);
            // could use "creator type" to filter; but this doesn't cover the creator type "OTHER"
            for (ResourceCreatorRole role : ResourceCreatorRole.values()) {
                if (role == ResourceCreatorRole.UPDATER) {
                    continue;
                }
                params.getResourceCreatorProxies().add(new ResourceCreatorProxy(creator, role));
            }
            queryBuilder.append(params);
            ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
            getAuthenticationAndAuthorizationService().initializeReservedSearchParameters(reservedSearchParameters, getAuthenticatedUser());
            queryBuilder.append(reservedSearchParameters);

            if (isEditor() && creator instanceof Person) {
                try {
                    getGroups().addAll(getAuthenticationAndAuthorizationService().getGroupMembership((Person) creator));
                } catch (Throwable e) {
                    logger.error("problem communicating with crowd getting user info for {} ", creator, e);
                }
                setUploadedResourceAccessStatistic(getResourceService().getResourceSpaceUsageStatistics(Arrays.asList(getId()), null, null, null, null));
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

}
