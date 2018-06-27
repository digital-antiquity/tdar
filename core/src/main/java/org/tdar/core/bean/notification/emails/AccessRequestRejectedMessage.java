package org.tdar.core.bean.notification.emails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;

@Entity
@DiscriminatorValue("ACCESS_REJECTED")
public class AccessRequestRejectedMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -8410688051017443576L;

    @Override
    public String createSubjectLine() {
        Resource resource = (Resource) getMap().get(EmailKeys.RESOURCE);
        return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
    }

}
