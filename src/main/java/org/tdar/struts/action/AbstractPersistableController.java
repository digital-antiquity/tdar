package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
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
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.resource.AbstractResourceController;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id: AbstractResourceController.java 2695 2011-08-05 18:30:32Z abrin $
 * 
 * Provides basic metadata support for controllers that manage subtypes of
 * Resource.
 * 
 * Don't extend this class unless you need this metadata to be set.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 2695 $
 */
@Results({ @Result(name = AbstractResourceController.REDIRECT_HOME, type = "redirect", location = URLConstants.HOME),
        @Result(name = AbstractResourceController.REDIRECT_PROJECT_LIST, type = "redirect", location = URLConstants.DASHBOARD) })
public abstract class AbstractPersistableController<P extends Persistable> extends AuthenticationAware.Base implements Preparable {

    public static final String CONFIRM = "confirm";
    public static final String DELETE_CONSTANT = "delete";
    private static final long serialVersionUID = -559340771608580602L;
    private Long startTime = -1L;
    private String delete;
    private P persistable;
    private Long id;
    @SuppressWarnings("unused")
    private Class<P> persistableClass;
    public final static String msg = "%s is %s %s (%s): %s";
    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    private boolean asyncSave = true;
    private List<AuthorizedUser> authorizedUsers;

    protected P loadFromId(final Long id) {
        setPersistable(getGenericService().find(getPersistableClass(), id));
        return persistable;
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
        return new ArrayList<Persistable>();
    }

    @SkipValidation
    @Action(value = "view", interceptorRefs = { @InterceptorRef("unAuthenticatedStack") }, results = { 
                    @Result(name = SUCCESS, location = "view.ftl"),
                    @Result(name = GONE, location = "/WEB-INF/content/resource-deleted.ftl"),
                    @Result(name = NOT_FOUND, location = "/WEB-INF/content/page-not-found.ftl") })
    public String view() {
        String toReturn = validateViewRequest();
        //a "new" record (id == -1) is not appropriate for a view page
        if(toReturn == SUCCESS)
        {
            toReturn = loadMetadata();
            loadExtraViewMetadata();
        }
        return toReturn;
    }
    
    //return SUCCESS if request is valid for a "view" type action (the persistable must not be null or 'new').
    protected String validateViewRequest() {
        String toReturn = SUCCESS;
        if(getPersistable() == null) {
            getServletResponse().setStatus(HttpStatus.SC_NOT_FOUND);
            toReturn = NOT_FOUND;
        } else if(getPersistable().getId() == -1) {
            //show the user "not found" page,  but use "bad request" page for more informative logging.
            getServletResponse().setStatus(HttpStatus.SC_BAD_REQUEST); 
            toReturn = NOT_FOUND;
        }
        if(toReturn != SUCCESS) {
            logger.warn("view page requested without valid ID. Request was '{}' ", getServletRequest().getQueryString());            
        }
        return toReturn;
    }

    /*
     * override this to load extra metadata for the "view"
     */
    public void loadExtraViewMetadata() {

    }

    @SkipValidation
    @Action(value = "add", results = { @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, type = "redirect", location = "edit") })
    public String add() {
        logAction("CREATING");
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = DELETE_CONSTANT, results = { @Result(name = SUCCESS, type = "redirect", location = URLConstants.DASHBOARD),
            @Result(name = CONFIRM, location = "/WEB-INF/content/confirm-delete.ftl") })
    public String delete() {
        checkSession();
        if (getServletRequest() != null && getServletRequest().getMethod() != null
                && getServletRequest().getMethod().equalsIgnoreCase("post")
                && getDelete() != null && getDelete().equals(DELETE_CONSTANT)) {

            try {
                if (isNullOrNew()) {
                    logger.warn("Null item, turning delete into a no-op");
                    return REDIRECT_HOME;
                } else if (!isEditable() && !isAdministrator()) {
                    String msg = String.format("user %s does not have the rights to delete this resource %d", getAuthenticatedUser(), getPersistable().getId());
                    logger.warn(msg);
                    return REDIRECT_HOME;
                }

                if (getDeleteIssues() != null && getDeleteIssues().size() > 0) {
                    addActionError("cannot delete item because references still exist");
                    return CONFIRM;
                }
                logAction("DELETING");
                if (deleteCustom() != SUCCESS)
                    return ERROR;

                delete(persistable);
                if (persistable instanceof HasStatus) {
                    ((HasStatus) persistable).setStatus(Status.DELETED);
                    getGenericService().saveOrUpdate(persistable);
                } else {
                    getGenericService().delete(persistable);
                }
                // purgeFromArchive(resource);
            } catch (Exception e) {
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
            @Result(name = SUCCESS, type = "redirect", location = "view?id=${persistable.id}"),
            @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
            @Result(name = INPUT, location = "edit.ftl")
    })
    public String save() {
        checkSession();
        String actionReturnStatus = SUCCESS;
        logAction("SAVING");
        long currentTimeMillis = System.currentTimeMillis();
        boolean isNew = true;
        try {
            if (getPersistable() == null) {
                logger.warn("Trying to save but {} was null, returning INPUT", getPersistableClass().getSimpleName());
                return INPUT;
            }
            if (getPersistable().getId() != null && getPersistable().getId() > 0) {
                isNew = false;
            }
            preSaveCallback();
            // First time it to make sure that the record is valid for a save
            if (persistable instanceof Updatable) {
                ((Updatable) persistable).markUpdated(getAuthenticatedUser());
            }
            actionReturnStatus = save(persistable);

        } catch (Exception exception) {
            addActionErrorWithException("Sorry, we were unable to save: " + getPersistable(), exception);
            return INPUT;
        } finally {
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

    private void checkSession() {
        try {
            if (getSessionData().getAuthenticationToken().getId() > -1 && getSessionData().getPerson().getId() > -1) {
                // ok good we have a session
            } else {
                addActionError("something wrong with the session, was it initialized properly?");
            }
        } catch (Exception e) {
            addActionErrorWithException("something wrong with the session, was it initialized properly?", e);
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
            @Result(name = INPUT, location = "add", type = "redirect"),
            @Result(name = UNAUTHORIZED, location = "/WEB-INF/content/unauthorized.ftl")
    })
    public String edit() {
        checkSession();
        if (isNullOrNew()) {
            logger.warn("Trying to edit but entity was null, returning INPUT");
            return INPUT;
        } else if (isEditable() || isAdministrator()) {
            logAction("EDITING");
            return loadMetadata();
        } else {
            addActionError(String
                    .format("You do not have permissions to edit the %s (ID: %s)", getPersistableClass().getSimpleName(), getPersistable().getId()));
            return UNAUTHORIZED;
        }
    }

    public boolean isEditable() {
        return false;
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
            setPersistable(loadFromId(getId()));
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
}
