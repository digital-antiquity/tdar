package org.tdar.core.service.event;

import java.util.Date;

import org.tdar.core.event.EventType;

public abstract class ObjectContainer<T> {

	String id;
	T doc;
	Date dateAdded = new Date();
	private EventType eventType;


	public ObjectContainer(T doc2, String generateId, EventType eventType) {
		this.doc = doc2;
		this.id = generateId;
		this.setEventType(eventType);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public T getDoc() {
		return doc;
	}

	public void setDoc(T doc) {
		this.doc = doc;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public abstract String getType();
}
