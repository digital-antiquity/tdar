package org.tdar.dataone.service;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataOneSynchronizeTask implements Runnable {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private DataOneService dataOneService;

    @Override
    public void run() {
        logger.debug("running sync... {}", dataOneService);
        dataOneService.synchronizeTdarChangesWithDataOneObjects();
        logger.debug("completed sync... {}", dataOneService);
    }

    public DataOneService getDataOneService() {
        return dataOneService;
    }

    public void setDataOneService(DataOneService dataOneService) {
        this.dataOneService = dataOneService;
    }

}
