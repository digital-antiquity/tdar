package org.tdar.struts.action;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.external.session.SessionDataAware;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;

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

    public static final String TYPE_REDIRECT = "redirect";

    @DoNotObfuscate(reason = "never obfuscate the session user")
    TdarUser getAuthenticatedUser();

    boolean isAuthenticated();

    boolean isBillingManager();

}
