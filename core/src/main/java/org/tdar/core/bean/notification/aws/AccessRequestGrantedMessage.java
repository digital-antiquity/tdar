package org.tdar.core.bean.notification.aws;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.EmailService;
@Entity
@DiscriminatorValue("ACCESS_GRANTED")
public class AccessRequestGrantedMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6549896844646538119L;

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get(EmailService.RESOURCE2);
		return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
	}

}
