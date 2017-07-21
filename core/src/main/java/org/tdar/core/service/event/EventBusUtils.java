package org.tdar.core.service.event;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class is our way of checking whether there's a transaction running, and
 * if there is, queuing up the events, creating a queue, or simply processing
 * the event
 * 
 * Borrowed heavily from: http://stackoverflow.com/a/15116390/667818
 * 
 * @author abrin
 *
 */
public class EventBusUtils {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(EventBusUtils.class);

    /**
     * Gets an Event Queue or processes the event depending on whether there's a
     * transaction running or not.
     *
     * @param messageBus
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Optional<EventBusResourceHolder> getTransactionalResourceHolder(TxMessageBus messageBus) {

        // if there's no transaction, return
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return Optional.empty();
        }

        // if we have a transaction already, get the version of the
        // ResourceHolder (queue) that's bound to this transaction / thread
        EventBusResourceHolder o = (EventBusResourceHolder) TransactionSynchronizationManager.getResource(messageBus);
        if (o != null) {
            return Optional.of(o);
        }
        // otherwise, create a new one
        o = new EventBusResourceHolder();
        TransactionSynchronizationManager.bindResource(messageBus, o);
        o.setSynchronizedWithTransaction(true);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager
                    .registerSynchronization(new MessageBusResourceSynchronization(o, messageBus));
        }
        return Optional.of(o);

    }
}
