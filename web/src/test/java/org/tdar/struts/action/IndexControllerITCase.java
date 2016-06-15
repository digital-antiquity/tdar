package org.tdar.struts.action;

import com.opensymphony.xwork2.*;
import org.apache.struts2.StrutsSpringJUnit4TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;

import javax.servlet.ServletException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


@ContextConfiguration(classes = TdarBaseWebAppConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
public class IndexControllerITCase extends StrutsSpringJUnit4TestCase<TdarActionSupport> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() throws Exception {
        ActionProxy proxy = getActionProxy("/robots");
        logger.debug(executeAction("/robots"));
        logger.debug(response.getContentType());
        logger.debug(response.getContentAsString());
    }

    @Test
    public void testHome() throws Exception {
        executeAction("/");
        logger.debug(response.getContentAsString());
        logger.debug("{}", getAction().getActionErrors());
    }

    @Override
    protected void initServletMockObjects() {
        logger.debug("initServletMockObjects");
        servletContext = new MockServletContext(applicationContext);
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        pageContext = new MockPageContext(servletContext, request, response);
    }

    @Override
    public  void setApplicationContext(ApplicationContext ac) {
        logger.debug("setting application context:{}", ac);
        super.setApplicationContext(ac);
    }

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