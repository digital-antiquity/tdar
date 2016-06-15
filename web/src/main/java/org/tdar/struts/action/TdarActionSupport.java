package org.tdar.struts.action;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.LocalizableException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.struts.ErrorListener;
import org.tdar.struts.WROProfile;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.jaxb.ActionErrorWrapper;
import org.tdar.web.TdarServletConfiguration;
import org.tdar.web.WebFileSystemResourceService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import ro.isdc.wro.model.resource.ResourceType;

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
// @Controller
public abstract class TdarActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware, ActionErrorWrapper {

    private static final long serialVersionUID = 7084489869489013998L;

    // result name constants
    private boolean hideExceptionArea = false;
    private Date freemarkerProcessingTime = null;

    private static final String JS_ERRORLOG_NOSCRIPT = "NOSCRIPT";
    // FIXME: UTF-8 here is likely inviting encoding errors/challenges especially if it ends up in the console which is often the "ASCII" charset
    private static final String JS_ERRORLOG_DELIMITER = "ɹǝʇıɯıןǝp";

    public static final String BAD_SLUG = "bad-slug";
    public static final String DRAFT = "draft";
    public static final String JAXBRESULT = "jaxbdocument";
    public static final String JSONRESULT = "jsonresult";
    public static final String HTTPHEADER = "httpheader";
    public static final String TDAR_REDIRECT = "tdar-redirect";
    public static final String REDIRECT = "redirect";

    public static final String WAIT = "wait";
    public static final String THUMBNAIL = "thumbnail";
    public static final String SM = "sm";
    public static final String MD = "md";
    public static final String LG = "lg";
    public static final String SUCCESS_ASYNC = "SUCCESS_ASYNC";

    /**
     * The action could not execute because the request has invalid or insufficient information
     */
    public static final String UNKNOWN_ERROR = "exception"; // 500
    public static final String BAD_REQUEST = "badrequest"; // 400

//    public static final String TYPE_TDAR_REDIRECT = "redirect";

    public static final String FORBIDDEN = "forbidden"; // 403
    public static final String NOT_FOUND = "not_found"; // 404
    /**
     * The action could not execute because the action requires an authenticated user.
     */
    public static final String UNAUTHORIZED = "unauthorized"; // 401
    public static final String UNAUTHORIZED_REDIRECT = "unauthorized_redirect";

    public static final String AUTHENTICATED = "authenticated";

    /**
     * The action could not execute because one or more resources referenced in the action are no longer
     * available (as opposed to a resource that is simply not found)
     */
    public static final String GONE = "gone";

    public static final String SAVE = "save";
    public static final String ADD = "add";
    public static final String VIEW = "view";
    public static final String EDIT = "edit";
    public static final String JSON = "json";
    public static final String BILLING = "billing";
    public static final String CONTRIBUTOR = "contributor";
    public static final String CONFIRM = "confirm";
    public static final String DELETE = "delete";
    public static final String NEW = "new";
    public static final String FREEMARKER = "freemarker";
    public static final String FREEMARKERHTTP = "freemarkerhttp";

    /**
     * The system has authenticated the user and the user is authorized to perform the requested action, but
     * the action could not execute because the user must explicitly acknowledge/accept certain items (e.g.
     * updated Terms Of Service)
     */
    public static final String USER_AGREEMENT = "user_agreement";
    public static final String RESULT_REDIRECT_START = "redirect-start";

    private String javascriptErrorLog;

    /**
     * The view layer ftl primes the js error log with "NOSCRIPT", and the js init tries to clear the log. This way
     * the validate() method can roughly determine if
     * a) (if errorlog == 'NOSCRIPT' ) javascript was disabled on the client
     * b) (errorlog is blank) no js errors were captured (still plenty of ways js errors could have happened though)
     * c) (errorlog has junk in it) You've Got JS Errors!!
     * 
     */

    private String moreInfoUrlKey;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient WebFileSystemResourceService webFilesystemResourceService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient GenericService genericService;

    private transient List<String> stackTraces = new ArrayList<String>();

    private SessionData sessionData;

    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

    private Map<String, String> clientValidationInfo = new LinkedHashMap<>();

    private ErrorListener errorListener;

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    public String getNamespace() {
        return ServletActionContext.getActionMapping().getNamespace();
    }

    public String getActionName() {
        if (ActionContext.getContext() == null) {
            return null;
        }
        return ActionContext.getContext().getName();
    }

    @DoNotObfuscate(reason = "Session Object")
    public SessionData getSessionData() {
        if (sessionData == null) {
            getLogger().error("Session data was null, should be managed by Spring.");
            throw new IllegalStateException(getText("tdarActionSupport.no_sesion_data"));
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

    public String getBaseUrl() {
        return getTdarConfiguration().getBaseUrl();
    }

    public String getContactEmail() {
        return getTdarConfiguration().getContactEmail();
    }

    public String getSiteAcronym() {
        return getTdarConfiguration().getSiteAcronym();
    }

    public String getServiceProvider() {
        return getTdarConfiguration().getServiceProvider();
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

    public String getIntegrationDocumentationUrl() {
        return getTdarConfiguration().getIntegrationDocumentationUrl();
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

    protected Logger getLogger() {
        return logger;
    }

    public GenericService getGenericService() {
        return genericService;
    }

    protected void addActionErrorWithException(String message, Throwable exception) {
        String trace = ExceptionUtils.getStackTrace(exception);

        getLogger().error("{} [code: {}]: {} -- {}", new Object[] { message, ExceptionWrapper.convertExceptionToCode(exception), exception, trace });
        if (exception instanceof TdarActionException) {
            setHideExceptionArea(true);
        }
        if (exception instanceof TdarRecoverableRuntimeException) {
            int maxDepth = 4;
            Throwable thrw = exception;
            if (exception instanceof LocalizableException) {
                ((LocalizableException) exception).setLocale(getLocale());
            }
            StringBuilder sb = new StringBuilder(exception.getLocalizedMessage());

            while ((thrw.getCause() != null) && (maxDepth > -1)) {
                thrw = thrw.getCause();
                if (StringUtils.isNotBlank(thrw.getMessage())) {
                    sb.append(": ").append(thrw.getMessage());
                }
                maxDepth--;
            }

            addActionError(sb.toString());
        } else if (StringUtils.isNotBlank(message)) {
            addActionError(message);
        }
        stackTraces.add(ExceptionWrapper.convertExceptionToCode(exception));
    }

    @Override
    public void addFieldError(String fieldName, String errorMessage) {
        if (errorListener != null) {
            errorListener.addError(String.format("%s:%s", fieldName, errorMessage));
        }
        super.addFieldError(fieldName, errorMessage);
    }

    // FIXME: shouldn't we just getText() every message here or add addActionErrorMessageKey(String messageKey)
    @Override
    public void addActionError(String message) {
        getLogger().debug("ACTIONERROR:: {}", message);
        if (errorListener != null) {
            errorListener.addError(message);
        }
        super.addActionError(message);
    }

    // FIXME: when replacing ActionErrors above, this will not need the getText calls
    protected void processErrorObject(ErrorTransferObject errors) {
        getLogger().trace("found errors {}", errors);
        if (errors == null) {
            return;
        }
        for (String error : errors.getActionErrors()) {
            this.addActionError(getText(error));
        }

        Map<String, List<String>> fieldErrors = errors.getFieldErrors();
        for (String field : fieldErrors.keySet()) {
            for (String error : fieldErrors.get(field)) {
                this.addFieldError(field, getText(error));
            }
        }

        for (String msg : errors.getActionMessages()) {
            this.addActionMessage(getText(msg));
        }

        for (String msg : errors.getStackTraces()) {
            getStackTraces().add(msg);
        }

        setMoreInfoUrlKey(errors.getMoreInfoUrlKey());
    }

    public List<String> getStackTraces() {
        return stackTraces;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
    
    public String getContextPath() {
        return getServletRequest().getContextPath();
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    @Override
    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    @Deprecated
    /**
     * Avoid direct references to the request object and http methods.  It's a sign that you are doing something
     * best handled by an Interceptor or a ResultType.
     */
    protected final boolean isPostRequest() {
        return "POST".equals(servletRequest.getMethod());
    }

    protected final boolean isGetRequest() {
        return "GET".equals(servletRequest.getMethod());
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
        }
        return "http:";
    }

    public String getStaticHost() {
        if (!getTdarConfiguration().isStaticContentEnabled()) {
            // expecting that default requests are relative to root; so / becomes //
            return getContextPath();
        }

        String port = "";
        if (isSecure() && (getTdarConfiguration().getStaticContentSSLPort() != 443)) {
            port = ":" + getTdarConfiguration().getStaticContentSSLPort();
        }

        if (!isSecure() && (getTdarConfiguration().getStaticContentPort() != 80)) {
            port = ":" + getTdarConfiguration().getStaticContentPort();
        }

        return String.format("%s//%s%s%s", getProtocol(), getTdarConfiguration().getStaticContentHost(), port,getStaticContext());
    }
    
    private String getStaticContext() {
        return getTdarConfiguration().getStaticContext();
    }

    /**
     * Return the default/suggested base url for static content (trailing slash removed, if present)
     * 
     * @return
     */
    public String getHostedContentBaseUrl() {
        String baseUrl = getStaticHost() + TdarServletConfiguration.HOSTED_CONTENT_BASE_URL;
        if (baseUrl.endsWith("/")) {
            baseUrl = StringUtils.chop(baseUrl);
        }
        return baseUrl;
    }


    public boolean getShowJiraLink() {
        return getTdarConfiguration().getShowJiraLink();
    }

    public String getJiraScriptLink() {
        return getTdarConfiguration().getJiraScriptLink();
    }

    public boolean isReindexing() {
        Activity indexingTask = ActivityManager.getInstance().getIndexingTask();
        if ((indexingTask != null) && !indexingTask.hasEnded()) {
            return true;
        }
        return false;
    }

    public String getCurrentUrl() {
        return UrlService.getOriginalUrlPath(servletRequest);
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

    public String getJavascriptErrorLogDefault() {
        return JS_ERRORLOG_NOSCRIPT;
    }

    public String getJavascriptErrorLogDelimiter() {
        return JS_ERRORLOG_DELIMITER;
    }

    /**
     * Check the js error log and js validation error log. If we detect any js errors, log them at ERROR. Validation
     * errors are an expected part of the workflow and are only logged at INFO.
     */
    public void reportAnyJavascriptErrors() {
        if (StringUtils.isBlank(javascriptErrorLog)) {
            getLogger().trace("No javascript errors reported by the client");
        } else {
            String[] errors = javascriptErrorLog.split("\\Q" + getJavascriptErrorLogDelimiter() + "\\E");
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Client {} reported {} javascript errors.  <<{}>>", ServletActionContext.getRequest().getHeader("User-Agent"),
                        errors.length, StringUtils.join(errors, " :: "));
            }
        }

        List<String> lines = new ArrayList<>(clientValidationInfo.size());
        for (Map.Entry<String, String> entry : getClientValidationInfo().entrySet()) {
            String line = String.format("%s\t %s", entry.getKey(), entry.getValue());
            lines.add(line);
        }
        if (!lines.isEmpty()) {
            getLogger().info("the client reported validation errors: \n {}", StringUtils.join(lines, " :: "));
        }
    }

    public void setJavascriptErrorLog(String errorLog) {
        javascriptErrorLog = errorLog;
    }

    public String getJavascriptErrorLog() {
        return javascriptErrorLog;
    }

    /**
     * @see TdarConfiguration#isSwitchableMapObfuscation()
     * @return whatever value the tdar configuration isSwitchableMapObfuscation returns.
     */
    public boolean isSwitchableMapObfuscation() {
        return getTdarConfiguration().isSwitchableMapObfuscation();
    }

    public Map<String, String> getClientValidationInfo() {
        return clientValidationInfo;
    }

    public void setClientValidationInfo(LinkedHashMap<String, String> clientValidationInfo) {
        this.clientValidationInfo = clientValidationInfo;
    }

    public String getText(String aTextName, Object... args) {
        return super.getText(aTextName, Arrays.asList(args));
    }

    public List<String> getJavascriptFiles() {
        return webFilesystemResourceService.fetchGroupUrls(getWroProfile(), ResourceType.JS);
    }

    public List<String> getCssFiles() {
        return webFilesystemResourceService.fetchGroupUrls(getWroProfile(), ResourceType.CSS);
    }

    public String getWroProfile() {
        return WROProfile.DEFAULT.getProfileName();
    }

    public boolean isWebFilePreprocessingEnabled() {
        return webFilesystemResourceService.testWRO();
    }

    public String getMoreInfoUrlKey() {
        return moreInfoUrlKey;
    }

    public void setMoreInfoUrlKey(String moreInfoUrl) {
        this.moreInfoUrlKey = moreInfoUrl;
    }

    public boolean isHideExceptionArea() {
        return hideExceptionArea;
    }

    public void setHideExceptionArea(boolean hideExceptionArea) {
        this.hideExceptionArea = hideExceptionArea;
    }

    public boolean isErrorWarningSectionVisible() {
        if (hideExceptionArea) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(getActionErrors())) {
            return true;
        }
        if (MapUtils.isNotEmpty(getFieldErrors())) {
            return true;
        }
        if (this instanceof AbstractInformationResourceController) {
            AbstractInformationResourceController<?> cast = (AbstractInformationResourceController<?>) this;
            if (cast.authorize() && CollectionUtils.isNotEmpty(cast.getHistoricalFileErrors())) {
                return true;
            }

        }
        return false;
    }

    public void addActionErrors(List<String> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (String error : errors) {
            addActionError(error);
        }
    }

    public String getWroTempDirName() {
        return webFilesystemResourceService.getWroDir();
    }

    public boolean isUseCDN() {
        return getTdarConfiguration().shouldUseCDN();
    }

    public boolean isShouldAutoDownload() {
        return getTdarConfiguration().shouldAutoDownload();
    }

    public void registerErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    public Date getFreemarkerProcessingTime() {
        return freemarkerProcessingTime;
    }

    public void setFreemarkerProcessingTime(Date freemarkerProcessingTime) {
        this.freemarkerProcessingTime = freemarkerProcessingTime;
    }

    public String getTosUrl() {
        return getTdarConfiguration().getTosUrl();
    }

    public String getContributorAgreementUrl() {
        return getTdarConfiguration().getContributorAgreementUrl();
    }

    public boolean isAuthenticationAllowed() {
        return getTdarConfiguration().allowAuthentication();
    }

    public FileProxy generateFileProxy(String filename, File file) {
        FileProxy fileProxy = null;
        if (filename != null && file != null) {
            fileProxy = new FileProxy(filename, file, VersionType.UPLOADED);
        }
        return fileProxy;

    }

    /**
     * Returns true if action's slug is valid, otherwise false. Additionally, if redirect is necessary and provided
     * actino immplements SlugViewAction, this method sets the action's slugSuffix based on the action's startRecord,
     * endRecord, and recordsPerPage values.
     * 
     * @param p
     * @param action
     * @return true if
     */
    // fixme: remove side-effects; use method overrides in appropriate subclasses instead of unchecked casts
    public boolean handleSlugRedirect(Persistable p, TdarActionSupport action) {
        if (p instanceof Slugable && action instanceof SlugViewAction) {
            Slugable s = (Slugable) p;
            SlugViewAction a = (SlugViewAction) action;
            if (StringUtils.isBlank(s.getSlug()) && StringUtils.isBlank(a.getSlug())) {
                return true;
            }
//            logger.debug("action: {}, slug: {}", getActionName(), a.getSlug());
//            if (Objects.equals(getActionName(), a.getSlug())) {
//                return true;
//            }
            
            if (!Objects.equals(s.getSlug(), a.getSlug())) {
                getLogger().trace("slug mismatch - wanted:{}   got:{}", s.getSlug(), a.getSlug());
                if (action instanceof SearchResultHandler<?>) {
                    SearchResultHandler<?> r = (SearchResultHandler<?>) action;
                    if (r.getStartRecord() != SearchResultHandler.DEFAULT_START || r.getRecordsPerPage() != r.getDefaultRecordsPerPage()) {
                        a.setSlugSuffix(String.format("?startRecord=%s&recordsPerPage=%s", r.getStartRecord(), r.getRecordsPerPage()));
                    }
                }
                return false;
            }

        }
        return true;
    }

    /**
     * Load up controller and then check that the user can execute function prior to calling action (used in prepare)
     * 
     * @param pc
     * @param type
     * @throws TdarActionException
     */
    public <P extends Persistable> void prepareAndLoad(PersistableLoadingAction<P> pc, RequestType type) throws TdarActionException {
        P p = null;
        Class<P> persistableClass = pc.getPersistableClass();

        // get the ID
        Long id = pc.getId();
        // if we're not null or transient, somehow we've been initialized wrongly
        if (PersistableUtils.isNotNullOrTransient(pc.getPersistable())) {
            getLogger().error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", pc.getPersistable().getId(), id);
        }
        // if the ID is not set, don't try and load/set it
        else if (PersistableUtils.isNotNullOrTransient(id)) {
            p = genericService.find(persistableClass, id);
            pc.setPersistable(p);
        }

        logRequest(pc, type, p);
        checkValidRequest(pc);
    }

    private <P extends Persistable> void logRequest(PersistableLoadingAction<P> pc, RequestType type, P p) {
        String status = "";
        String name = "";
        if (p instanceof HasStatus) {
            status = ((HasStatus) p).getStatus().toString();
        }

        if (pc.getAuthenticatedUser() != null) {
            // don't log anonymous users
            name = pc.getAuthenticatedUser().getUsername();
        } else {
            return;
        }

        if (StringUtils.isBlank(name)) {
            name = "anonymous";
        }
        String title = "";
        if (p != null && p instanceof HasName) {
            title = ((HasName) pc.getPersistable()).getName();
        }
        getLogger().info(String.format("%s is %s %s (%s): %s - %s", name, type.getLabel(), pc.getClass().getSimpleName(), pc.getId(), status, title));
    }

    /**
     * Check that the request is valid. In general, this should be able to used as is, though, it's possible to either (a) override the entire method or (b)
     * implement authorize() differently.
     * 
     * @param pc
     * @throws TdarActionException
     */
    protected <P extends Persistable> void checkValidRequest(PersistableLoadingAction<P> pc) throws TdarActionException {

        Persistable persistable = pc.getPersistable();
        // if the persistable is NULL and the ID is not null, then we have a "load" issue; if the ID is not numeric, thwn we wouldn't have even gotten here
        if (PersistableUtils.isNullOrTransient(persistable) && PersistableUtils.isNotNullOrTransient(pc.getId())) {
            // deal with the case that we have a new or not found resource
            getLogger().debug("Dealing with transient persistable {}", persistable);
            // ID specified
            if (persistable == null) {
                // persistable is null, so the lookup failed (aka not found)
                abort(StatusCode.NOT_FOUND, getText("abstractPersistableController.not_found"));
            }
            // ID is NULL or -1 too, so bad request
            else if (PersistableUtils.isNullOrTransient(persistable.getId())) {
                // id not specified or not a number, so this is an invalid request
                abort(StatusCode.BAD_REQUEST,
                        getText("abstractPersistableController.cannot_recognize_request", persistable.getClass().getSimpleName()));
            }
        }

        // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
        if (authorizationService.can(pc.getAdminRights(), pc.getAuthenticatedUser())) {
            return;
        }

        // call the locally defined "authorize" method for more specific checks
        if (pc.authorize()) {
            return;
        }

        // default is to be an error
        String errorMessage = getText("abstractPersistableController.no_permissions");
        // addActionError(errorMessage);
        abort(StatusCode.FORBIDDEN, FORBIDDEN, errorMessage);
    }

    /**
     * Throw an exception with a status code
     * 
     * @param statusCode
     * @param errorMessage
     * @throws TdarActionException
     */
    protected void abort(StatusCode statusCode, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, errorMessage);
    }

    protected void abort(StatusCode statusCode, String response, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, response, errorMessage);
    }

    protected boolean checkLogoAvailable(FilestoreObjectType type, Long id, VersionType version) {
        try {
            FileStoreFile proxy = new FileStoreFile(type, version, id, "logo" + version.toPath() + ".jpg");
            File file = getTdarConfiguration().getFilestore().retrieveFile(type, proxy);
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public boolean isTest() {
        return getTdarConfiguration().isTest();
    }

    /**
     * Indicates to view layer whether it should show the login menu (e.g. "Welcome Back, Walter Kurtz").
     * 
     * @return
     */
    public boolean isLoginMenuEnabled() {
        return false;
    }

    /**
     * Indicates whether the view layer should show sub-navigation elements (usually true while logged in, but some actions may wish to disable).
     *
     * */
    public boolean isSubnavEnabled() {
        return true;
    }
    
    public boolean isNavSearchBoxVisible() {
        return true;
    }
 
    public String getResourceCreatorRoleDocumentationUrl() {
        return getTdarConfiguration().getResourceCreatorRoleDocumentationUrl();
    }
 
    public String getLeafletApiKey() {
        return getTdarConfiguration().getLeafletMapsApiKey();
    }
    
    /**
     * show the left sidebar in the future should override page.properties
     * 
     * @return
     */
    public boolean isLeftSidebar() {
        return false;
    }
    
    /**
     * show the right sidebar in the future should override page.properties
     * 
     * @return
     */
    public boolean isRightSidebar() {
        return false;
    }
 
    public List<String> getBarColors() {
    	return getTdarConfiguration().getBarColors();
    }
}