package org.tdar.core.bean.notification.aws;

import org.tdar.utils.MessageHelper;

public class MonthlyUserStatisticsMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage("EmailType.MONTHLY_USER_STATISTICS");
	}
}
