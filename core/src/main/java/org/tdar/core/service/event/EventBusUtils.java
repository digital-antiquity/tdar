package org.tdar.core.service.event;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * http://stackoverflow.com/a/15116390/667818
 * @author abrin
 *
 */
public class EventBusUtils {
	private static final Logger logger = LoggerFactory.getLogger(EventBusUtils.class);

	public static Optional<EventBusResourceHolder> getTransactionalResourceHolder(TxMessageBus messageBus) {

		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return Optional.empty();
		}

		EventBusResourceHolder o = (EventBusResourceHolder) TransactionSynchronizationManager.getResource(messageBus);
		if (o != null)
			return Optional.of(o);

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
