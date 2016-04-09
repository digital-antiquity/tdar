package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.Pair;

/**
 * Abstract class to help with batch processes, track errors, and managing the batches.
 * 
 * @author abrin
 * 
 * @param <P>
 */
public abstract class AbstractScheduledBatchProcess<P extends Persistable> extends AbstractPersistableScheduledProcess<P> {

    private static final long serialVersionUID = -334767596935956563L;

    protected final List<Pair<P, Throwable>> errors = new ArrayList<>();

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
    public synchronized final void cleanup() {
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
                Email email = new Email();
                email.setSubject(String.format("%s: %s messages", getTdarConfiguration().getSiteAcronym(), getDisplayName()));
                email.setMessage(sb.toString());
                emailService.send(email);
            } catch (Exception e) {
                logger.error("could not send email:{}\n\n{}", sb.toString(), e);
            }
        }
        setAllIds(null);
        batchCleanup();
    }

    /**
     * Override this method for any custom cleanup that needs to occur after cleanup().
     * 
     */
    protected void batchCleanup() {

    }

    @Override
    public synchronized void execute() {
        List<Long> batch = getNextBatch();
        while (CollectionUtils.isNotEmpty(batch)) {
            processBatch(batch);
            getBatchIdQueue().removeAll(batch);
            batch = getNextBatch();
        }
    }
    
    public boolean clearBeforeBatch() {
    	return false;
    }

    public synchronized List<Long> getNextBatch() {
    	if (clearBeforeBatch()) {
			genericDao.clearCurrentSession();
    	}
        List<Long> queue = getBatchIdQueue();
        if (queue.isEmpty()) {
            logger.trace("No more ids to process");
            return Collections.emptyList();
        }
        int endIndex = Math.min(queue.size(), getBatchSize());
        List<Long> sublist = queue.subList(0, endIndex);
        // make a copy of the batch first
        ArrayList<Long> batch = new ArrayList<>(sublist);
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
        if (batch.isEmpty()) {
            return;
        }
        for (P entity : genericDao.findAll(getPersistentClass(), batch)) {
            try {
                process(entity);
            } catch (Throwable exception) {
                logger.warn("Unable to process entity " + entity, exception);
                errors.add(new Pair<>(entity, exception));
            }
            Thread.yield();
        }
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    public int getBatchSize() {
        return getTdarConfiguration().getScheduledProcessBatchSize();
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
        if ((getTdarConfiguration().getScheduledProcessStartId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_START_ID)
                && (getTdarConfiguration().getScheduledProcessEndId() == TdarConfiguration.DEFAULT_SCHEDULED_PROCESS_END_ID)) {
            return genericDao.findAllIds(getPersistentClass());
        }
        else {
            return genericDao.findAllIds(getPersistentClass(),
                    getTdarConfiguration().getScheduledProcessStartId(), getTdarConfiguration().getScheduledProcessEndId());
        }
    }

    public synchronized List<Long> getBatchIdQueue() {
        if (getAllIds() == null) {
            setAllIds(findAllIds());
            logger.debug("{} ids in queue", CollectionUtils.size(getAllIds()));
        }
        return getAllIds();
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

    public List<Long> getAllIds() {
        return allIds;
    }

    public void setAllIds(List<Long> allIds) {
        this.allIds = allIds;
    }
}
