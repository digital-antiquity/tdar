package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class InviteAcceptedMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6035465612724342667L;

	@Override
	public String createSubjectLine() {
		return MessageHelper.getInstance().getText("EmailType.INVITE_ACCEPTED", Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
	}
}
