package org.tdar.core.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronization;

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

	@Override
	protected void cleanupResource(EventBusResourceHolder resourceHolder, TxMessageBus resourceKey, boolean committed) {
		resourceHolder.getPendingMessages().clear();
	}

	@Override
	public void afterCompletion(int status) {
        logger.trace("completion: {}", status);
		if (status == TransactionSynchronization.STATUS_COMMITTED) {
		    logger.debug("COMMITTING EVENTS {}", holder.getPendingMessages().size());
			for (Object o : holder.getPendingMessages()) {
				try {
				    messageBus.post((ObjectContainer) o);
				} catch (Exception e) {
					logger.error("exception in post-transaction procesisng", e);
				}
			}
		} else {
			holder.getPendingMessages().clear();
		}
		super.afterCompletion(status);
	}

}
