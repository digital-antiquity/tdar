package org.tdar.core.dao;

import org.hibernate.SessionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilestoreLoggingSessionEventListener implements SessionEventListener {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void transactionCompletion(boolean successful) {
        logger.debug("   transaction completed: {}", successful);

    }

    @Override
    public void jdbcConnectionAcquisitionStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcConnectionAcquisitionEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcConnectionReleaseStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcConnectionReleaseEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcPrepareStatementStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcPrepareStatementEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcExecuteStatementStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcExecuteStatementEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcExecuteBatchStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jdbcExecuteBatchEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cachePutStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cachePutEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cacheGetStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cacheGetEnd(boolean hit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void flushStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void flushEnd(int numberOfEntities, int numberOfCollections) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partialFlushStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void partialFlushEnd(int numberOfEntities, int numberOfCollections) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dirtyCalculationStart() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dirtyCalculationEnd(boolean dirty) {
        // TODO Auto-generated method stub

    }

    @Override
    public void end() {
        // TODO Auto-generated method stub
logger.debug("session end");
    }

}
