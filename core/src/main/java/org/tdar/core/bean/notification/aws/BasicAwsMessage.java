package org.tdar.core.bean.notification.aws;

public class BasicAwsMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		return this.getEmail().getSubject();
	}

}
