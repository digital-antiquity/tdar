package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

public class AccessRequestRejectedMessage extends AwsMessage {

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get("resource");
		return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
	}

}
