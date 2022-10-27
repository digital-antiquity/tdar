package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericService;
import org.tdar.struts_base.action.AuthenticationAware;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractAuthenticatableAction extends TdarBaseActionSupport implements AuthenticationAware {

    private static final long serialVersionUID = -4055376095680758009L;

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

    protected void abort(StatusCode statusCode, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, errorMessage);
    }

    protected void abort(StatusCode statusCode, String response, String errorMessage) throws TdarActionException {
        throw new TdarActionException(statusCode, response, errorMessage);
    }

    public boolean isAdministrator() {
        return isAuthenticated() && getAuthorizationService().isAdministrator(getAuthenticatedUser());
    }

    public boolean isEditor() {
        return isAuthenticated() && getAuthorizationService().isEditor(getAuthenticatedUser());
    }

    public boolean isAbleToFindDraftResources() {
        return isAuthenticated() && getAuthorizationService().can(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToFindDeletedResources() {
        return isAuthenticated() && getAuthorizationService().can(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToEditAnything() {
        return isAuthenticated() && getAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
    }

    public boolean isAbleToFindFlaggedResources() {
        return isAuthenticated() && getAuthorizationService().can(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser());
    }

    public boolean isAbleToReprocessDerivatives() {
        return isAuthenticated() && getAuthorizationService().can(InternalTdarRights.REPROCESS_DERIVATIVES, getAuthenticatedUser());
    }

    public boolean userCan(InternalTdarRights right) {
        return isAuthenticated() && getAuthorizationService().can(right, getAuthenticatedUser());
    }

    public boolean userCannot(InternalTdarRights right) {
        return isAuthenticated() && getAuthorizationService().cannot(right, getAuthenticatedUser());
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
        return getAuthorizationService().isBillingManager(getAuthenticatedUser());
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

    public boolean isAccessionFeeEnabled() {
        return getTdarConfiguration().isAccessionFeesEnabled();
    }
}
