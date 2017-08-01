package org.tdar.core.bean.notification.aws;

public class TestAwsMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		String firstName = "";
		String lastName = "";
		
		firstName = (String) getMap().get("firstName");
		lastName  = (String) getMap().get("lastName");
		
		return String.format(getEmailType().getSubject(), lastName, firstName);
	}

}
