package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.utils.MessageHelper;

public class TestAwsMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		String firstName = "";
		String lastName  = "";
		
		firstName = (String) getMap().get("firstName");
		lastName  = (String) getMap().get("lastName");
		
		return MessageHelper.getMessage("EmailType."+getEmailType().name(), Arrays.asList(lastName, firstName));
	}
}
