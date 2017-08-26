package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;

public class InviteAcceptedMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		return TdarConfiguration.getInstance().getSiteAcronym() + ": invite accepted";
	}
}
