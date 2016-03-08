package org.tdar.struts.action;

import com.opensymphony.xwork2.ActionProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;
import org.tdar.struts.TdarStrutsTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;


@ContextConfiguration(classes = TdarBaseWebAppConfiguration.class)
@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
public class IndexControllerITCase extends TdarStrutsTestCase<TdarActionSupport> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testRobots() throws Exception {
        executeAction("/robots");
        logger.debug(response.getContentAsString());
        assertThat(getAction().getActionErrors(), is( empty()));
    }

    @Test
    public void testHome() throws Exception {
        executeAction("/");
        logger.debug(response.getContentAsString());
        assertThat(getAction().getActionErrors(), is( empty()));
    }

}