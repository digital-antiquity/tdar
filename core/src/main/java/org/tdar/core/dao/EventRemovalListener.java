package org.tdar.core.dao;

import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.hibernateEvents.SessionProxy;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class EventRemovalListener implements RemovalListener<Session, Set<?>> {
	SessionProxy EVENT_PROXY = SessionProxy.getInstance();
	
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void onRemoval(RemovalNotification<Session, Set<?>> notification) {
	    if (notification.getKey() != null) {
	    	logger.debug("registering event removal via cache");
	        EVENT_PROXY.registerSessionClose(notification.getKey().hashCode(), false);
	    }
	}

}
