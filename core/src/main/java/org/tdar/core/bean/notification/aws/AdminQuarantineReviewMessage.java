package org.tdar.core.bean.notification.aws;

public class AdminQuarantineReviewMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return String.format("There are %s user emails to review ", getMap().get("totalEmails"));
	}

}
