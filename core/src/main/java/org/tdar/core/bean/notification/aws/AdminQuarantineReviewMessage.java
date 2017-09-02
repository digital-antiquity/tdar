package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.utils.MessageHelper;

public class AdminQuarantineReviewMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		//return String.format(getEmailType().getSubject(), getMap().get("totalEmails"));
		return MessageHelper.getMessage(getSubjectKey(), Arrays.asList(getMap().get("totalEmails")));
	}

}
