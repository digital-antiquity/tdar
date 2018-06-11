package org.tdar.core.bean.notification.emails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("RES_EXPORT_REQ")
public class ResourceExportRequestMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 264784414649423978L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.RESOURCE_EXPORT.getLocaleKey());
    }
}
