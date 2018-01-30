package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class AdminReportNewUsersMessage extends Email {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3711536317796542612L;

	@Override
	public String createSubjectLine() {
		return MessageHelper.getMessage("EmailType.ADMIN_NEW_USER_REPORT", Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym().toUpperCase(), getMap().get("totalUsers")));
	}

}
