package org.tdar.core.dao.hibernateEvents;

import org.hibernate.Session;

public interface EventListener {

    void flush(Integer sessionId);

}
