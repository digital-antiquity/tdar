package org.tdar.balk.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public interface BalkScheduledProcessService {

    void cronUploadTdar();

    void cronPollingStatsQueue();

}