package org.tdar.struts.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.struts.TdarStrutsTestCase;
import org.tdar.struts.action.TdarActionSupport;


public class IndexControllerStrutsITCase extends TdarStrutsTestCase<TdarActionSupport> {
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