package org.tdar.core.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.web.service.WebScheduledProcessService;

public class WebScheduledProcessITCase {

    @Autowired
    WebScheduledProcessService scheduledProcessService;
    
    @Test
    @Rollback(true)
    public void testAccountUsageHistory() {
        scheduledProcessService.cronUpdateAccountUsageHistory();
    }

}
