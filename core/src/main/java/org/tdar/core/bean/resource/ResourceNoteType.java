package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource notes.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public enum ResourceNoteType implements HasLabel, Localizable {
    GENERAL("General Note"), REDACTION("Redaction Note"), RIGHTS_ATTRIBUTION("Rights & Attribution"), ADMIN("Administration Note");

    private final String label;

    private ResourceNoteType(String label) {
        this.label = label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

}
