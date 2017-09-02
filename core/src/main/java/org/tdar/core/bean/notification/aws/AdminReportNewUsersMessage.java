package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class AdminReportNewUsersMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage(getSubjectKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym().toUpperCase(), getMap().get("totalUsers")));
	}

}
