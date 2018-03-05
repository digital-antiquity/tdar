package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.email.AwsEmailSender;

public class AmazonEmailServiceTestCase extends AbstractIntegrationTestCase {

    @Autowired 
    protected AwsEmailSender awsEmailService;
    
    @Test
    public void testAwsCredentials(){
    	String accessKey = TdarConfiguration.getInstance().getAwsAccessKey();
    	String secretKey = TdarConfiguration.getInstance().getAwsSecretKey(); 
    	
    	getLogger().debug("Key: {} Pass:{}",accessKey,secretKey);
    	
    	assertNotNull(accessKey);
    	assertNotNull(secretKey);
    }
    
    @Test
    public void testTestAwsEmail(){
    	assertEquals(EmailType.TEST_EMAIL.getFromAddress(), "test@tdar.org");
    }
}
