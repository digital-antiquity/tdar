package org.tdar.core.bean.notification.emails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("ADMIN_OVRDRW_NOTFY")
public class AdminOverdrawnNotification extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6728687907398205464L;

	
	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage(EmailType.ADMIN_OVERDRAWN_NOTIFICATION.getLocaleKey());
	}
	
}
