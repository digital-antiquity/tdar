package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

public class AccessRequestCustomMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5737055649043090324L;

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get("resource");
		return TdarConfiguration.getInstance().getSiteAcronym() + ": " + resource.getTitle();
	}

}
