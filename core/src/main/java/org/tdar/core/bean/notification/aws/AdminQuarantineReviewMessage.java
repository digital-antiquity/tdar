package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.bean.notification.Email;
import org.tdar.utils.MessageHelper;

public class AdminQuarantineReviewMessage extends Email {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2389674712822574793L;

	@Override
	public String createSubjectLine() {
		//return String.format(getEmailType().getSubject(), getMap().get("totalEmails"));
		return MessageHelper.getMessage("EmailType.ADMIN_QUARANTINE_REVIEW", Arrays.asList(getMap().get("totalEmails")));
	}

}
