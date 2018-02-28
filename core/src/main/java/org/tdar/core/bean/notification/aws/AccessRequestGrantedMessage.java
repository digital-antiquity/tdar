package org.tdar.core.bean.notification.aws;

import java.util.Date;
import java.text.SimpleDateFormat;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.EmailService;
@Entity
@DiscriminatorValue("ACCESS_GRANTED")
public class AccessRequestGrantedMessage extends Email {
	 private static final String DATE_FORMAT = "yyyy-MM-dd";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6549896844646538119L;

	@Override
	public String createSubjectLine() {
		Resource resource =  (Resource) getMap().get(EmailService.RESOURCE2);
		Date expires = (Date) getMap().get(EmailService.EXPIRES2);
		String until = "";
		
		if(expires !=null){
			until = " until "+new SimpleDateFormat(DATE_FORMAT).format(expires);
		}
		
		return TdarConfiguration.getInstance().getSiteAcronym() + ": Access Granted to " + resource.getTitle()+until;
	}

}
