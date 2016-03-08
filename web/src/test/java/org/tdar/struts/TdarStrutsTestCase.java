package org.tdar.struts;

import org.apache.struts2.StrutsSpringJUnit4TestCase;
import org.springframework.mock.web.*;

import javax.servlet.ServletException;
import java.io.UnsupportedEncodingException;


/**
 * This class mostly serves as a workaround for issues that arise when trying to use the StrutsSpringJUnit4TestCase
 * in Struts applications that use the Convention Plugin.
 */
public class TdarStrutsTestCase<T> extends StrutsSpringJUnit4TestCase<T> {
    @Override
    protected void initServletMockObjects() {
        servletContext = new MockServletContext(applicationContext);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        pageContext = new MockPageContext(servletContext, request, response);
    }

    /**
     * //FIXME: this is a workaround for TDAR-5167, which causes an action to fail when request method is not set
     */
    protected String executeAction(String url) throws ServletException, UnsupportedEncodingException {
        return executeAction(url, "GET");
    }

    /**
     * Request execute an action while simulating the specified HTTP method
     * @param url url of action to execute.
     * @param method uppercased http method name, e.g. GET, POST, HEAD, OPTIONS, PUT, DELETE
     * @return
     * @throws ServletException
     * @throws UnsupportedEncodingException
     */
    private String executeAction(String url, String method) throws ServletException, UnsupportedEncodingException{
        request.setMethod(method);
        return super.executeAction(url);
    }


}
