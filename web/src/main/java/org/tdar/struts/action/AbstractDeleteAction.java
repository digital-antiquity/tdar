package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.FilestoreLoggingException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/")
@Results(@Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.TDAR_REDIRECT, location = TdarActionSupport.UNKNOWN_ERROR))
public abstract class AbstractDeleteAction<P extends Persistable & Addressable> extends AbstractAuthenticatableAction implements Preparable {

    public final static String msg = "%s is %s %s (%s): %s";
    private String delete;
    private String deletionReason;
    private Long id;

    private boolean async = true;

    @Autowired
    private transient AuthorizationService authorizationService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private DeleteIssue deleteIssue;

    private static final long serialVersionUID = 3584381439894948194L;

    @SkipValidation
    @HttpsOnly
    @Action(value = DELETE,
            // FIXME: this won't work yet as delete is split into a GET and then a followup POST, we only want to protect the followup POST
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.DASHBOARD),
                    @Result(name = CONFIRM, location = "/WEB-INF/content/confirm-delete.ftl")
            })
    @WriteableSession
    public String delete() throws TdarActionException {
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            return INPUT;
        }

        getLogger().info("user {} is TRYING to {} a {}", getAuthenticatedUser(), getActionName(), getPersistable().getClass().getSimpleName());
        getLogger().trace("post: {} ; delete: {}", isPostRequest(), getDelete());
        if (isPostRequest() && DELETE.equals(getDelete())) {
            if (getDeleteIssue() != null) {
                addActionError(getText("abstractPersistableController.cannot_delete"));
                return CONFIRM;
            }
            logAction("DELETING");
            try {
                delete(persistable);
            } catch (FilestoreLoggingException fsl) {
                logger.error("Could not log to filestore", fsl);
                return SUCCESS;
            } catch (Exception e) {
                addActionErrorWithException(getText("abstractPersistableController.cannot_delete"), e);
                return INPUT;
            }
            return SUCCESS;
        }

        return CONFIRM;
    }

    // FIXME: resolve duplcate with AbstractPersistableController and remove
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
        getLogger().info(String.format(msg, email_, action_, getPersistable().getClass().getSimpleName().toUpperCase(), id_, name_));
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

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    private P persistable;

    @Override
    public void prepare() throws Exception {
        persistable = loadPersistable();
        if (persistable != null) {
            deleteIssue = getDeletionIssues();
            checkValidRequest();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public P getPersistable() {
        return persistable;
    }

    public void setPersistable(P persistable) {
        this.persistable = persistable;
    }

    public DeleteIssue getDeleteIssue() {
        return deleteIssue;
    }

    public void setDeleteIssue(DeleteIssue deleteIssue) {
        this.deleteIssue = deleteIssue;
    }

    public void checkValidRequest() throws TdarActionException {
        try {
            if (!getSessionData().isAuthenticated()) {
                addActionError(getText("abstractPersistableController.must_authenticate"));
                abort(StatusCode.OK, LOGIN, getText("abstractPersistableController.must_authenticate"));
            }
        } catch (Exception e) {
            addActionErrorWithException(getText("abstractPersistableController.session_not_initialized"), e);
            abort(StatusCode.OK, LOGIN, getText("abstractPersistableController.could_not_load"));
        }

        // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
        if (authorizationService.can(InternalTdarRights.DELETE_RESOURCES, getAuthenticatedUser())) {
            return;
        }

        if (canDelete()) {
            return;
        }
        String errorMessage = getText("abstractPersistableController.no_permissions");
        addActionError(errorMessage);
        abort(StatusCode.FORBIDDEN, UNAUTHORIZED, errorMessage);
    }

    abstract protected void delete(P persistable);

    abstract protected DeleteIssue getDeletionIssues();

    abstract protected boolean canDelete();

    abstract protected P loadPersistable();

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

}
