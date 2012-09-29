package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.URLConstants;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.resource.AbstractResourceController;

import com.opensymphony.xwork2.Preparable;

import static org.tdar.core.bean.Persistable.Base.*;

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
@Results({ @Result(name = AbstractResourceController.REDIRECT_HOME, type = "redirect", location = URLConstants.HOME),
        @Result(name = AbstractResourceController.REDIRECT_PROJECT_LIST, type = "redirect", location = URLConstants.DASHBOARD) })
public abstract class AbstractPersistableController<P extends Persistable> extends AuthenticationAware.Base implements Preparable, CrudAction<P> {

    public static final String CONFIRM = "confirm";
    public static final String DELETE_CONSTANT = "delete";
    private static final long serialVersionUID = -559340771608580602L;
    private Long startTime = -1L;
    private String delete;
    private String submitAction;
    private P persistable;
    private Long id;
    private String saveSuccessPath = "view";
    @SuppressWarnings("unused")
    private Class<P> persistableClass;
    public final static String msg = "%s is %s %s (%s): %s";
    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    private boolean asyncSave = true;
    private List<AuthorizedUser> authorizedUsers;

    protected P loadFromId(final Long id) {
        return getGenericService().find(getPersistableClass(), id);
    }

    /**
     * Override to perform custom save logic for the specific subtype of
     * Resource.
     * 
     * @param resource
     * @return the String result code to use.
     */
    protected abstract String save(P persistable);

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
     * @param resource
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
    @Action(value = "view",
        interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
        results = {
            @Result(name = SUCCESS, location = "view.ftl"),
            @Result(name = INPUT, type="httpheader", params={"error", "404"}),
            @Result(name = "draft", location = "/WEB-INF/content/errors/resource-in-draft.ftl")
    })
    public String view() throws TdarActionException {
        String resultName = SUCCESS;
        // ensureValidViewRequest();
        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        // checkValidRequest(UserIs.ANYONE, UsersCanModify.NONE, isViewable(), InternalTdarRights.VIEW_ANYTHING);
        resultName = loadMetadata();
        loadExtraViewMetadata();
        return resultName;
    }

    /*
     * override this to load extra metadata for the "view"
     */
    public void loadExtraViewMetadata() {

    }

    @SkipValidation
    @Action(value = "add", results = { @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, type = "redirect", location = "edit") })
    public String add() throws TdarActionException {
        checkValidRequest(RequestType.CREATE, this, InternalTdarRights.EDIT_ANY_RESOURCE);
        logAction("CREATING");
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = DELETE_CONSTANT, results = { @Result(name = SUCCESS, type = "redirect", location = URLConstants.DASHBOARD),
            @Result(name = CONFIRM, location = "/WEB-INF/content/confirm-delete.ftl") })
    @WriteableSession
    public String delete() throws TdarActionException {
        if (isPostRequest() && DELETE_CONSTANT.equals(getDelete())) {
        	try {
        		checkValidRequest(RequestType.DELETE, this, InternalTdarRights.DELETE_RESOURCES);
        		if (CollectionUtils.isNotEmpty(getDeleteIssues())) {
        			addActionError("cannot delete item because references still exist");
        			return CONFIRM;
        		}
        		logAction("DELETING");
        		// FIXME: deleteCustom might as well just return a boolean in this current implementation
        		// should we return the result name specified by deleteCustom() instead?
        		if (deleteCustom() != SUCCESS)
        			return ERROR;

        		delete(persistable);
        		// FIXME: push this logic to the service layer
        		if (persistable instanceof HasStatus) {
        			((HasStatus) persistable).setStatus(Status.DELETED);
        			getGenericService().saveOrUpdate(persistable);
        		} else {
        			getGenericService().delete(persistable);
        		}
        	}
        	catch (TdarActionException exception) {
        		throw exception;
        	}
        	catch (Exception e) {
        		addActionErrorWithException("could not delete " + getPersistableClass().getSimpleName(), e);
        	}
            return SUCCESS;
        }
        return CONFIRM;
    }

    @SkipValidation
    @Action(value = "list")
    public String list() {
        loadListData();
        return SUCCESS;
    }

    @Action(value = "save", results = {
            @Result(name = SUCCESS, type = "redirect", location = "${saveSuccessPath}?id=${persistable.id}"),
            @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
            @Result(name = INPUT, location = "edit.ftl")
    })
    @WriteableSession
    public String save() throws TdarActionException {
        // checkSession();
        String actionReturnStatus = SUCCESS;
        logAction("SAVING");
        long currentTimeMillis = System.currentTimeMillis();
        boolean isNew = false;
        try {
            checkValidRequest(RequestType.SAVE, this, InternalTdarRights.EDIT_ANYTHING);

            if (isNullOrNew()) {
                isNew = true;
            }
            preSaveCallback();
            if (persistable instanceof Updatable) {
                ((Updatable) persistable).markUpdated(getAuthenticatedUser());
            }
            actionReturnStatus = save(persistable);
        }
        catch (TdarActionException exception) {
        	throw exception;
        }
        catch (Exception exception) {
        	addActionErrorWithException("Sorry, we were unable to save: " + getPersistable(), exception);
            return INPUT;
        }
        finally {
            // FIXME: make sure this doesn't cause issues with SessionSecurityInterceptor now handling TdarActionExceptions
            postSaveCleanup();
        }
        try {
            postSaveCallback();
        } catch (TdarRecoverableRuntimeException tex) {
            addActionErrorWithException(tex.getMessage(), tex);
        }
        currentTimeMillis -= System.currentTimeMillis();
        Long editTime = System.currentTimeMillis() - getStartTime();
        if (getStartTime() == -1L) {
            editTime = -1L;
        }
        if (isNew && getPersistable() != null) {
            logger.debug("Created Id: {}", getPersistable().getId());
        }
        logger.debug("EDIT TOOK: {} SAVE TOOK: {}", editTime, Math.abs(currentTimeMillis));
        return actionReturnStatus;
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
            logger.debug("something weird happend, authenticated user is null");
        }
        logger.info(String.format(msg, email_, action_, getPersistableClass().getSimpleName().toUpperCase(), id_, name_));
    }

    protected void preSaveCallback() {
    }

    protected void postSaveCallback() {
    }

    /**
     * override if needed
     */
    protected void postSaveCleanup() {
    }

    @SkipValidation
    @Action(value = "edit", results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = "add", type = "redirect")
    })
    public String edit() throws TdarActionException {
        // ensureValidEditRequest();
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        logAction("EDITING");
        return loadMetadata();
    }

//    @SuppressWarnings("unused")
//    private void ensureValidEditRequest() throws TdarActionException {
//        // first make sure the user is even authenticated
//        if (!getSessionData().isAuthenticated()) {
//            abort(StatusCode.OK.withResultName(LOGIN), "Unauthenticated edit request.");
//        }
//        if (!isEditable()) {
//            // we performed an authz check that failed (not a structural issue with the persistable)
//            abort(StatusCode.UNAUTHORIZED,
//                    String.format("You do not have permissions to edit %s (id: %s)", getPersistableClass().getSimpleName(), getPersistable().getId()));
//        }
//    }

    protected enum RequestType {
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
     * @param RequestType
     *            -- the type of request the user is making
     * @param CrudAction
     *            <P extends Persistable>
     *            -- the action that we're going to call the check on
     * @param adminRightsCheck
     *            -- the adminRights associated with the basicCheck enabling an effective override
     * @throws TdarActionException
     *             -- this will contain the return status, if any SUCCESS vs. INVALID, INPUT, ETC
     */
    protected void checkValidRequest(RequestType userAction, CrudAction<P> action, InternalTdarRights adminRightsCheck)
            throws TdarActionException {
        // first check the session
        Object[] msg = { action.getAuthenticatedUser(), userAction, action.getPersistableClass().getSimpleName() };
        logger.trace("user {} is TRYING to {} a {}", msg);

        if (userAction.isAuthenticationRequired()) {
            try {
                if (!getSessionData().isAuthenticated()) {
                    addActionError("you must authenticate");
                    abort(StatusCode.OK.withResultName(LOGIN), "you must authenticate");
                }
            } catch (Exception e) {
                addActionErrorWithException("something wrong with the session, was it initialized properly?", e);
                abort(StatusCode.OK.withResultName(LOGIN), "could not load item, no user on session");
            }
        }
        
        Persistable persistable = action.getPersistable();
        if (isNullOrTransient(persistable)) {
        // deal with the case that we have a new or not found resource
            logger.debug("Dealing with transient persistable {}", persistable);
            switch (userAction) {
                case CREATE:
                case SAVE:
                    if (action.isCreatable()) {
                        return;
                    }
                case EDIT:
                default:

                    if (persistable == null) {
                        // persistable is null, so the lookup failed (aka not found)
                        abort(StatusCode.NOT_FOUND, String.format("Sorry, the page you requested cannot be found"));
                    } else if (persistable.getId() == -1) {
                        // id not specified or not a number, so this is an invalid request
                        abort(StatusCode.BAD_REQUEST, String.format(
                                "Sorry, tDAR does not recognize this type of request on a %s ", persistable.getClass().getSimpleName()));
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
        }
        addActionError("user does not have permissions to perform the requested action");
        abort(StatusCode.FORBIDDEN.withResultName(GONE), "could not load requested item (insufficient permissions -- may not be able to view deleted resources)");

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
    public boolean isViewable() throws TdarActionException {
        return true;
    }

    public boolean isCreatable() throws TdarActionException {
        return true;
    }

    /**
     * Generic method enabling override for whether a record is editable
     * 
     * @return boolean whether the user can EDIT this resource
     * @throws TdarActionException
     */
    public boolean isEditable() throws TdarActionException {
        return false;
    }

    /**
     * Generic method enabling override for whether a record is deleteable
     * 
     * @return boolean whether the user can DELETE this resource (default calls isEditable)
     * @throws TdarActionException
     */
    public boolean isDeleteable() throws TdarActionException {
        return isEditable();
    }

    /**
     * Generic method enabling override for whether a record is saveable
     * 
     * @return boolean whether the user can SAVE this resource (default is TRUE for NEW resources, calls isEditable for existing)
     * @throws TdarActionException
     */
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

    /*
     * This method is invoked when the paramsPrepareParamsInterceptor stack is
     * applied. It allows us to fetch an entity from the database based on the
     * incoming resourceId param, and then re-apply params on that resource.
     */
    public void prepare() {

        if (isPersistableIdSet()) {
            logger.error("item id should not be set yet -- persistable.id:{}\t controller.id:{}", getPersistable().getId(), getId());
        }
        if (getId() == null || getId() == -1L) {
            setPersistable(createPersistable());
        } else {

            P p = loadFromId(getId());
            // from a permissions standpoint... being really strict, we should mark this as read-only
            // getGenericService().markReadOnly(p);
            logger.trace("id:{}, persistable:{}", getId(), p);
            setPersistable(p);
        }

    }

    protected boolean isPersistableIdSet() {
        return getPersistable() != null && (getPersistable().getId() != null && getPersistable().getId() != -1L);
    }

    public abstract Class<P> getPersistableClass();

    public void setPersistableClass(Class<P> persistableClass) {
        this.persistableClass = persistableClass;
    }

    public abstract String loadMetadata();

    protected boolean isNullOrNew() {
        return getPersistable() == null || getPersistable().getId() == null || getPersistable().getId() == -1L;
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
        user.setUser(new Person());
        return user;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<GeneralPermissions>();
        for (GeneralPermissions permission : GeneralPermissions.values()) {
            if (permission.getContext() == null || getPersistable().getClass().isAssignableFrom(permission.getContext())) {
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
        logger.info("set start time: " + startTime);
        this.startTime = startTime;
    }

    @Override
    public void validate() {
        logger.debug("validating resource {} - {}", getPersistable(), getPersistableClass().getSimpleName());
        if (getPersistable() == null) {
            logger.warn("Null being validated.");
            addActionError("Sorry, we couldn't find the resource you specified.");
            return;
        }
        // String resourceTypeLabel = getPersistable().getResourceType().getLabel();
        if (getPersistable() instanceof Validatable) {
            try {
                boolean valid = ((Validatable) getPersistable()).isValidForController();
                if (!valid) {
                    addActionError("could not validate:" + getPersistable());
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

}
