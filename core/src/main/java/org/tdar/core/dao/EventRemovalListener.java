package org.tdar.core.dao;

import java.util.Set;

import org.hibernate.Session;
import org.tdar.core.dao.hibernateEvents.SessionProxy;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class EventRemovalListener implements RemovalListener<Session, Set<?>> {
	SessionProxy EVENT_PROXY = SessionProxy.getInstance();
	
	@Override
	public void onRemoval(RemovalNotification<Session, Set<?>> notification) {
		EVENT_PROXY.registerSessionClose(notification.getKey());
	}

}
