package org.tdar.core.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * This is the event class that actually handles processing the events (i.e. the
 * commits). If a transaction is complete, it empties the queue associated with
 * the transaction
 * 
 * @author abrin
 *
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class MessageBusResourceSynchronization<T extends ObjectContainer>
        extends ResourceHolderSynchronization<EventBusResourceHolder, TxMessageBus> {
    private final TxMessageBus messageBus;
    private final EventBusResourceHolder holder;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MessageBusResourceSynchronization(EventBusResourceHolder resourceHolder, TxMessageBus resourceKey) {
        super(resourceHolder, resourceKey);
        this.messageBus = resourceKey;
        this.holder = resourceHolder;
    }

    /**
     * Cleanup.
     */
    @Override
    protected void cleanupResource(EventBusResourceHolder resourceHolder, TxMessageBus resourceKey, boolean committed) {
        resourceHolder.getPendingMessages().clear();
    }

    /**
     * if successful, then we commit the events, otherwise, discard
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterCompletion(int status) {
        logger.trace("completion: {}", status);
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            logger.trace("COMMITTING EVENTS {}", holder.getPendingMessages().size());
            for (Object o : holder.getPendingMessages()) {
                try {
                    messageBus.post((ObjectContainer) o);
                } catch (Exception e) {
                    logger.error("exception in post-transaction procesisng", e);
                }
            }
        }
        // cleanup
        holder.getPendingMessages().clear();
        super.afterCompletion(status);
    }

}
