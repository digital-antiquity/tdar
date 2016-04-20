package org.tdar.core.service.event;

import java.util.Date;

import org.tdar.core.event.EventType;

public abstract class ObjectContainer<T> {

	String id;
	T doc;
	Long dateAdded = System.nanoTime();
	private EventType eventType;


	public ObjectContainer(T doc, String generateId, EventType eventType) {
		this.doc = doc;
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

	public Long getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Long dateAdded) {
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
