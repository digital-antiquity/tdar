package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;

public class AmazonEmailServiceTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

//    @Autowired 
//    protected AwsEmailSender awsEmailService;
    
    @Test
    public void testAwsCredentials(){
    	String accessKey = TdarConfiguration.getInstance().getAwsAccessKey();
    	String secretKey = TdarConfiguration.getInstance().getAwsSecretKey(); 
    	
    	logger.debug("Key: {} Pass:{}",accessKey,secretKey);
    	
    	assertNotNull(accessKey);
    	assertNotNull(secretKey);
    }
    
    @Test
    public void testTestAwsEmail(){
    	assertEquals(EmailType.TEST_EMAIL.getFromAddress(), "test@tdar.org");
    }
}
