package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource notes.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public enum ResourceNoteType implements HasLabel { 
    GENERAL("General Note"),
    REDACTION("Redaction Note"),
    RIGHTS_ATTRIBUTION("Rights & Attribution"),
    ADMIN("Administration Note");
    
    private final String label;

    private ResourceNoteType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
