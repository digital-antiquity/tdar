package org.tdar.struts.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * This class is not "Crowd" specific, but, it does use Crowd's general method for handling tokens. It looks for a token and attempts to validate it, and then
 * set the user on the session.
 * 
 * @author abrin
 *
 */
public class CrowdTokenInterceptor extends AbstractAuthenticationInterceptor {

    private static final long serialVersionUID = -5229488146705458709L;

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init() {
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        // get the token name
        Object[] token = (Object[]) ActionContext.getContext().getParameters().get(CONFIG.getRequestTokenName());
        if (ArrayUtils.isEmpty(token)) {
            HttpServletRequest request = ServletActionContext.getRequest();
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(CONFIG.getRequestTokenName())) {
                    token = new Object[1];
                    token[0] = c.getValue();
                }
            }
        }
        // if not authenticated, check for the token, then validate that
        if (!getSessionData().isAuthenticated()) {
            validateSsoTokenAndAttachUser(token);
        }
        return invocation.invoke();
    }
}
