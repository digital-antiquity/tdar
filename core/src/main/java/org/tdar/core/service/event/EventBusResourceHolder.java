package org.tdar.core.service.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSupport;

public class EventBusResourceHolder<T extends ObjectContainer> extends ResourceHolderSupport {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String,T> pendingMessages = new HashMap<>();

	public void addMessage(T message) {
		String id = message.getId();
		if (pendingMessages.containsKey(id)) {
			T existing = pendingMessages.get(id);
			if (existing.getDateAdded() > message.getDateAdded()) {
				logger.debug(" SKIP {} EVENT: {} ({})", message.getType(), message.getEventType(), id);
				return;
			}
		}
		logger.debug("{} EVENT: {} ({})", message.getType(), message.getEventType(), id);
		pendingMessages.put(id, message);
	}

	protected Collection<T> getPendingMessages() {
		return pendingMessages.values();
	}

}
