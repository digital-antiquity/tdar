package org.tdar.core.bean.notification.emails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;

@Entity
@DiscriminatorValue("BASIC")
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
