package org.tdar.balk.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts_base.action.AuthenticationAware;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractAuthenticatedAction extends TdarActionSupport implements AuthenticationAware {

    private static final long serialVersionUID = -4055376095680758009L;

    @Autowired
    private transient AuthorizationService authorizationService;

    protected String cleanupPath(String pathFormat, String path) {
        String format = String.format(pathFormat, getContextPath(), path);
        format = StringUtils.replace(format, "/balk/balk/", "/balk/");
        format = StringUtils.replace(format, "/balk//balk/", "/balk/");
        getLogger().debug("redirectPath: {}", format);
        return format;

    }

    @Override
    @DoNotObfuscate(reason = "never obfuscate the session user")
    public TdarUser getAuthenticatedUser() {
        if (getSessionData() == null) {
            return null;
        }
        Long tdarUserId = getSessionData().getTdarUserId();
        if (PersistableUtils.isNotNullOrTransient(tdarUserId)) {
            TdarUser user = new TdarUser();
            user.setUsername(getSessionData().getUsername());
            user.setId(getSessionData().getTdarUserId());
            return user;
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
        return isAuthenticated() && authorizationService.isAdministrator(getAuthenticatedUser());
    }

    public boolean isEditor() {
        return isAuthenticated() && authorizationService.isEditor(getAuthenticatedUser());
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

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    @Override
    public boolean isBillingManager() {
        return false;
    }
}
