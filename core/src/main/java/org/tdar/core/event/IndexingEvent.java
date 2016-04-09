package org.tdar.core.event;

import java.io.Serializable;

import org.tdar.core.bean.Indexable;

public class IndexingEvent implements Serializable {

	private static final long serialVersionUID = -6141090148370581562L;
	private EventType type;
	private Indexable indexable;
	private Long extraId;

	public IndexingEvent(Indexable irFile, EventType type, Long extraId) {
		this(irFile, type);
		this.setExtraId(extraId);
	}

	public IndexingEvent(Indexable indexable, EventType type) {
		this.indexable = indexable;
		this.type = type;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Indexable getIndexable() {
		return indexable;
	}

	public void setIndexable(Indexable indexable) {
		this.indexable = indexable;
	}

	public Long getExtraId() {
		return extraId;
	}

	public void setExtraId(Long extraId) {
		this.extraId = extraId;
	}

}
