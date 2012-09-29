package org.tdar.struts.action;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.StatisticService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.ImageService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

/**
 * $Id$
 * <p>
 * Base action class that provides access to the service layer, SessionData, and constants for custom result names.
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Configuration
public abstract class TdarActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static final long serialVersionUID = 7084489869489013998L;

    // result name constants
    public static final String WAIT = "wait";
    public static final String SUCCESS_ASYNC = "SUCCESS_ASYNC";
    public static final String NOT_FOUND = "not_found";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String AUTHENTICATED = "authenticated";
    public static final String GONE = "gone";
    public static final String BAD_REQUEST = "badrequest";

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
    private transient OntologyService ontologyService;
    @Autowired
    private transient OntologyNodeService ontologyNodeService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

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
    private transient GenericKeywordService genericKeywordService;
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
    private transient StatisticService statisticService;

    private transient List<String> stackTraces = new ArrayList<String>();

    private SessionData sessionData;

    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

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

    public ResourceService getResourceService() {
        return resourceService;
    }

    public BookmarkedResourceService getBookmarkedResourceService() {
        return bookmarkedResourceService;
    }

    public EntityService getEntityService() {
        return entityService;
    }

    public AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService() {
        return authenticationAndAuthorizationService;
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    public String getActionName() {
        if (ActionContext.getContext() == null)
            return null;
        return ActionContext.getContext().getName();
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

    public String getThemeDir() {
        return TdarConfiguration.getInstance().getThemeDir();
    }

   public String getGoogleMapsApiKey() {
        return TdarConfiguration.getInstance().getGoogleMapsApiKey();	
   }

    public String getGoogleAnalyticsId() {
        return TdarConfiguration.getInstance().getGoogleAnalyticsId();
    }

    public boolean getPrivacyControlsEnabled() {
        return TdarConfiguration.getInstance().getPrivacyControlsEnabled();
    }
    
    public boolean isCopyrightMandatory() {
        return TdarConfiguration.getInstance().getCopyrightMandatory();
    }

    public boolean isCopyrightEnabled() {
        return TdarConfiguration.getInstance().getCopyrightEnabled();
    }
    
    public boolean getLicensesEnabled() {
        return TdarConfiguration.getInstance().getLicensesEnabled();
    }

    public String getServerEnvironmentStatus() {
        return TdarConfiguration.getInstance().getServerEnvironmentStatus();
    }

    public boolean isProduction() {
        return TdarConfiguration.getInstance().getServerEnvironmentStatus().equalsIgnoreCase(TdarConfiguration.PRODUCTION);
    }

    public String getHelpUrl() {
        return TdarConfiguration.getInstance().getHelpUrl();
    }

    public String getAboutUrl() {
        return TdarConfiguration.getInstance().getAboutUrl();
    }

    public String getCommentsUrl() {
        return TdarConfiguration.getInstance().getAboutUrl();
    }

    public String getGMapDefaultLat() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(TdarConfiguration.getInstance().getGmapDefaultLat());
    }

    public String getGMapDefaultLng() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(TdarConfiguration.getInstance().getGmapDefaultLng());
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

    public DataIntegrationService getDataIntegrationService() {
        return dataIntegrationService;
    }

    public GenericKeywordService getGenericKeywordService() {
        return genericKeywordService;
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
    protected List<String> toSortedStringList(Collection<?> collection) {
        ArrayList<String> stringList = new ArrayList<String>(collection.size());
        for (Object o : collection) {
            stringList.add(o.toString());
        }
        Collections.sort(stringList);
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

        getLogger().error("{}: {} -- {}", new Object[] { message, exception, trace });
        if (exception instanceof TdarRecoverableRuntimeException) {
            super.addActionError(exception.getMessage());
        } else {
            super.addActionError(message);
        }
        stackTraces.add(trace);
    }

    @Override
    public void addActionError(String message) {
        logger.debug("ACTIONERROR:: {}", message);
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

    public ResourceCollectionService getResourceCollectionService() {
        return resourceCollectionService;
    }

    public StatisticService getStatisticService() {
        return statisticService;
    }

    protected HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    protected HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    protected final boolean isPostRequest() {
        return "POST".equals(servletRequest.getMethod());
    }

    protected final boolean isGetRequest() {
        return "GET".equals(servletRequest.getMethod());
    }

    public List<CoverageType> getAllCoverageTypes() {
        List<CoverageType> coverageTypes = new ArrayList<CoverageType>();
        coverageTypes.add(CoverageType.CALENDAR_DATE);
        coverageTypes.add(CoverageType.RADIOCARBON_DATE);
        return coverageTypes;
    }

}
