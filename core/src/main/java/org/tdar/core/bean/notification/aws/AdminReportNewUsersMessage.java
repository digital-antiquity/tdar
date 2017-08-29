package org.tdar.core.bean.notification.aws;

import org.tdar.core.configuration.TdarConfiguration;

public class AdminReportNewUsersMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return String.format("%s New User Report: %s new users", TdarConfiguration.getInstance().getSiteAcronym().toUpperCase(), getMap().get("totalUsers"));
	}

}
