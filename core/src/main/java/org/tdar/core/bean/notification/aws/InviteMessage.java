package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;

public class InviteMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		String properName = ((TdarUser) getMap().get("from")).getProperName();
		String tdar = TdarConfiguration.getInstance().getSiteAcronym();
		return String.format("%s has invited you to %s",properName,tdar);
	}

}
