package org.tdar.core.event;

import java.io.Serializable;

public abstract class AbstractTdarEvent implements Serializable {

    private static final long serialVersionUID = -7606016589495469926L;
    protected EventType type;
    protected Object indexable;
    private Long extraId;

    public AbstractTdarEvent(Object indexable, EventType type) {
        this.indexable = indexable;
        this.type = type;
    }

    public AbstractTdarEvent(Object irFile, EventType type, Long extraId) {
        this(irFile, type);
        this.setExtraId(extraId);
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Object getRecord() {
        return indexable;
    }

    public void setIndexable(Object indexable) {
        this.indexable = indexable;
    }

    public Long getExtraId() {
        return extraId;
    }

    public void setExtraId(Long extraId) {
        this.extraId = extraId;
    }

}