package org.tdar.core.bean.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.Pair;

public interface ScheduledProcess<P extends Persistable> extends Serializable {

	/**
	 * FIXME: rename to isEnabled() instead?  Is there a difference?
	 * @return
	 */
    public boolean isConfigured();

    public String getDisplayName();

    public Class<P> getPersistentClass();

    /**
     * Returns a full List of all IDs to be processed via processBatch
     * 
     * @return
     */
    public List<Long> getPersistableIdQueue();

    /**
     * Processes a subset of the full List of persistable entities returned by prepareList() based on {@see TdarConfiguration.getScheduledBatchSize()} Returns a
     * Map, where the keys are ScheduledProcess-specific status codes mapped to Lists of Pairs<persistable ID, label/display name>
     * 
     * @param batch
     * @return
     * @throws Exception
     */
    public void processBatch(List<Long> batch) throws Exception;

    public void process(P persistable) throws Exception;

    public void cleanup();

    boolean isShouldRunOnce();

    public Long getLastId();

    public void setLastId(Long lastId);

    public int getBatchSize();

    public static abstract class Base<Q extends Persistable> implements ScheduledProcess<Q> {

        private static final long serialVersionUID = -8936499060533204646L;

        private transient Long lastId = -1L;

        private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();
        private List<Pair<Q, Throwable>> errors = new ArrayList<Pair<Q, Throwable>>();
        protected final Logger logger = LoggerFactory.getLogger(getClass());
        
        @Autowired
        private EmailService emailService;

        @Autowired
        @Qualifier("genericDao")
        private GenericDao genericDao;

        private List<Long> allIds;

        public boolean isConfigured() {
            return true;
        }

        @Override
        public void cleanup() {
            StringBuilder sb = new StringBuilder();
            for (Pair<Q,Throwable> error : errors) {
                sb.append("\n").append(error.getFirst().toString()).append(" : ").append(error.getSecond());
            }
            if (sb.length() > 0) {
                emailService.send(sb.toString(), String.format("tDAR: %s messages", getDisplayName()), tdarConfiguration.getSystemAdminEmail());
            }
        }

        @Override
        public void processBatch(List<Long> batch) throws Exception {
            for (Q entity : genericDao.findAll(getPersistentClass(), batch)) {
                try {
                    process(entity);
                } catch (Exception exception) {
                    logger.warn("Unable to process entity " + entity, exception);
                    errors.add(new Pair<Q, Throwable>(entity, exception));
                }
                Thread.yield();
            }
        }

        public boolean isShouldRunOnce() {
            return false;
        }

        public Long getLastId() {
            return lastId;
        }

        public void setLastId(Long lastId) {
            this.lastId = lastId;
        }

        public String toString() {
            return getDisplayName();
        }

        public int getBatchSize() {
            return tdarConfiguration.getScheduledProcessBatchSize();
        }

        public TdarConfiguration getTdarConfiguration() {
            return tdarConfiguration;
        }
        
        public List<Long> findAllIds() {
            // FIXME: replace with LinkedList iff we find that performance suffers - ScheduledProcessService uses subList to iterate and clear 
            // batches and so LinkedList may offer better traversal/removal performance at the cost of increased memory usage.
            if(tdarConfiguration.getScheduledProcessStartId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_START_ID &&
                    tdarConfiguration.getScheduledProcessEndId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_END_ID) {
                return genericDao.findAllIds(getPersistentClass());
            } else {
                return genericDao.findAllIds(getPersistentClass(), 
                        tdarConfiguration.getScheduledProcessStartId(), tdarConfiguration.getScheduledProcessEndId());
            }
                
        }

        @Override
        public synchronized List<Long> getPersistableIdQueue() {
            if (allIds == null) {
                allIds = findAllIds(); 
            }
            return allIds;
        }


        public Logger getLogger() {
            return logger;
        }
    }
}