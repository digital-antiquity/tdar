package org.tdar.balk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BalkScheduledProcessService {

    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronPollingQueue() {
        logger.debug("scheudled");
    }

}
