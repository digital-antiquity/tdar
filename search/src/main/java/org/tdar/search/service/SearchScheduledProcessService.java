package org.tdar.search.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.processes.CreatorAnalysisProcess;
import org.tdar.search.service.processes.weekly.WeeklyResourcesAdded;

@Service
public class SearchScheduledProcessService {

    @Autowired
    private ScheduledProcessService scheduledProcessService;

    @Autowired
    private SearchIndexService searchIndexService;

    @Scheduled(cron = "12 0 0 * * THU")
    public void cronWeeklyAdded() {
        scheduledProcessService.queue(WeeklyResourcesAdded.class);
    }

    /**
     * Once a week, on Sundays, generate some static, cached stats for use by
     * the admin area and general system
     */
    @Scheduled(cron = "12 0 0 * * SUN")
    public void cronGenerateWeeklyStats() {
        scheduledProcessService.queue(CreatorAnalysisProcess.class);
    }

    
    @Scheduled(cron = "12 0 0 * * SUN")
    public void optimize() {
        searchIndexService.optimizeAll();;
    }

}
