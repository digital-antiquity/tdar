package org.tdar.struts.action.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.cache.BrowseDecadeCountCache;
import org.tdar.core.cache.BrowseYearCountCache;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.search.SearchFieldType;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.FacetGroup;
import org.tdar.struts.action.AbstractLookupController;
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
public class ExploreController extends AbstractLookupController {

    private static final long serialVersionUID = -2826087034735486222L;

    public static final String EXPLORE = "explore";

    private Long viewCount = 0L;
    private List<InvestigationType> investigationTypes = new ArrayList<InvestigationType>();
    private List<CultureKeyword> cultureKeywords = new ArrayList<CultureKeyword>();
    private List<SiteTypeKeyword> siteTypeKeywords = new ArrayList<SiteTypeKeyword>();
    private List<MaterialKeyword> materialTypes = new ArrayList<MaterialKeyword>();
    private List<String> alphabet = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
    private List<BrowseYearCountCache> scholarData;
    private String timelineData;
    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private List<String> groups = new ArrayList<String>();
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;
    private String mapJson;

    private String homepageResourceCountCache;
    private List<Resource> featuredResources = new ArrayList<Resource>();
    private List<Resource> recentResources = new ArrayList<Resource>();

    private List<BillingAccount> accounts = new ArrayList<BillingAccount>();
    Map<String, SearchFieldType> searchFieldLookup = new HashMap<>();

    @Autowired
    private transient SerializationService serializationService;
    
    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient InformationResourceService informationResourceService;

    @Action(EXPLORE)
    public String explore() throws IOException {
        setHomepageResourceCountCache(serializationService.convertToJson(resourceService.getResourceCounts()));
        setMaterialTypes(genericKeywordService.findAllWithCache(MaterialKeyword.class));
        setInvestigationTypes(genericKeywordService.findAllWithCache(InvestigationType.class));
        setCultureKeywords(genericKeywordService.findAllApprovedWithCache(CultureKeyword.class));
        setSiteTypeKeywords(genericKeywordService.findAllApprovedWithCache(SiteTypeKeyword.class));
        setupTimelineData();
        setScholarData(informationResourceService.findResourceCountsByYear());

        List<HomepageGeographicCache> isoGeographicCounts = resourceService.getISOGeographicCounts();
        try {
            setMapJson(serializationService.convertToJson(isoGeographicCounts));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        getFeaturedResources().addAll(resourceService.getWeeklyPopularResources());
        try {
            getRecentResources().addAll(searchService.findMostRecentResources(10, getAuthenticatedUser(), this));
        } catch (ParseException pe) {
            getLogger().debug("parse exception", pe);
        }
        return SUCCESS;
    }

    private void setupTimelineData() throws IOException {
        List<BrowseDecadeCountCache> findResourcesByDecade = new ArrayList<>(informationResourceService.findResourcesByDecade());
        Iterator<BrowseDecadeCountCache> iterator = findResourcesByDecade.iterator();
        while (iterator.hasNext()) {
            BrowseDecadeCountCache decade = iterator.next();
            if (decade.getKey() < 1 || decade.getKey() > DateTime.now().getYear()) {
                iterator.remove();
            }
        }
        setTimelineData(serializationService.convertToJson(findResourcesByDecade));
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

    public String getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(String list) {
        this.timelineData = list;
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

    public String getHomepageResourceCountCache() {
        return homepageResourceCountCache;
    }

    public void setHomepageResourceCountCache(String string) {
        this.homepageResourceCountCache = string;
    }

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

    public List<BrowseYearCountCache> getScholarData() {
        return scholarData;
    }

    public void setScholarData(List<BrowseYearCountCache> scholarData) {
        this.scholarData = scholarData;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public boolean isShowAdminInfo() {
        return isAuthenticated() && (isEditor() || Objects.equals(getId(), getAuthenticatedUser().getId()));
    }

    public boolean isShowBasicInfo() {
        return isAuthenticated() && (isEditor() || Objects.equals(getId(), getAuthenticatedUser().getId()));
    }

    public List<Resource> getFeaturedResources() {
        return featuredResources;
    }

    public void setFeaturedResources(List<Resource> featuredResources) {
        this.featuredResources = featuredResources;
    }

    public List<Resource> getRecentResources() {
        return recentResources;
    }

    public void setRecentResources(List<Resource> recentResources) {
        this.recentResources = recentResources;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getMapJson() {
        return mapJson;
    }

    public void setMapJson(String mapJson) {
        this.mapJson = mapJson;
    }

}