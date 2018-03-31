package org.tdar.core.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.AbstractIntegrationWebTestCase;
import org.tdar.web.service.WebScheduledProcessService;

public class WebScheduledProcessITCase extends AbstractIntegrationWebTestCase {

    @Autowired
    WebScheduledProcessService scheduledProcessService;

    @Test
    @Rollback(true)
    public void testAccountUsageHistory() {
        scheduledProcessService.cronUpdateAccountUsageHistory();
    }

}
