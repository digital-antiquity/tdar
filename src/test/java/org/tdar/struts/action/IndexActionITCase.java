package org.tdar.struts.action;

import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.annotations.Before;
import org.apache.struts2.StrutsSpringJUnit4TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.GenericService;


import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/testApplicationContext-tdar.xml"})
public class IndexActionITCase extends StrutsSpringJUnit4TestCase {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testAboutMapping() throws Exception {
        ActionProxy actionProxy = getActionProxy("/about");
        assertNotNull(actionProxy);
        logger.debug("test:{}", actionProxy);
   }

    @Ignore
    @Test
    public void testAboutExecute() throws Exception {
        ActionProxy actionProxy = getActionProxy("/about");

        //FIXME: the next line throws exception if about.ftl is not on the classpath
        String result = actionProxy.execute();
        assertThat(result, is(ActionSupport.SUCCESS));
    }
}
