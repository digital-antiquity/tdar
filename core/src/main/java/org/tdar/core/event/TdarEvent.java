package org.tdar.core.event;

import java.io.Serializable;

/**
 * A basic tDAR Event object. It maintains a reference to the object that needs to be processed, the Type of event, and an extra ID
 * if needed.  This is useful for InformationResourceFiles to track the parent (InformationResource ID)
 * @author abrin
 *
 */
public class TdarEvent extends AbstractTdarEvent implements Serializable {

    private static final long serialVersionUID = -6141090148370581562L;
    public TdarEvent(Object irFile, EventType type, Long extraId) {
        super(irFile,type,extraId);
    }

    public TdarEvent(Object indexable, EventType type) {
        super(indexable, type);
    }

}
