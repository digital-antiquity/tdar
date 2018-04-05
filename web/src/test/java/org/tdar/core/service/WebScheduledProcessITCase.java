package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.AbstractIntegrationWebTestCase;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.processes.weekly.WeeklyResourcesAdded;
import org.tdar.web.service.WebScheduledProcessService;

public class WebScheduledProcessITCase extends AbstractIntegrationWebTestCase {

    @Autowired
    WebScheduledProcessService webScheduledProcessService;

    @Autowired
    ScheduledProcessService scheduledProcessService;

    @Autowired
    SearchIndexService searchIndexService;
    
    @Test
    @Rollback(true)
    public void testAccountUsageHistory() {
        webScheduledProcessService.cronUpdateAccountUsageHistory();
    }


    @Test
    @Rollback
    public void testNewResources() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        Dataset dataset = createAndSaveNewDataset();
        dataset.setDateCreated(DateTime.now().minusDays(2).toDate());
        dataset.setDateUpdated(DateTime.now().minusDays(2).toDate());
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        searchIndexService.index(dataset);

        scheduledProcessService.queue(WeeklyResourcesAdded.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.getManager().reset();
        scheduledProcessService.getManager().addProcess(SendEmailProcess.class);
        Email received = checkMailAndGetLatest("The following resources were added to");
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo(), getTdarConfiguration().getSystemAdminEmail());
    }
}
