package org.tdar.core.event;

/**
 * The "Type" of event we're handling
 **/
public enum EventType {
    DELETE,
    CREATE_OR_UPDATE,
    REINDEX_CHILDREN;
}
