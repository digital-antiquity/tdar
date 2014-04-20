package org.tdar.struts.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.URLConstants;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.data.ResourceSpaceUsageStatistic;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

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
public abstract class AbstractPersistableController<P extends Persistable> extends AuthenticationAware.Base implements Preparable, CrudAction<P> {

    public static final String SAVE_SUCCESS_PATH = "${saveSuccessPath}?id=${persistable.id}";
    public static final String LIST = "list";
    public static final String DRAFT = "draft";

    private static final long serialVersionUID = -559340771608580602L;
    private Long startTime = -1L;
    private String delete;
    private String deletionReason;
    private String submitAction;
    private P persistable;
    private Long id;
    private Status status;
    private String saveSuccessPath = "view";
    @SuppressWarnings("unused")
    private Class<P> persistableClass;
    public final static String msg = "%s is %s %s (%s): %s";
    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    private boolean asyncSave = true;
    private List<AuthorizedUser> authorizedUsers;

    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

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

    /**
     * Override to perform custom save logic for the specific subtype of
     * Resource.
     * 
     * @param persistable
     * @return the String result code to use.
     * @throws TdarActionException
     */
    protected abstract String save(P persistable) throws TdarActionException;

    /**
     * Used to instantiate and return a new specific subtype of Resource to be
     * used by the Struts action and JSP/FTL page. Must be overridden by any
     * subclass of the AbstractResourceController.
     * 
     * @return a new instance of the specific subtype of Resource for which this
     *         ResourceController is managing requests.
     */
    protected P createPersistable() {
        try {
            return getPersistableClass().newInstance();
        } catch (Exception e) {
            addActionErrorWithException("could not instantiate class " + getPersistableClass(), e);
        }
        return null;
    }

    /**
     * Override to provide custom deletion logic for the specific kind of
     * Resource this ResourceController is managing.
     * 
     * @param persistable
     */
    protected abstract void delete(P persistable);

    protected String deleteCustom() {
        return SUCCESS;
    }

    protected void loadListData() {
    }

    public Collection<? extends Persistable> getDeleteIssues() {
        return Collections.emptyList();
    }

    @SkipValidation
    @HttpOnlyIfUnauthenticated
    @Action(value = VIEW,
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = "view.ftl"),
                    @Result(name = INPUT, type = "httpheader", params = { "error", "404" }),
                    @Result(name = DRAFT, location = "/WEB-INF/content/errors/resource-in-draft.ftl")
            })
    public String view() throws TdarActionException {
        String resultName = SUCCESS;

        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        resultName = loadViewMetadata();
        loadExtraViewMetadata();
        return resultName;
    }

    /*
     * override this to load extra metadata for the "view"
     */
    public void loadExtraViewMetadata() {

    }

    @SkipValidation
    @Action(value = DELETE, results = { @Result(name = SUCCESS, type = TYPE_REDIRECT, location = URLConstants.DASHBOARD),
            @Result(name = CONFIRM, location = "/WEB-INF/content/confirm-delete.ftl") })
    @WriteableSession
    public String delete() throws TdarActionException {
        if (isPostRequest() && DELETE.equals(getDelete())) {
            try {
                checkValidRequest(RequestType.DELETE, this, InternalTdarRights.DELETE_RESOURCES);
                checkForNonContributorCrud();
                if (CollectionUtils.isNotEmpty(getDeleteIssues())) {
                    addActionError(getText("abstractPersistableController.cannot_delete"));
                    return CONFIRM;
                }
                logAction("DELETING");
                // FIXME: deleteCustom might as well just return a boolean in this current implementation
                // should we return the result name specified by deleteCustom() instead?
                if (deleteCustom() != SUCCESS) {
                    return ERROR;
                }

                delete(persistable);
                getGenericService().delete(persistable);
            } catch (TdarActionException exception) {
                throw exception;
            } catch (Exception e) {
                addActionErrorWithException(
                        getText("abstractPersistableController.cannot_delete_reason", Arrays.asList(getPersistableClass().getSimpleName())), e);
            }
            return SUCCESS;
        }
        return CONFIRM;
    }

    @SkipValidation
    @Action(value = LIST)
    public String list() {
        loadListData();
        return SUCCESS;
    }

    @Action(value = SAVE, results = {
            @Result(name = SUCCESS, type = TYPE_REDIRECT, location = SAVE_SUCCESS_PATH),
            @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
            @Result(name = INPUT, location = "edit.ftl")
    })
    @WriteableSession
    @HttpsOnly
    public String save() throws TdarActionException {
        // checkSession();
        String actionReturnStatus = SUCCESS;
        logAction("SAVING");
        long currentTimeMillis = System.currentTimeMillis();
        boolean isNew = false;
        try {
            if (isPostRequest()) {
                checkValidRequest(RequestType.SAVE, this, InternalTdarRights.EDIT_ANYTHING);

                if (isNullOrNew()) {
                    isNew = true;
                }
                preSaveCallback();
                determineAndUpdateStatus();

                if (persistable instanceof Updatable) {
                    ((Updatable) persistable).markUpdated(getAuthenticatedUser());
                }
                if (persistable instanceof Indexable) {
                    ((Indexable) persistable).setReadyToIndex(false);
                }
                actionReturnStatus = save(persistable);

                try {
                    postSaveCallback(actionReturnStatus);
                } catch (TdarRecoverableRuntimeException tex) {
                    addActionErrorWithException(tex.getMessage(), tex);
                }

                // should there not be "one" save at all? I think this should be here
                if (shouldSaveResource()) {
                    getGenericService().saveOrUpdate(persistable);
                }

                indexPersistable();
                // who cares what the save implementation says. if there's errors return INPUT
                if (!getActionErrors().isEmpty()) {
                    getLogger().debug("Action errors found {}; Replacing return status of {} with {}", getActionErrors(), actionReturnStatus, INPUT);
                    // FIXME: if INPUT -- should I just "return"?
                    actionReturnStatus = INPUT;
                }
            } else {
                throw new TdarActionException(StatusCode.BAD_REQUEST, getText("abstactPersistableController.bad_request"));
            }
        } catch (TdarActionException exception) {
            throw exception;
        } catch (Exception exception) {
            addActionErrorWithException(getText("abstactPersistableController.unable_to_save", getPersistable()), exception);
            return INPUT;
        } finally {
            // FIXME: make sure this doesn't cause issues with SessionSecurityInterceptor now handling TdarActionExceptions
            postSaveCleanup(actionReturnStatus);
        }
        long saveTime = System.currentTimeMillis() - currentTimeMillis;
        long editTime = System.currentTimeMillis() - getStartTime();
        if (getStartTime() == -1L) {
            editTime = -1L;
        }
        if (isNew && (getPersistable() != null)) {
            getLogger().debug("Created Id: {}", getPersistable().getId());
        }
        getLogger().debug("EDIT TOOK: {} SAVE TOOK: {} (edit:{}  save:{})", new Object[] { editTime, saveTime, formatTime(editTime), formatTime(saveTime) });

        // don't allow SUCCESS response if there are actionErrors, but give the other callbacks leeway in setting their own error message.
        if (CollectionUtils.isNotEmpty(getActionErrors()) && SUCCESS.equals(actionReturnStatus)) {
            return INPUT;
        }
        return actionReturnStatus;
    }

    private void determineAndUpdateStatus() {
        if (persistable instanceof HasStatus) {
            if (getStatus() == null) {
                setStatus(Status.ACTIVE);
            }

            ((HasStatus) getPersistable()).setStatus(getStatus());
        }
    }

    protected void indexPersistable() {
        if (persistable instanceof Indexable) {
            ((Indexable) persistable).setReadyToIndex(true);
            getSearchIndexService().index((Indexable) persistable);
        }
    }

    private void logAction(String action_) {
        String name_ = "";
        String email_ = "";
        Long id_ = -1L;
        try {
            if (getPersistable() instanceof HasName) {
                name_ = ((HasName) getPersistable()).getName();
            }
        } catch (Exception e) {/* eating, yum */
        }

        try {
            id_ = getPersistable().getId();
        } catch (Exception e) {
        }
        try {
            email_ = getAuthenticatedUser().getEmail().toUpperCase();
        } catch (Exception e) {
            getLogger().debug("something weird happend, authenticated user is null");
        }
        getLogger().info(String.format(msg, email_, action_, getPersistableClass().getSimpleName().toUpperCase(), id_, name_));
    }

    protected void preSaveCallback() {
    }

    protected void postSaveCallback(String actionReturnStatus) {
    }

    /**
     * override if needed
     * 
     * @param actionReturnStatus
     */
    protected void postSaveCleanup(String actionReturnStatus) {
    }

    @SkipValidation
    @Action(value = ADD, results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = BILLING, type = TYPE_REDIRECT, location = URLConstants.CART_ADD)
    })
    @HttpsOnly
    public String add() throws TdarActionException {
        if (!isAbleToCreateBillableItem()) {
            return BILLING;
        }

        // FIXME:make this a preference...
        if ((getPersistable() instanceof HasStatus) && isEditor() && !isAdministrator()) {
            ((HasStatus) getPersistable()).setStatus(Status.DRAFT);
        }

        checkValidRequest(RequestType.CREATE, this, InternalTdarRights.EDIT_ANY_RESOURCE);
        checkForNonContributorCrud();
        logAction("CREATING");
        return loadAddMetadata();
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
            addActionMessage(getText("abstactPersistableController.change_profile"));
        }
    }

    protected boolean isAbleToCreateBillableItem() {
        return true;
    }

    public String loadAddMetadata() {
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = "edit.ftl")
    })
    @HttpsOnly
    public String edit() throws TdarActionException {
        // ensureValidEditRequest();
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        checkForNonContributorCrud();
        logAction("EDITING");
        return loadEditMetadata();
    }

    public String loadEditMetadata() throws TdarActionException {
        return loadViewMetadata();
    }

    public enum RequestType {
        EDIT(true),
        CREATE(true),
        DELETE(true),
        MODIFY_EXISTING(true),
        SAVE(true),
        VIEW(false),
        NONE(false);
        private final boolean authenticationRequired;

        private RequestType(boolean authenticationRequired) {
            this.authenticationRequired = authenticationRequired;
        }

        public boolean isAuthenticationRequired() {
            return authenticationRequired;
        }

    }

    /**
     * This method centralizes a lot of the logic around rights checking ensuring a valid session and rights if needed
     * 
     * @param userAction
     *            -- the type of request the user is making
     * @param action
     *            <P extends Persistable>
     *            -- the action that we're going to call the check on
     * @param adminRightsCheck
     *            -- the adminRights associated with the basicCheck enabling an effective override
     * @throws TdarActionException
     *             -- this will contain the return status, if any SUCCESS vs. INVALID, INPUT, ETC
     */
    // FIXME: revies and consolidate status codes where possible
    protected void checkValidRequest(RequestType userAction, CrudAction<P> action, InternalTdarRights adminRightsCheck)
            throws TdarActionException {
        // first check the session
        Object[] msg = { action.getAuthenticatedUser(), userAction, action.getPersistableClass().getSimpleName() };
        getLogger().info("user {} is TRYING to {} a {}", msg);

        if (userAction.isAuthenticationRequired()) {
            try {
                if (!getSessionData().isAuthenticated()) {
                    addActionError(getText("abstactPersistableController.must_authenticate"));
                    abort(StatusCode.OK.withResultName(LOGIN), getText("abstactPersistableController.must_authenticate"));
                }
            } catch (Exception e) {
                addActionErrorWithException(getText("abstactPersistableController.session_not_initialized"), e);
                abort(StatusCode.OK.withResultName(LOGIN), getText("abstactPersistableController.could_not_load"));
            }
        }

        Persistable persistable = action.getPersistable();
        if (Persistable.Base.isNullOrTransient(persistable)) {
            // deal with the case that we have a new or not found resource
            getLogger().debug("Dealing with transient persistable {}", persistable);
            switch (userAction) {
                case CREATE:
                case SAVE:
                    if (action.isCreatable()) {
                        return;
                    }
                    // (intentional fall-through)
                case EDIT:
                default:
                    if (persistable == null) {
                        // persistable is null, so the lookup failed (aka not found)
                        abort(StatusCode.NOT_FOUND, getText("abstactPersistableController.not_found"));
                    } else if (Persistable.Base.isNullOrTransient(persistable.getId())) {
                        // id not specified or not a number, so this is an invalid request
                        abort(StatusCode.BAD_REQUEST, getText("abstactPersistableController.cannot_recognize_request", persistable.getClass().getSimpleName()));
                    }
            }
        }

        // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
        if (getAuthenticationAndAuthorizationService().can(adminRightsCheck, action.getAuthenticatedUser())) {
            return;
        }

        switch (userAction) {
            case CREATE:
                if (action.isCreatable()) {
                    return;
                }
                break;
            case EDIT:
            case MODIFY_EXISTING:
                if (action.isEditable()) {
                    return;
                }
                break;
            case VIEW:
                if (action.isViewable()) {
                    return;
                }
                break;
            case SAVE:
                if (action.isSaveable()) {
                    return;
                }
                break;
            case DELETE:
                if (action.isDeleteable()) {
                    return;
                }
                break;
            default:
                break;
        }
        addActionError(getText("abstactPersistableController.no_permissions"));
        abort(StatusCode.FORBIDDEN.withResultName(UNAUTHORIZED), getText("abstactPersistableController.no_permissions"));

    }

    protected void abort(StatusCode statusCode, String errorMessage) throws TdarActionException {
        getServletResponse().setStatus(statusCode.getHttpStatusCode());
        throw new TdarActionException(statusCode, errorMessage);
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
    public boolean isCreatable() throws TdarActionException {
        return true;
    }

    /**
     * Generic method enabling override for whether a record is editable
     * 
     * @return boolean whether the user can EDIT this resource
     * @throws TdarActionException
     */
    @Override
    public boolean isEditable() throws TdarActionException {
        return false;
    }

    /**
     * Generic method enabling override for whether a record is deleteable
     * 
     * @return boolean whether the user can DELETE this resource (default calls isEditable)
     * @throws TdarActionException
     */
    @Override
    public boolean isDeleteable() throws TdarActionException {
        return isEditable();
    }

    /**
     * Generic method enabling override for whether a record is saveable
     * 
     * @return boolean whether the user can SAVE this resource (default is TRUE for NEW resources, calls isEditable for existing)
     * @throws TdarActionException
     */
    @Override
    public boolean isSaveable() throws TdarActionException {
        if (isNullOrNew()) {
            return true;
        } else {
            return isEditable();
        }
    }

    /**
     * Used to signal confirmation of deletion requests.
     * 
     * @param delete
     *            the delete to set
     */
    public void setDelete(String delete) {
        this.delete = delete;
    }

    /**
     * this is the "override" that gets set when a user clicks on the "confirm" button to confirm
     * they want to delete a record
     * 
     * @return the delete
     */
    public String getDelete() {
        return delete;
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

        if (isPersistableIdSet()) {
            getLogger().error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", getPersistable().getId(), getId());
        }
        if (Persistable.Base.isNullOrTransient(getId())) {
            setPersistable(createPersistable());
        } else {

            P p = loadFromId(getId());
            // from a permissions standpoint... being really strict, we should mark this as read-only
            // getGenericService().markReadOnly(p);
            getLogger().info("id:{}, persistable:{}", getId(), p);
            setPersistable(p);
        }

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
     * Returns true if we need to checkpoint and save the resource at various stages to handle many-to-one relationships
     * properly (due to cascading not working properly)
     * 
     * @return
     */
    public boolean shouldSaveResource() {
        return true;
    }

    public void setAsync(boolean async) {
        this.asyncSave = async;
    }

    public boolean isAsync() {
        return asyncSave;
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
        getLogger().info("set start time: " + startTime);
        this.startTime = startTime;
    }

    @Override
    public void validate() {
        reportAnyJavascriptErrors();
        getLogger().debug("validating resource {} - {}", getPersistable(), getPersistableClass().getSimpleName());
        if (getPersistable() == null) {
            getLogger().warn("Null being validated.");
            addActionError(getText("abstactPersistableController.could_not_find"));
            return;
        }
        // String resourceTypeLabel = getPersistable().getResourceType().getLabel();
        if (getPersistable() instanceof Validatable) {
            try {
                boolean valid = ((Validatable) getPersistable()).isValidForController();
                if (!valid) {
                    addActionError(getText("abstactPersistableController.could_not_validate", getPersistable()));
                }
            } catch (Exception e) {
                addActionError(e.getMessage());
            }
        }
    }

    /*
     * This method returns the base URL for where a save should go, in 99% of the cases,
     * this goes to <b>view</b>
     */
    public String getSaveSuccessPath() {
        return saveSuccessPath;
    }

    /*
     * This method sets the base URL for where a save should go, in 99% of the cases,
     * this method should not be needed
     */
    public void setSaveSuccessPath(String successPath) {
        this.saveSuccessPath = successPath;
    }

    public String getSubmitAction() {
        return submitAction;
    }

    public void setSubmitAction(String submitAction) {
        this.submitAction = submitAction;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
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
        return new ArrayList<Status>(getAuthenticationAndAuthorizationService().getAllowedSearchStatuses(getAuthenticatedUser()));
    }

}
