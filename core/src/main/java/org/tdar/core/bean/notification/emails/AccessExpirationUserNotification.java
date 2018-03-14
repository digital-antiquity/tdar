package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("ACCESS_EXP_USER")
public class AccessExpirationUserNotification extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1330718162119098391L;
	
	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage(EmailType.ACCESS_EXPIRE_USER_NOTIFICATION.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
	}
}
