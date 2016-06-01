package org.tdar.core.service.event;

public interface TxMessageBus<R extends ObjectContainer<?>> {

	void post(R o) throws Exception;

}
