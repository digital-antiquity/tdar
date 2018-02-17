package org.tdar.core.bean.notification.aws;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.EmailService;
@Entity
@DiscriminatorValue("ACCESS_REJECTED")
public class AccessRequestRejectedMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8410688051017443576L;

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get(EmailService.RESOURCE2);
		return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
	}

}
