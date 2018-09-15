package org.tdar.search.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.processes.upgradeTasks.PartialReindexProjectTitleProcess;
import org.tdar.search.service.processes.weekly.WeeklyResourcesAdded;

public class SearchScheduledProcessITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    private transient SearchScheduledProcessService sps;

    @Autowired
    private transient ScheduledProcessService scheduledProcessService;

    @Autowired
    DailyEmailProcess dailyEmailProcess;

    @Test
    @Rollback
    public void testResourceReport() throws SearchException, SearchIndexException, IOException {
        Dataset dataset = createAndSaveNewDataset();
        searchIndexService.index(dataset);
        scheduledProcessService.queue(WeeklyResourcesAdded.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        assertTrue(dailyEmailProcess.isCompleted());
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

    }

    @Test
    @Rollback
    public void testUpgradeTask() {
        ScheduledProcess process = applicationContext.getBean(PartialReindexProjectTitleProcess.class);
        LinkedHashSet<ScheduledProcess> tasks = scheduledProcessService.getManager().getUpgradeTasks();
        tasks.clear();
        tasks.add(process);
        List<String> runUpgradeTasks = scheduledProcessService.runUpgradeTasks();
        assertTrue(runUpgradeTasks.contains(process.getDisplayName()));
        scheduledProcessService.hasRun(process.getDisplayName());
        process = applicationContext.getBean(PartialReindexProjectTitleProcess.class);
        tasks.clear();
        tasks.add(process);
        runUpgradeTasks = scheduledProcessService.runUpgradeTasks();
        assertFalse(runUpgradeTasks.contains(process.getDisplayName()));

    }

    @Test
    @Rollback(true)
    public void testWeeklyStats() {
        sps.cronWeeklyAdded();
    }

}
