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
public class DataOneScheduledProcessServiceImpl implements DataOneScheduledProcessService {

    @Autowired
    private DataOneService dataOneService;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /* (non-Javadoc)
     * @see org.tdar.dataone.service.DataOneScheduledProcessService#cronChangesAdded()
     */
    @Override
    @Scheduled(fixedDelay = ScheduledProcessService.FIVE_MIN_MS)
    @Transactional(readOnly=false)
    public void cronChangesAdded() {
        logger.debug("running sync...");
        dataOneService.synchronizeTdarChangesWithDataOneObjects();
    }

}
