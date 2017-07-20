package org.tdar.dataone.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.ScheduledProcessService;

public interface DataOneScheduledProcessService {

    void cronChangesAdded();

}