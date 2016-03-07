package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.AbstractIntegrationWebTestCase;

public class CronScheduledTaskITCase extends AbstractIntegrationWebTestCase {

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
