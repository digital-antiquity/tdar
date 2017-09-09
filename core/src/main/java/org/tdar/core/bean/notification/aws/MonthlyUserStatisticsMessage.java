package org.tdar.core.bean.notification.aws;

import java.util.Arrays;
import java.util.Set;

import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class MonthlyUserStatisticsMessage extends AwsMessage {
	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage("EmailType.MONTHLY_USER_STATISTICS");
	}
}
