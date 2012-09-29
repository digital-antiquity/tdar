package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.CrowdService;
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
public interface AuthenticationAware extends ServletRequestAware, ServletResponseAware, SessionDataAware {

    public final static String AUTHENTICATED = "authenticated";
    public final static String UNAUTHORIZED = "unauthorized";

    public CrowdService getCrowdService();

    public abstract static class Base extends TdarActionSupport implements AuthenticationAware {

        private static final long serialVersionUID = -7792905441259237588L;

        private HttpServletRequest servletRequest;
        private HttpServletResponse servletResponse;
        @Autowired
        private transient CrowdService crowdService;

        public Person getAuthenticatedUser() {
            return getSessionData().getPerson();
        }

        public boolean isAdministrator() {
            return isAuthenticated() && crowdService.isAdministrator(getSessionData().getPerson());
        }

        public boolean isContributor() {
            return isAuthenticated() && getSessionData().getPerson().isContributor();
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

        public CrowdService getCrowdService() {
            return crowdService;
        }

        public void setCrowdService(CrowdService crowdService) {
            this.crowdService = crowdService;
        }

        protected HttpServletRequest getServletRequest() {
            return servletRequest;
        }

        public void setServletRequest(HttpServletRequest servletRequest) {
            this.servletRequest = servletRequest;
        }

        protected HttpServletResponse getServletResponse() {
            return servletResponse;
        }

        public void setServletResponse(HttpServletResponse servletResponse) {
            this.servletResponse = servletResponse;
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
