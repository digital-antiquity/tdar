package org.tdar.struts.action;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.DownloadService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.StatisticService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceRelationshipService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.workflow.ActionMessageErrorSupport;
import org.tdar.utils.activity.Activity;
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
@Scope("prototype")
@Controller
public abstract class TdarActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware, ActionMessageErrorSupport {

    private static final long serialVersionUID = 7084489869489013998L;

    // result name constants
    public static final String REDIRECT = "redirect";
    public static final String WAIT = "wait";
    public static final String THUMBNAIL = "thumbnail";
    public static final String FORBIDDEN = "forbidden";
    public static final String SUCCESS_ASYNC = "SUCCESS_ASYNC";
    public static final String NOT_FOUND = "not_found";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String AUTHENTICATED = "authenticated";
    public static final String GONE = "gone";
    public static final String BAD_REQUEST = "badrequest";
    public static final String SAVE = "save";
    public static final String ADD = "add";
    public static final String VIEW = "view";
    public static final String EDIT = "edit";
    public static final String JSON = "json";
    public static final String BILLING = "BILLING";
    public static final String CONFIRM = "confirm";
    public static final String DELETE = "delete";
    public static final String NEW = "new";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient DownloadService downloadService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient EmailService emailService;
    @Autowired
    private transient AccountService accountService;
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
    private transient FreemarkerService freemarkerService;

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
    private transient SearchIndexService searchIndexService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient ResourceRelationshipService resourceRelationshipService;

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

    public ResourceRelationshipService getResourceRelationshipService() {
        return resourceRelationshipService;
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

    public String getNamespace() {
        return ServletActionContext.getActionMapping().getNamespace();
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
        return getTdarConfiguration().getThemeDir();
    }

    public String getCulturalTermsHelpUrl() {
        return getTdarConfiguration().getCulturalTermsHelpURL();
    }

    public String getInvestigationTypesHelpUrl() {
        return getTdarConfiguration().getInvestigationTypesHelpURL();
    }

    public String getMaterialTypesHelpUrl() {
        return getTdarConfiguration().getMaterialTypesHelpURL();
    }

    public String getSiteTypesHelpUrl() {
        return getTdarConfiguration().getSiteTypesHelpURL();
    }

    public String getMobileImportURL() {
        return getTdarConfiguration().getMobileImportURL();
    }

    public String getGoogleMapsApiKey() {
        return getTdarConfiguration().getGoogleMapsApiKey();
    }

    public String getGoogleAnalyticsId() {
        return getTdarConfiguration().getGoogleAnalyticsId();
    }

    public boolean getPrivacyControlsEnabled() {
        return getTdarConfiguration().getPrivacyControlsEnabled();
    }

    public boolean isCopyrightMandatory() {
        return getTdarConfiguration().getCopyrightMandatory();
    }

    public boolean isArchiveFileEnabled() {
        return getTdarConfiguration().isArchiveFileEnabled();
    }

    public boolean isVideoEnabled() {
        return getTdarConfiguration().isVideoEnabled();
    }

    public boolean isLicensesEnabled() {
        return getTdarConfiguration().getLicenseEnabled();
    }

    public String getServerEnvironmentStatus() {
        return getTdarConfiguration().getServerEnvironmentStatus();
    }

    public String getHostName() {
        return getTdarConfiguration().getHostName();
    }

    public int getHostPort() {
        return getTdarConfiguration().getPort();
    }

    public String getContactEmail() {
        return getTdarConfiguration().getContactEmail();
    }

    public String getSiteAcronym() {
        return getTdarConfiguration().getSiteAcronym();
    }

    public String getSiteName() {
        return getTdarConfiguration().getSiteName();
    }

    public String getCommentUrl() {
        return getTdarConfiguration().getCommentUrl();
    }

    public String getCommentUrlEscaped() {
        String input = getTdarConfiguration().getCommentUrl();
        int length = input.length();
        StringBuffer output = new StringBuffer(length * 6);
        for (int i = 0; i < input.length(); i++) {
            output.append("&#");
            output.append((int) input.charAt(i));
            output.append(";");
        }
        return output.toString();
    }

    public String getBugReportUrl() {
        return getTdarConfiguration().getBugReportUrl();
    }

    public String getDocumentationUrl() {
        return getTdarConfiguration().getDocumentationUrl();
    }

    public boolean isProduction() {
        return getTdarConfiguration().getServerEnvironmentStatus().equalsIgnoreCase(TdarConfiguration.PRODUCTION);
    }

    public String getHelpUrl() {
        return getTdarConfiguration().getHelpUrl();
    }

    public String getAboutUrl() {
        return getTdarConfiguration().getAboutUrl();
    }

    public String getCommentsUrl() {
        return getTdarConfiguration().getAboutUrl();
    }

    public Boolean isRPAEnabled() {
        return getTdarConfiguration().isRPAEnabled();
    }

    public String getMapDefaultLat() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(getTdarConfiguration().getMapDefaultLat());
    }

    public String getMapDefaultLng() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(getTdarConfiguration().getMapDefaultLng());
    }

    public boolean isGeoLocationToBeUsed() {
        return getTdarConfiguration().isGeoLocationToBeUsed();
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

    public EmailService getEmailService() {
        return emailService;
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

    public SearchIndexService getSearchIndexService() {
        return searchIndexService;
    }

    public FreemarkerService getFreemarkerService() {
        return freemarkerService;
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
        String trace = ExceptionUtils.getFullStackTrace(exception);

        getLogger().error("{}: {} -- {}", new Object[] { message, exception, trace });
        if (exception instanceof TdarRecoverableRuntimeException) {
            int maxDepth = 4;
            Throwable thrw = exception;
            StringBuilder sb = new StringBuilder(exception.getMessage());

            while (thrw.getCause() != null && maxDepth > -1) {
                thrw = thrw.getCause();
                if (StringUtils.isNotBlank(thrw.getMessage())) {
                    sb.append(": ").append(thrw.getMessage());
                }
                maxDepth--;
            }

            super.addActionError(sb.toString());
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

    @Override
    public List<String> getStackTraces() {
        return stackTraces;
    }

    public ResourceCollectionService getResourceCollectionService() {
        return resourceCollectionService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    public StatisticService getStatisticService() {
        return statisticService;
    }

    protected HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    protected HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    @Override
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

    public boolean isHttpsEnabled() {
        return getTdarConfiguration().isHttpsEnabled();
    }

    public Integer getHttpsPort() {
        return getTdarConfiguration().getHttpsPort();
    }

    public String getNewsUrl() {
        return getTdarConfiguration().getNewsUrl();
    }

    public boolean isPayPerIngestEnabled() {
        return getTdarConfiguration().isPayPerIngestEnabled();
    }

    public Integer getMaxUploadFilesPerRecord() {
        return getTdarConfiguration().getMaxUploadFilesPerRecord();
    }

    public boolean isSecure() {
        return servletRequest.isSecure();
    }

    public String getProtocol() {
        if (isSecure()) {
            return "https:";
        } else {
            return "http:";
        }
    }

    public boolean getShowJiraLink() {
        return getTdarConfiguration().getShowJiraLink();
    }

    public String getJiraScriptLink() {
        return getTdarConfiguration().getJiraScriptLink();
    }

    public boolean isReindexing() {
        Activity indexingTask = ActivityManager.getInstance().getIndexingTask();
        if (indexingTask != null && !indexingTask.hasEnded()) {
            return true;
        }
        return false;
    }

    public String getCurrentUrl() {
        return urlService.getOriginalUrlPath(servletRequest);
    }

    public boolean isViewRowSupported() {
        return getTdarConfiguration().isViewRowSupported();
    }

    public Long getGuestUserId() {
        return getTdarConfiguration().getGuestUserId();
    }

    public String getCulturalTermsLabel() {
        return getTdarConfiguration().getCulturalTermsLabel();
    }
}
