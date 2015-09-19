package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;

import org.apache.struts2.StrutsSpringJUnit4TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.web.TdarWebAppConfiguration;

import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;

@ContextConfiguration(classes = TdarWebAppConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@Ignore
public class IndexControllerITCase extends StrutsSpringJUnit4TestCase<HomepageSupportingController> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() throws Exception {
        ActionProxy proxy = getActionProxy("/robots");
        HomepageSupportingController myAction = (HomepageSupportingController) proxy.getAction();
        logger.debug(executeAction("/robots"));
        String execute = myAction.execute();
        finishExecution();
        logger.debug(response.getContentType());
        logger.debug(response.getContentAsString());
        assertEquals("Error", ActionSupport.SUCCESS, execute);


    }
    
}