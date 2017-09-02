package org.tdar.core.bean.notification.aws;

import java.util.Arrays;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

public class InviteMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		String properName = ((TdarUser) getMap().get("from")).getProperName();
		String tdar = TdarConfiguration.getInstance().getSiteAcronym();
		return MessageHelper.getMessage(getSubjectKey(),Arrays.asList(properName,tdar));
	}

}
