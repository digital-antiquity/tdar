package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;

@ContextConfiguration(classes = TdarBaseWebAppConfiguration.class)
@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, classMode = ClassMode.AFTER_CLASS)
public class CronScheduledTaskITCase extends AbstractIntegrationTestCase {

    @Autowired
    ScheduledProcessService scheduledProcessService;

    @Test
    // @Ignore("still haven't figured out how to access task registrar")
    public void testCronList() {
        List<String> cronEntries = scheduledProcessService.getCronEntries();
        assertThat(cronEntries, is(not(nullValue())));
        assertThat(cronEntries, is(not(empty())));
    }

}
