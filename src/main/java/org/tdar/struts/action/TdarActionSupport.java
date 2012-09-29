package org.tdar.struts.action;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.CategoryVariableService;
import org.tdar.core.service.CodingSheetService;
import org.tdar.core.service.CultureKeywordService;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.DataTableService;
import org.tdar.core.service.DatasetService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.GeographicKeywordService;
import org.tdar.core.service.ImageService;
import org.tdar.core.service.InformationResourceFileService;
import org.tdar.core.service.InformationResourceFileVersionService;
import org.tdar.core.service.InformationResourceService;
import org.tdar.core.service.InvestigationTypeService;
import org.tdar.core.service.MaterialKeywordService;
import org.tdar.core.service.OntologyNodeService;
import org.tdar.core.service.OntologyService;
import org.tdar.core.service.OtherKeywordService;
import org.tdar.core.service.ProjectService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.ResourceService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.SimpleCachingService;
import org.tdar.core.service.SiteNameKeywordService;
import org.tdar.core.service.SiteTypeKeywordService;
import org.tdar.core.service.TemporalKeywordService;
import org.tdar.core.service.UrlService;
import org.tdar.utils.Pair;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.ActionSupport;

/**
 * $Id$
 * 
 * Provides access to common service layer classes.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Configuration
public abstract class TdarActionSupport extends ActionSupport {

    private static final long serialVersionUID = 7084489869489013998L;

    public static final String WAIT = "wait";
    public static final String SUCCESS_ASYNC = "SUCCESS_ASYNC";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient DatasetService datasetService;
    @Autowired
    private transient DataTableService dataTableService;
    @Autowired
    private transient CodingSheetService codingSheetService;
    @Autowired
    private transient SimpleCachingService simpleCachingService;
    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient OntologyNodeService ontologyNodeService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private transient EntityService entityService;
    @Autowired
    private transient CategoryVariableService categoryVariableService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient InformationResourceService informationResourceService;
    @Autowired
    private transient InformationResourceFileService informationResourceFileService;
    @Autowired
    private transient SearchService searchService;
    @Autowired
    private transient OtherKeywordService otherKeywordService;
    @Autowired
    private transient CultureKeywordService cultureKeywordService;
    @Autowired
    private transient SiteNameKeywordService siteNameKeywordService;
    @Autowired
    private transient GeographicKeywordService geographicKeywordService;
    @Autowired
    private transient TemporalKeywordService temporalKeywordService;
    @Autowired
    private transient SiteTypeKeywordService siteTypeKeywordService;
    @Autowired
    private transient MaterialKeywordService materialKeywordService;
    @Autowired
    private transient InvestigationTypeService investigationTypeService;
    @Autowired
    private transient DataIntegrationService dataIntegrationService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient InformationResourceFileVersionService informationResourceFileVersionService;
    @Autowired
    private transient UrlService urlService;
    @Autowired
    private transient ImageService imageService;
    @Autowired
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ReflectionService reflectionService;

    private transient List<String> stackTraces = new ArrayList<String>();

    private SessionData sessionData;

    public ProjectService getProjectService() {
        return projectService;
    }

    public DatasetService getDatasetService() {
        return datasetService;
    }

    public CodingSheetService getCodingSheetService() {
        return codingSheetService;
    }

    public OntologyService getOntologyService() {
        return ontologyService;
    }

    public Map<ResourceType, Pair<Long,Double>> getResourceTypeCounts() {
        return simpleCachingService.getHomepageCache().getResourceCount();
    }

    public Map<GeographicKeyword, Long> getISOCountryCount() {
        return simpleCachingService.getHomepageCache().getCountryCount();
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public BookmarkedResourceService getBookmarkedResourceService() {
        return bookmarkedResourceService;
    }

    public EntityService getEntityService() {
        return entityService;
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    public SessionData getSessionData() {
        if (sessionData == null) {
            getLogger().error("Session data was null, should be managed by Spring.");
            throw new IllegalStateException("Session data was null, should be managed by Spring.");
        }
        return sessionData;
    }

    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    protected void clearAuthenticationToken() {
        AuthenticationToken token = getSessionData().getAuthenticationToken();
        token.setSessionEnd(new Date());
        getGenericService().update(token);
        getSessionData().clearAuthenticationToken();
    }

    protected Logger getLogger() {
        return logger;
    }

    public CategoryVariableService getCategoryVariableService() {
        return categoryVariableService;
    }

    public InformationResourceService getInformationResourceService() {
        return informationResourceService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public DataTableService getDataTableService() {
        return dataTableService;
    }

    public OntologyNodeService getOntologyNodeService() {
        return ontologyNodeService;
    }

    public InformationResourceFileService getInformationResourceFileService() {
        return informationResourceFileService;
    }

    public OtherKeywordService getOtherKeywordService() {
        return otherKeywordService;
    }

    public CultureKeywordService getCultureKeywordService() {
        return cultureKeywordService;
    }

    public SiteNameKeywordService getSiteNameKeywordService() {
        return siteNameKeywordService;
    }

    public DataIntegrationService getDataIntegrationService() {
        return dataIntegrationService;
    }

    public GeographicKeywordService getGeographicKeywordService() {
        return geographicKeywordService;
    }

    public SiteTypeKeywordService getSiteTypeKeywordService() {
        return siteTypeKeywordService;
    }

    public TemporalKeywordService getTemporalKeywordService() {
        return temporalKeywordService;
    }

    public MaterialKeywordService getMaterialKeywordService() {
        return materialKeywordService;
    }

    public InvestigationTypeService getInvestigationTypeService() {
        return investigationTypeService;
    }

    public GenericService getGenericService() {
        return genericService;
    }

    public InformationResourceFileVersionService getInformationResourceFileVersionService() {
        return informationResourceFileVersionService;
    }

    public UrlService getUrlService() {
        return urlService;
    }

    public ImageService getImageService() {
        return imageService;
    }

    public SearchIndexService getSearchIndexService() {
        return searchIndexService;
    }

    /**
     * Returns a list of Strings resulting from applying toString to each
     * element of the incoming Collection.
     * 
     * Basically, map( toString, collection ), but since Java doesn't support
     * closures yet...
     * 
     * @param collection
     * @return
     */
    protected List<String> toStringList(Collection<?> collection) {
        ArrayList<String> stringList = new ArrayList<String>(collection.size());
        for (Object o : collection) {
            stringList.add(o.toString());
        }
        return stringList;
    }

    protected <P extends Persistable> List<Long> toIdList(Collection<P> persistables) {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (P persistable : persistables) {
            ids.add(persistable.getId());
        }
        return ids;
    }

    protected void addActionErrorWithException(String message, Throwable exception) {
        String trace = getStackTrace(exception);
        getLogger().error("{} -- {}", message + " : " + exception, trace);
        super.addActionError(message);
        stackTraces.add(trace);

    }

    @Override
    public void addActionError(String message) {
        logger.debug("ACTIONERROR:: {}",message);
        super.addActionError(message);
    }

    protected String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();

    }

    public List<String> getStackTraces() {
        return stackTraces;
    }

    /**
     * @return the resourceCollectionService
     */
    public ResourceCollectionService getResourceCollectionService() {
        return resourceCollectionService;
    }

    /**
     * @return the reflectionService
     */
    public ReflectionService getReflectionService() {
        return reflectionService;
    }

}
