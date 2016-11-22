package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts_base.action.AuthenticationAware;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractAuthenticatableAction extends TdarBaseActionSupport implements AuthenticationAware {

    private static final long serialVersionUID = -4055376095680758009L;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    @DoNotObfuscate(reason = "never obfuscate the session user")
    public TdarUser getAuthenticatedUser() {
        if (getSessionData() == null) {
            return null;
        }
        Long tdarUserId = getSessionData().getTdarUserId();
        if (PersistableUtils.isNotNullOrTransient(tdarUserId)) {
            return getGenericService().find(TdarUser.class, tdarUserId);
        } else {
            return null;
        }
    }

    public <P extends Persistable> boolean authorize(PersistableLoadingAction<Persistable> action) throws TdarActionException {

        String name = action.getPersistableClass().getSimpleName();
        Object[] msg = { action.getAuthenticatedUser(), name, action.getPersistableClass().getSimpleName() };
        if (!(action.getAuthenticatedUser() == null && "view".equalsIgnoreCase(name))) {
            // don't log anonymous users
            getLogger().info("user {} is TRYING to {} a {}", msg);
        }

        // first check the session
        Persistable persistable = action.getPersistable();

        if (PersistableUtils.isNullOrTransient(persistable) || PersistableUtils.isNullOrTransient(action.getId())) {
            // deal with the case that we have a new or not found resource
            getLogger().debug("Dealing with transient persistable {}", persistable);
            return true;
        }
        if (persistable == null) {
            // persistable is null, so the lookup failed (aka not found)
            abort(StatusCode.NOT_FOUND, getText("abstractPersistableController.not_found"));
        } else if (PersistableUtils.isNullOrTransient(persistable.getId())) {
            // id not specified or not a number, so this is an invalid request
            abort(StatusCode.BAD_REQUEST,
                    getText("abstractPersistableController.cannot_recognize_request", persistable.getClass().getSimpleName()));
        }

        // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
        if (authorizationService.can(action.getAdminRights(), action.getAuthenticatedUser())) {
            return true;
        }
        String errorMessage = getText("abstractPersistableController.no_permissions");
        addActionError(errorMessage);
        abort(StatusCode.FORBIDDEN, UNAUTHORIZED, errorMessage);
        return false;
    }

    protected void abort(StatusCode statusCode, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, errorMessage);
    }

    protected void abort(StatusCode statusCode, String response, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, response, errorMessage);
    }

    public boolean isAdministrator() {
        return isAuthenticated() && authorizationService.isAdministrator(getAuthenticatedUser());
    }

    public boolean isEditor() {
        return isAuthenticated() && authorizationService.isEditor(getAuthenticatedUser());
    }

    public boolean isAbleToFindDraftResources() {
        return isAuthenticated() && authorizationService.can(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToFindDeletedResources() {
        return isAuthenticated() && authorizationService.can(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToEditAnything() {
        return isAuthenticated() && authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
    }

    public boolean isAbleToFindFlaggedResources() {
        return isAuthenticated() && authorizationService.can(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToReprocessDerivatives() {
        return isAuthenticated() && authorizationService.can(InternalTdarRights.REPROCESS_DERIVATIVES, getAuthenticatedUser());
    }

    public boolean userCan(InternalTdarRights right) {
        return isAuthenticated() && authorizationService.can(right, getAuthenticatedUser());
    }

    public boolean userCannot(InternalTdarRights right) {
        return isAuthenticated() && authorizationService.cannot(right, getAuthenticatedUser());
    }

    public boolean isContributor() {
        TdarUser authenticatedUser = getAuthenticatedUser();
        return isAuthenticated() && authenticatedUser.isRegistered() && authenticatedUser.isContributor();
    }

    @Override
    public boolean isAuthenticated() {
        return getSessionData().isAuthenticated();
    }

    protected <T> List<T> createListWithSingleNull() {
        ArrayList<T> list = new ArrayList<T>();
        list.add(null);
        return list;
    }

    /**
     * Return filtered list containing only valid id's (or null if given null)
     */
    protected List<Long> filterInvalidUsersIds(List<Long> rawIds) {
        if (CollectionUtils.isEmpty(rawIds)) {
            return Collections.emptyList();
        }

        List<Long> validIds = new ArrayList<Long>();
        for (Long id : rawIds) {
            if ((id != null) && (id >= GenericService.MINIMUM_VALID_ID)) {
                validIds.add(id);
            }
        }
        return validIds;
    }

    public int getSessionTimeout() {
        return getServletRequest().getSession().getMaxInactiveInterval();
    }

    /**
     * return true if authenticated user has permission to assign other users as the owner of an invoice
     * 
     * @return
     */
    @Override
    public boolean isBillingManager() {
        return authorizationService.isBillingManager(getAuthenticatedUser());
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    /**
     * Indicates to view layer whether it should show the login menu (e.g. "Welcome Back, Walter Kurtz").
     * 
     * @return
     */
    @Override
    public boolean isLoginMenuEnabled() {
        return getAuthenticatedUser() != null;
    }
}
