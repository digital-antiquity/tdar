package org.tdar.core.service.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * This is effectively our "Queue" for events attached to the current thread. It
 * maintains events until we're ready to complete the transaction and flush them
 * 
 * @author abrin
 *
 * @param <T>
 */
public class EventBusResourceHolder<T extends ObjectContainer<?>> extends ResourceHolderSupport {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, T> pendingMessages = new HashMap<>();

	/**
	 * Adding a message, or ObjectContainer queues the event. To be safe we track the date added in nano-seconds, we dedup items
	 * from the queue based on their ID and the date. The latter item is the one that we keep.
	 * @param message
	 */
	public void addMessage(T message) {
		String id = message.getId();
		if (pendingMessages.containsKey(id)) {
			T existing = pendingMessages.get(id);
			if (existing.getDateAdded() > message.getDateAdded()) {
				logger.debug(" SKIP {} EVENT: {} ({})", message.getType(), message.getEventType(), id);
				return;
			}
		}
		logger.debug("PENDING {} EVENT: {} ({})", message.getType(), message.getEventType(), id);
		pendingMessages.put(id, message);
	}

	protected Collection<T> getPendingMessages() {
		return pendingMessages.values();
	}

}
