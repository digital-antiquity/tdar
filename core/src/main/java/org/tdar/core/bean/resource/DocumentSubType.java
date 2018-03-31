package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Controlled vocabulary for document types.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum DocumentSubType implements HasLabel, Localizable {

    PAPER(DocumentType.CONFERENCE_PRESENTATION),
    POSTER(DocumentType.CONFERENCE_PRESENTATION),
    SYMPOSIUM(
            DocumentType.CONFERENCE_PRESENTATION),
    ELECTRONIC_SYMPOSIUM(DocumentType.CONFERENCE_PRESENTATION),
    FORUM(
            DocumentType.CONFERENCE_PRESENTATION),
    DEBATE(DocumentType.CONFERENCE_PRESENTATION),
    POSTER_SYMPOSIUM(DocumentType.CONFERENCE_PRESENTATION);

    private DocumentType type;

    private DocumentSubType(DocumentType type) {
        this.setType(type);
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

}
