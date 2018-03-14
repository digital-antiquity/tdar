package org.tdar.core.bean.notification.emails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

@Entity
@DiscriminatorValue("ACCESS_REQUEST")
public class AccessRequestCustomMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5737055649043090324L;

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get(EmailKeys.RESOURCE);
		return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
	}

}
