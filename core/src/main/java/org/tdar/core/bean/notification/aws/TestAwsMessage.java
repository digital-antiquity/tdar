package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("TEST_MESSAGE")
public class TestAwsMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6057420297607199621L;

	@Override
	public String createSubjectLine() {
		String firstName = "";
		String lastName  = "";
		
		firstName = (String) getMap().get("firstName");
		lastName  = (String) getMap().get("lastName");
		
		return MessageHelper.getMessage("EmailType.TEST_EMAIL", Arrays.asList(lastName, firstName));
	}
}
