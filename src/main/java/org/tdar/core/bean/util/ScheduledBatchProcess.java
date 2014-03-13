package org.tdar.core.bean.util;

import java.util.ArrayList;
import java.util.Collections;
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

import com.mchange.v2.util.CollectionUtils;

/**
 * Abstract class to help with batch processes, track errors, and managing the batches.
 * 
 * @author abrin
 *
 * @param <P>
 */
public abstract class ScheduledBatchProcess<P extends Persistable> extends ScheduledProcess.Base<P> {

    private static final long serialVersionUID = -8936499060533204646L;

    private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();
    protected final List<Pair<P, Throwable>> errors = new ArrayList<Pair<P, Throwable>>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    // this seems really weird to have @Autowired fields in beans...
    private EmailService emailService;

    @Autowired
    @Qualifier("genericDao")
    // this seems really weird to have @Autowired fields in beans...
    protected GenericDao genericDao;

    private List<Long> allIds;

    public abstract void process(P persistable) throws Exception;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public final void cleanup() {
        if (!isCompleted()) {
            logger.warn("Trying to cleanup {} which hasn't run to completion yet (remaining ids: {}), ignoring.",
                    this, getBatchIdQueue());
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Pair<P, Throwable> error : errors) {
            sb.append("\n").append(error.getFirst().toString()).append(" : ").append(error.getSecond());
        }
        if (sb.length() > 0) {
            try {
                emailService.send(sb.toString(),
                        String.format("%s: %s messages", tdarConfiguration.getSiteAcronym(), getDisplayName()));
            } catch (Exception e) {
                logger.error("could not send email:{}\n\n{}", sb.toString(), e);
            }
        }
        allIds = null;
        batchCleanup();
    }

    /**
     * Override this method for any custom cleanup that needs to occur after cleanup().
     * 
     */
    protected void batchCleanup() {

    }

    @Override
    public void execute() {
        processBatch(getNextBatch());
    }

    public List<Long> getNextBatch() {
        List<Long> queue = getBatchIdQueue();
        if (queue.isEmpty()) {
            logger.trace("No more ids to process");
            return Collections.emptyList();
        }
        int endIndex = Math.min(queue.size(), getBatchSize());
        List<Long> sublist = queue.subList(0, endIndex);
        // make a copy of the batch first
        ArrayList<Long> batch = new ArrayList<Long>(sublist);
        // sublist is a view of the backing list, clearing it modifies the
        // backing list.
        sublist.clear();
        if (!batch.isEmpty()) {
            setLastId(batch.get(endIndex - 1));
        }
        logger.trace("batch {}", batch);
        return batch;
    }

    public void processBatch(List<Long> batch) {
        if (batch.isEmpty())
            return;
        for (P entity : genericDao.findAll(getPersistentClass(), batch)) {
            try {
                process(entity);
            } catch (Throwable exception) {
                logger.warn("Unable to process entity " + entity, exception);
                errors.add(new Pair<P, Throwable>(entity, exception));
            }
            Thread.yield();
        }
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    public int getBatchSize() {
        return tdarConfiguration.getScheduledProcessBatchSize();
    }

    /**
     * Subtypes should override this to implement whatever custom logic they
     * need to locate ids for the entities the batch processor should be
     * operating over.
     * 
     * @return
     */
    public List<Long> findAllIds() {
        // FIXME: replace with LinkedList iff we find that performance suffers -
        // we use subList to iterate and clear
        // batches and so LinkedList may offer better traversal/removal
        // performance at the cost of increased memory usage.
        if (tdarConfiguration.getScheduledProcessStartId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_START_ID &&
                tdarConfiguration.getScheduledProcessEndId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_END_ID) {
            return genericDao.findAllIds(getPersistentClass());
        } else {
            return genericDao.findAllIds(getPersistentClass(),
                    tdarConfiguration.getScheduledProcessStartId(), tdarConfiguration.getScheduledProcessEndId());
        }
    }

    public synchronized List<Long> getBatchIdQueue() {
        if (allIds == null) {
            allIds = findAllIds();
            Collections.sort(allIds);
            logger.debug("{} ids in queue", CollectionUtils.size(allIds) );
        }
        return allIds;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns true if the batch id queue is empty.
     * NOTE: Invoking ScheduledBatchProcess.cleanup() sets the batch id queue to null,
     * which will cause any subsequent invocations of this method to return false.
     */
    @Override
    public boolean isCompleted() {
        return getBatchIdQueue().isEmpty();
    }
}
