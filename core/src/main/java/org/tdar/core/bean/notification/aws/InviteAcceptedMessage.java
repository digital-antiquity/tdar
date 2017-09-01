package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class InviteAcceptedMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		return MessageHelper.getInstance().getText("EmailType."+getEmailType().name(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
	}
}
