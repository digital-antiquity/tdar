package org.tdar.dataone.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.ScheduledProcessService;

@Service
public class DataOneScheduledProcessService {

    @Autowired
    private DataOneService dataOneService;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

//    @PostConstruct
//    public void postConstruct() {
//        DataOneSynchronizeTask command = new DataOneSynchronizeTask();
//        command.setDataOneService(dataOneService);
//        scheduler.scheduleAtFixedRate(command, 0, 5, TimeUnit.MINUTES);
//    }
    
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    @Transactional(readOnly=false)
    public void cronChangesAdded() {
        logger.debug("running sync...");
        dataOneService.synchronizeTdarChangesWithDataOneObjects();
    }

}
