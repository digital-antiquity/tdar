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
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.web.SessionDataAware;

/**
 * $Id$
 * 
 * <p>
 * Base class for actions that require authentication or some tie-in with authentication.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface AuthenticationAware extends SessionDataAware {

    final String TYPE_REDIRECT = "redirect";

    @DoNotObfuscate(reason = "never obfuscate the session user")
    TdarUser getAuthenticatedUser();

    boolean isAuthenticated();

    boolean isBillingManager();

    /**
     * FIXME: move to top level abstract class or partial interface.
     *
     */
    public abstract static class Base extends TdarActionSupport implements AuthenticationAware {

        private static final long serialVersionUID = -7792905441259237588L;

        @Autowired
        private transient AuthorizationService authorizationService;

        @Override
        @DoNotObfuscate(reason = "never obfuscate the session user")
        public TdarUser getAuthenticatedUser() {
            if (getSessionData() == null) {
                return null;
            }
            Long tdarUserId = getSessionData().getTdarUserId();
            if (Persistable.Base.isNotNullOrTransient(tdarUserId)) {
                return getGenericService().find(TdarUser.class, tdarUserId);
            } else {
                return null;
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
        protected <P extends Persistable> void checkValidRequest(RequestType userAction, CrudAction<P> action, InternalTdarRights adminRightsCheck)
                throws TdarActionException {
            // first check the session
            String name = action.getPersistableClass().getSimpleName();
            Object[] msg = { action.getAuthenticatedUser(), userAction, name };
            if (!(action.getAuthenticatedUser() == null && "view".equalsIgnoreCase(userAction.name()))) {
                // don't log anonymous users
                getLogger().info("user {} is TRYING to {} a {}", msg);
            }

            if (userAction.isAuthenticationRequired()) {
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
                            abort(StatusCode.NOT_FOUND, getText("abstractPersistableController.not_found"));
                        } else if (Persistable.Base.isNullOrTransient(persistable.getId())) {
                            // id not specified or not a number, so this is an invalid request
                            abort(StatusCode.BAD_REQUEST,
                                    getText("abstractPersistableController.cannot_recognize_request", persistable.getClass().getSimpleName()));
                        }
                }
            }

            // the admin rights check -- on second thought should be the fastest way to execute as it pulls from cached values
            if (authorizationService.can(adminRightsCheck, action.getAuthenticatedUser())) {
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
            String errorMessage = getText("abstractPersistableController.no_permissions");
            addActionError(errorMessage);
            abort(StatusCode.FORBIDDEN.withResultName(UNAUTHORIZED), errorMessage);
        }

        protected void abort(StatusCode statusCode, String errorMessage) throws TdarActionException {
            throw new TdarActionException(statusCode, errorMessage);
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
            return isAuthenticated() && authenticatedUser.isRegistered() && authenticatedUser.getContributor();
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

    }

}
