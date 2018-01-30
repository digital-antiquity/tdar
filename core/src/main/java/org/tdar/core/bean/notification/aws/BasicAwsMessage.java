package org.tdar.core.bean.notification.aws;

import org.tdar.core.bean.notification.Email;

public class BasicAwsMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = -152769506900833762L;

	@Override
	public String createSubjectLine() {
		return this.getSubject();
	}

}
