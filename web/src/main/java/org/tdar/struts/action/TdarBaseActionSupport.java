package org.tdar.struts.action;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Slugable;
import org.tdar.core.exception.LocalizableException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.struts.WROProfile;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.web.WebFileSystemResourceService;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.TdarServletConfiguration;

import ro.isdc.wro.model.resource.ResourceType;

public class TdarBaseActionSupport extends TdarActionSupport {

    @Autowired
    private transient WebFileSystemResourceService webFilesystemResourceService;

    @Autowired
    private transient AuthorizationService authorizationService;

    private boolean hideExceptionArea = false;


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

    private static final long serialVersionUID = -2172116718934925751L;

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
        getStackTraces().add(ExceptionWrapper.convertExceptionToCode(exception));
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

    public String getWroTempDirName() {
        return webFilesystemResourceService.getWroDir();
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
        getLogger().debug("{} {}", persistableClass, id);
        // if we're not null or transient, somehow we've been initialized wrongly
        if (PersistableUtils.isNotNullOrTransient(pc.getPersistable())) {
            getLogger().error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", pc.getPersistable().getId(), id);
        }
        // if the ID is not set, don't try and load/set it
        else if (PersistableUtils.isNotNullOrTransient(id)) {
            p = getGenericService().find(persistableClass, id);
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

}
