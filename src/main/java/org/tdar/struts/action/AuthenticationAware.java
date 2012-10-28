package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
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

    public AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService();

    public abstract static class Base extends TdarActionSupport implements AuthenticationAware {

        private static final long serialVersionUID = -7792905441259237588L;

        @Autowired
        private transient AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

        public Person getAuthenticatedUser() {
            if (getSessionData() == null)
                return null;
            if (Persistable.Base.isNotNullOrTransient(getSessionData().getPerson())) {
                return getGenericService().find(Person.class, getSessionData().getPerson().getId());
            } else {
                return null;
            }
        }

        public boolean isAdministrator() {
            return isAuthenticated() && authenticationAndAuthorizationService.isAdministrator(getAuthenticatedUser());
        }

        public boolean isEditor() {
            return isAuthenticated() && authenticationAndAuthorizationService.isEditor(getAuthenticatedUser());
        }

        public boolean isAbleToFindDraftResources() {
            return isAuthenticated() && authenticationAndAuthorizationService.can(InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, getAuthenticatedUser());
        }

        public boolean isAbleToFindDeletedResources() {
            return isAuthenticated() && authenticationAndAuthorizationService.can(InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser());
        }

        public boolean isAbleToEditAnything() {
            return isAuthenticated() && authenticationAndAuthorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
        }

        public boolean isAbleToFindFlaggedResources() {
            return isAuthenticated() && authenticationAndAuthorizationService.can(InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser());
        }

        public boolean isAbleToReprocessDerivatives() {
            return isAuthenticated() && authenticationAndAuthorizationService.can(InternalTdarRights.REPROCESS_DERIVATIVES, getAuthenticatedUser());
        }

        public boolean userCan(InternalTdarRights right) {
            return isAuthenticated() && authenticationAndAuthorizationService.can(right, getAuthenticatedUser());
        }

        public boolean userCannot(InternalTdarRights right) {
            return isAuthenticated() && authenticationAndAuthorizationService.cannot(right, getAuthenticatedUser());
        }

        public boolean isContributor() {
            return isAuthenticated() && getAuthenticatedUser().getContributor();
        }

        public boolean isAuthenticated() {
            return getSessionData().isAuthenticated();
        }

        protected void createAuthenticationToken(Person person) {
            AuthenticationToken token = AuthenticationToken.create(person);
            getEntityService().save(token);
            getSessionData().setAuthenticationToken(token);
        }

        /**
         * @param <T>
         * @param modifiedResource
         * @param message
         * @param payload
         */
        protected <T extends Resource> void logResourceModification(T modifiedResource, String message, String payload) {
            getResourceService().logResourceModification(modifiedResource, getAuthenticatedUser(), message, payload);
        }

        protected <T> List<T> createListWithSingleNull() {
            ArrayList<T> list = new ArrayList<T>();
            list.add(null);
            return list;
        }

        public AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService() {
            return authenticationAndAuthorizationService;
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
                if (id != null && id >= GenericService.MINIMUM_VALID_ID) {
                    validIds.add(id);
                }
            }
            return validIds;
        }

        public int getSessionTimeout() {
            return getServletRequest().getSession().getMaxInactiveInterval();
        }
    }

}
