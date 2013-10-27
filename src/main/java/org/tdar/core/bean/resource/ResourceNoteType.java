package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource notes.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public enum ResourceNoteType implements HasLabel {
    GENERAL(MessageHelper.getMessage("resourceNoteType.general")),
    REDACTION(MessageHelper.getMessage("resourceNoteType.redaction")),
    RIGHTS_ATTRIBUTION(MessageHelper.getMessage("resourceNoteType.rights")),
    ADMIN(MessageHelper.getMessage("resourceNoteType.admin"));

    private final String label;

    private ResourceNoteType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
