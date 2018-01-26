package org.tdar.struts.interceptor;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * for controllers that expose a hidden parameter of doubleSubmitKey, check whether that key has been used or not, if it has, 
 * prevent it from being used concurrently with another request.  If we get a double-submit, we return immediately.  If we
 * are successful in processing, we remove the key.  This means that even if INPUT is returned by a form, we can still resubmit.
 *  
 * @author abrin
 *
 */
public class DoubleSubmitInterceptor implements Interceptor {

    static final String _DOUBLE_SUBMIT_KEY = "doubleSubmitKey";

    private static final LinkedBlockingQueue<String> knownKeys = new LinkedBlockingQueue<>(1000);

    private static final long serialVersionUID = 1696177808475933427L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        Parameter parameter = invocation.getInvocationContext().getParameters().get(_DOUBLE_SUBMIT_KEY);
        if (parameter == null || StringUtils.isBlank(parameter.getValue())) {
            return invocation.invoke();
        }
        String key = parameter.getValue();
        if (StringUtils.isNotBlank(key) && knownKeys.contains(key)) {
            logger.warn("double submit prevented");
            return TdarActionSupport.BAD_REQUEST;
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace("adding key:{}", key);
            logger.trace("      keys:{}", knownKeys);
        }

        knownKeys.add(key);
        String result = invocation.invoke();
        knownKeys.remove(key);

        if (logger.isTraceEnabled()) {
            logger.trace("removed key:{}", key);
            logger.trace("       keys:{}", knownKeys);
        }
        return result;
        
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

}
