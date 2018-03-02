package org.tdar.core.event;

public class AsyncTdarEvent extends AbstractTdarEvent {

    private static final long serialVersionUID = 8921781919508693983L;

    public AsyncTdarEvent(Object indexable, EventType type) {
        super(indexable, type);
    }

}
