package org.tdar.struts_base.action;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
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
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts_base.ErrorListener;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.jaxb.ActionErrorWrapper;

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
public abstract class TdarActionSupport extends ActionSupport implements ServletRequestAware, ServletResponseAware, ActionErrorWrapper {

    private static final long serialVersionUID = 7084489869489013998L;

    // result name constants
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
        
        if (fieldErrors.size() > 0 && !hasActionErrors()) {
            addActionError(getText("tdarActionSupport.error_occurred"));
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
    public String getMoreInfoUrlKey() {
        return moreInfoUrlKey;
    }

    public void setMoreInfoUrlKey(String moreInfoUrl) {
        this.moreInfoUrlKey = moreInfoUrl;
    }



    public void addActionErrors(List<String> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (String error : errors) {
            addActionError(error);
        }
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
    
    public boolean isSelenium() {
        return getTdarConfiguration().isSelenium();
    }
    
    @Override
    public Collection<String> getErrorMessages() {
        return getActionErrors();
    }

    /**
     * Remove null items from the specified list(s).
     * @param lists
     */
    public final void stripNulls(List<?> ...lists) {
        Arrays.stream(lists)
                //if list is null itself, skip it
                .filter(list -> list != null)
                //otherwise, remove all the null items
                .forEach(list -> {
                    list.removeIf(item -> item == null);
                });
    }

    public final boolean isListCollectionsEnabled() {
        return TdarConfiguration.getInstance().isListCollectionsEnabled();
    }
}