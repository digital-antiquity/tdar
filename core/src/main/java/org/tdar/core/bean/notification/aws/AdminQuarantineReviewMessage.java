package org.tdar.core.bean.notification.aws;

public class AdminQuarantineReviewMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return String.format(getEmailType().getSubject(), getMap().get("totalEmails"));
	}

}
