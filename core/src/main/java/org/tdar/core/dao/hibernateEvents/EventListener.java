package org.tdar.core.dao.hibernateEvents;

public interface EventListener {

    void flush(Integer sessionId);

	void clear(Integer sessionId);

}
