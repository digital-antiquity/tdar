package org.tdar.struts.result;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This class extends the behavior of FreemarkerResult by adding the ability to set http headers
 * as well as the http status code, similar to HttpHeaderResult
 * @author Jim deVos
 */
public class FreemarkerHttpHeaderResult extends FreemarkerResult {

    public static final int STATUS_OK = 200;

    private static final long serialVersionUID = 195648957144219214L;
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerHttpHeaderResult.class);

    /**
     * This result type doesn't have a default param, null is ok to reduce noice in logs
     */
    public static final String DEFAULT_PARAM = null;

    private boolean parse = true;
    private Map<String, String> headers;
    private int status = -1;

    public FreemarkerHttpHeaderResult() {
        super();
        headers = new HashMap<String, String>();
    }

    public FreemarkerHttpHeaderResult(int status) {
        this();
        this.status = status;
        this.parse = false;
    }

    /**
     * Returns a Map of all HTTP headers.
     *
     * @return a Map of all HTTP headers.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets whether or not the HTTP header values should be evaluated against the ValueStack (by default they are).
     *
     * @param parse <tt>true</tt> if HTTP header values should be evaluated against the ValueStack, <tt>false</tt>
     *              otherwise.
     */
    public void setParse(boolean parse) {
        this.parse = parse;
    }

    /**
     * Sets the http servlet response status code that should be set on a response.
     *
     * @param status the Http status code
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    /**
     * Adds an HTTP header to the response
     *
     * @param name  header name
     * @param value header value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Sets the optional HTTP response status code and also re-sets HTTP headers after they've
     * been optionally evaluated against the ValueStack.
     *
     * @param invocation an encapsulation of the action execution state.
     * @throws Exception if an error occurs when re-setting the headers.
     */
    public void execute(ActionInvocation invocation) throws Exception {
        HttpServletResponse response = ServletActionContext.getResponse();
        ValueStack stack = ActionContext.getContext().getValueStack();

        if (status != -1) {
            response.setStatus(status);
        }

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String value = entry.getValue();
                String finalValue = parse ? TextParseUtil.translateVariables(value, stack) : value;
                response.addHeader(entry.getKey(), finalValue);
            }
        }

    }



}
