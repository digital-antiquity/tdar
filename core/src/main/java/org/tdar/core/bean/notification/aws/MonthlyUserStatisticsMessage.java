package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.notification.Email;
import org.tdar.utils.MessageHelper;

public class MonthlyUserStatisticsMessage extends Email {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8514278566758205698L;

	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage("EmailType.MONTHLY_USER_STATISTICS");
	}
}
