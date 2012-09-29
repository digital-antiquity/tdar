package org.tdar.core.bean.resource;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource notes.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public enum ResourceNoteType { 
    GENERAL("General Note"),
    REDACTION("Redaction Note"),
    RIGHTS_ATTRIBUTION("Rights & Attribution"),
    ADMIN("Administration Note");
    
    private final String label;
    
    private ResourceNoteType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }

}
