package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.authority.AuthorityManagementLog;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("ADMIN_NOTIFY_MSG")
public class AdminNotificationMessage extends Email {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4278006286599861751L;
	
	@Override
	public String createSubjectLine() {
		@SuppressWarnings("rawtypes")
		AuthorityManagementLog logData  = (AuthorityManagementLog) getMap().get("");
		int numUpdated = (int) getMap().get("");
		String className  = (String) getMap().get("");
		
		return MessageHelper.getMessage(EmailType.ADMIN_NOTIFICATION.getLocaleKey(),
                Arrays.asList(
                		TdarConfiguration.getInstance().getSiteAcronym(),
                        MessageHelper.getMessage("authorityManagementService.service_name"),
                        logData.getUserDisplayName(), 
                        numUpdated, 
                        className, 
                        logData.getAuthority().toString())
            );
	}

}
