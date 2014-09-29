package org.tdar.struts.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.struts.data.ResourceSpaceUsageStatistic;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Provides basic metadata support for controllers that manage subtypes of
 * Resource.
 * 
 * Don't extend this class unless you need this metadata to be set.
 * 
 * 
 * @author Adam Brin, <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public abstract class AbstractPersistableViewableAction<P extends Persistable> extends AuthenticationAware.Base implements Preparable, ViewableAction<P> {

    private static final long serialVersionUID = -5126488373034823160L;
    public static final String DRAFT = "draft";

    @Autowired
    private transient RecaptchaService recaptchaService;

    private AntiSpamHelper h = new AntiSpamHelper();
    private Long startTime = -1L;
    private P persistable;
    private Long id;
    private Status status;
    @SuppressWarnings("unused")
    private Class<P> persistableClass;
    public final static String msg = "%s is %s %s (%s): %s";
    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    private List<AuthorizedUser> authorizedUsers;
    private List<String> authorizedUsersFullNames = new ArrayList<String>();

    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient AuthorizationService authorizationService;

    public static String formatTime(long millis) {
        Date dt = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        // SimpleDateFormat sdf = new SimpleDateFormat("H'h, 'm'm, 's's, 'S'ms'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // no offset
        return sdf.format(dt);
    }

    protected P loadFromId(final Long id) {
        return getGenericService().find(getPersistableClass(), id);
    }

    @SkipValidation
    @HttpOnlyIfUnauthenticated
    @Action(value = VIEW,
            results = {
                    @Result(name = SUCCESS, location = "view.ftl"),
                    @Result(name = INPUT, type = HTTPHEADER, params = { "error", "404" }),
                    @Result(name = DRAFT, location = "/WEB-INF/content/errors/resource-in-draft.ftl")
            })
    public String view() throws TdarActionException {
        String resultName = SUCCESS;
        isViewable();
        // genericService.setCacheModeForCurrentSession(CacheMode.NORMAL);

        resultName = loadViewMetadata();
        loadExtraViewMetadata();
        return resultName;
    }

    /*
     * override this to load extra metadata for the "view"
     */
    public void loadExtraViewMetadata() {

    }

    /**
     * The 'contributor' property only affects which menu items we show (for now). Let non-contributors perform
     * CRUD actions, but send them a reminder about the 'contributor' option in the prefs page
     * 
     * FIXME: this needs to be centralized, as it's not going to be caught in all of the location it exists in ... eg: editColumnMetadata ...
     */
    protected void checkForNonContributorCrud() {
        if (!isContributor()) {
            // FIXME: The html here could benefit from link to the prefs page. Devise a way to hint to the view-layer that certain messages can be decorated
            // and/or replaced.
            addActionMessage(getText("abstractPersistableController.change_profile"));
        }
    }

    /**
     * Generic method enabling override for whether a record is viewable
     * 
     * @return boolean whether the user can VIEW this resource
     * @throws TdarActionException
     */
    @Override
    public boolean isViewable() throws TdarActionException {
        return true;
    }

    @Override
    public P getPersistable() {
        return persistable;
    }

    public void setPersistable(P persistable) {
        this.persistable = persistable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method is invoked when the paramsPrepareParamsInterceptor stack is
     * applied. It allows us to fetch an entity from the database based on the
     * incoming resourceId param, and then re-apply params on that resource.
     * 
     * @see <a href="http://blog.mattsch.com/2011/04/14/things-discovered-in-struts-2/">Things discovered in Struts 2</a>
     */
    @Override
    public void prepare() {
        P p = null;
        getLogger().debug("{} {}", getPersistableClass(), getId());
        if (isPersistableIdSet()) {
            getLogger().error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", getPersistable().getId(), getId());
        } else {
            p = loadFromId(getId());
            setPersistable(p);
        }
        // first check the session
        if (!(getAuthenticatedUser() == null)) {
            // don't log anonymous users
            getLogger().info("user {} is TRYING to VIEW a {}", msg);
        }
    }
    
    public void checkValidRequest() throws TdarActionException {
        if (isAuthenticationRequired()) {
            try {
                if (!getSessionData().isAuthenticated()) {
                    addActionError(getText("abstractPersistableController.must_authenticate"));
                    abort(StatusCode.OK.withResultName(LOGIN), getText("abstractPersistableController.must_authenticate"));
                }
            } catch (Exception e) {
                addActionErrorWithException(getText("abstractPersistableController.session_not_initialized"), e);
                abort(StatusCode.OK.withResultName(LOGIN), getText("abstractPersistableController.could_not_load"));
            }
        }

        Persistable persistable = getPersistable();
        if (Persistable.Base.isNullOrTransient(persistable)) {
            // deal with the case that we have a new or not found resource
            getLogger().debug("Dealing with transient persistable {}", persistable);
            if (persistable == null) {
                // persistable is null, so the lookup failed (aka not found)
                abort(StatusCode.NOT_FOUND, getText("abstractPersistableController.not_found"));
            } else if (Persistable.Base.isNullOrTransient(persistable.getId())) {
                // id not specified or not a number, so this is an invalid request
                abort(StatusCode.BAD_REQUEST,
                        getText("abstractPersistableController.cannot_recognize_request", persistable.getClass().getSimpleName()));
            }
        }
        // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
        if (authorizationService.can(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
            return;
        }

        if (isViewable()) {
            return;
        }
        String errorMessage = getText("abstractPersistableController.no_permissions");
        addActionError(errorMessage);
        abort(StatusCode.FORBIDDEN.withResultName(UNAUTHORIZED), errorMessage);
    }

    private boolean isAuthenticationRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean isPersistableIdSet() {
        return Persistable.Base.isNotNullOrTransient(getPersistable());
    }

    @Override
    public abstract Class<P> getPersistableClass();

    public void setPersistableClass(Class<P> persistableClass) {
        this.persistableClass = persistableClass;
    }

    public abstract String loadViewMetadata() throws TdarActionException;

    protected boolean isNullOrNew() {
        return !isPersistableIdSet();
    }

    /**
     * @param authorizedUsers
     *            the authorizedUsers to set
     */
    public void setAuthorizedUsers(List<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    /**
     * @return the authorizedUsers
     */
    public List<AuthorizedUser> getAuthorizedUsers() {
        if (authorizedUsers == null) {
            authorizedUsers = new ArrayList<AuthorizedUser>();
        }
        return authorizedUsers;
    }

    public AuthorizedUser getBlankAuthorizedUser() {
        AuthorizedUser user = new AuthorizedUser();
        user.setUser(new TdarUser());
        return user;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<GeneralPermissions>();
        for (GeneralPermissions permission : GeneralPermissions.values()) {
            if ((permission.getContext() == null) || getPersistable().getClass().isAssignableFrom(permission.getContext())) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * @return the startTime
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * @return the startTime
     */
    public Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(Long startTime) {
        getLogger().trace("set start time: " + startTime);
        this.startTime = startTime;
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

    public Status getStatus() {
        if (status != null) {
            return status;
        }
        if (getPersistable() instanceof HasStatus) {
            return ((HasStatus) getPersistable()).getStatus();
        }
        return null;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public List<String> getAuthorizedUsersFullNames() {
        return authorizedUsersFullNames;
    }

    public void setAuthorizedUsersFullNames(List<String> authorizedUsersFullNames) {
        this.authorizedUsersFullNames = authorizedUsersFullNames;
    }
}
