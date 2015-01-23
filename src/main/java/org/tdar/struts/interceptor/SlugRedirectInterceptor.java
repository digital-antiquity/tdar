package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.SlugViewAction;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * A simple interceptor that latches on after prepare and looks for whether we need to redirect the request or not.  This should get the redirects out of execute
 * @author abrin
 *
 */
public class SlugRedirectInterceptor implements Interceptor {

    private static final long serialVersionUID = 4955121652211645909L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    transient GenericService genericService;

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        
        ActionProxy proxy = invocation.getProxy();
        if (proxy.getAction() instanceof SlugViewAction) {
            SlugViewAction sva = (SlugViewAction)proxy.getAction();
            if (sva.isRedirectBadSlug()) {
                HttpServletRequest request = ServletActionContext.getRequest();
                HttpServletResponse response = ServletActionContext.getResponse();
                String url = sva.getPersistable().getDetailUrl();
                if (StringUtils.isNotBlank(request.getQueryString())) {
                    url = String.format("%s?%s", url, request.getQueryString());
                }
                logger.trace("sending redirect from: {} to: {}", request.getRequestURI(), url);
                response.sendRedirect(url);
                return null;
            }
        }

        return invocation.invoke();
    }

}
