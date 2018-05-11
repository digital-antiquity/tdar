package org.tdar.core.service.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.EmailDao;
import org.tdar.utils.EmailRawMessageHelper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;

public class MockAwsQueuePollerServiceImpl implements AwsQueuePollerService {
    private static final TdarConfiguration config = TdarConfiguration.getInstance();
    private Regions awsRegion = Regions.US_WEST_2;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    EmailDao emailDao;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getBouncedMessages()
     */
    @Override
    public List<Message> getBouncedMessages() {
        logger.debug("Mocking getting messages");
        List<Message> messages = new ArrayList<Message>();

        for (Email email : emailDao.findAll()) {
            if (email.getStatus() != Status.BOUNCED && email.getTo().equals("bounce@simulator.amazonses.com")) {
                messages.add(getMessage(email));
            }
        }
        return messages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#removeMessageFromQueue(java.lang.String)
     */
    @Override
    public void removeMessageFromQueue(String messageReceiptHandle) {

    }

    private BasicAWSCredentials getAwsCredentials() {
        return new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
    }

    private AmazonSQS getSqsClient() {
        AmazonSQS client = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(getAwsCredentials())).withRegion(awsRegion).build();
        return client;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getTdarMessageId(org.json.simple.JSONArray)
     */
    @Override
    public String getTdarMessageId(JSONArray headers) {
        String tdarMessageId = null;

        if (headers == null) {
            return null;
        }

        int size = headers.size();
        for (int i = 0; i < size; i++) {
            JSONObject header = (JSONObject) headers.get(i);
            if (header.get("name").equals(EmailRawMessageHelper.X_TDAR_MESSAGE_ID)) {
                tdarMessageId = (String) header.get("value");
                break;
            }
        }
        return tdarMessageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getMessageHeaders(com.amazonaws.services.sqs.model.Message)
     */
    @Override
    public JSONArray getMessageHeaders(Message message) {
        JSONArray json = null;
        try {
            json = (JSONArray) new JSONParser().parse(message.getBody());
        } catch (ParseException e) {
            logger.debug("Couldn't parse the message headers: {} {}", e, e);
        }
        return json;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getNotificationType(com.amazonaws.services.sqs.model.Message)
     */
    @Override
    public String getNotificationType(Message message) {
        return "Bounce";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getBounce(com.amazonaws.services.sqs.model.Message)
     */
    @Override
    public String getBounce(Message message) {
        String[] messages  = new String[]{
        "{\"bounceType\":\"Transient\",\"bouncedRecipients\":[{\"emailAddress\":\"test@tdar.org\",\"action\":\"failed\",\"diagnosticCode\":\"smtp; 554 4.4.7 Message expired: unable to deliver in 840 minutes.<421 4.4.0 Unable to lookup DNS for tdar.org>\",\"status\":\"4.4.7\"}],\"bounceSubType\":\"General\",\"feedbackId\":\"01000162b07fce73-efa9d490-cf94-4cfa-bc8e-2f0996aec632-000000\",\"reportingMTA\":\"dsn; a8-97.smtp-out.amazonses.com\",\"timestamp\":\"2018-04-10T16:59:48.991Z\"}",
        "{\"bounceType\":\"Permanent\",\"bouncedRecipients\":[{\"emailAddress\":\"failure@tdar.org\",\"action\":\"failed\",\"diagnosticCode\":\"smtp; 550 5.1.1 <failure@tdar.org>: Email address could not be found, or was misspelled (G8)\",\"status\":\"5.1.1\"}],\"bounceSubType\":\"General\",\"feedbackId\":\"01000163227dea98-1ad15402-e8b4-40d2-ad3e-ee8db4be5de5-000000\",\"reportingMTA\":\"dsn; a8-29.smtp-out.amazonses.com\",\"remoteMtaIp\":\"173.203.187.1\",\"timestamp\":\"2018-05-02T20:14:28.667Z\"}",
        "{\"bounceType\":\"Permanent\",\"bounceSubType\":\"General\",\"bouncedRecipients\":[{\"emailAddress\":\"bounce@simulator.amazonses.com\",\"action\":\"failed\",\"status\":\"5.1.1\",\"diagnosticCode\":\"smtp; 550 5.1.1 user unknown\"}],\"timestamp\":\"2018-02-16T21:29:30.231Z\",\"feedbackId\":\"01010161a085ab48-f70f929e-f80c-4c04-a591-06733725ca40-000000\",\"remoteMtaIp\":\"207.171.163.188\",\"reportingMTA\":\"dsn; a27-10.smtp-out.us-west-2.amazonses.com\"}",
        "{\"bounceType\":\"Transient\",\"bouncedRecipients\":[{\"emailAddress\":\"test@tdar.org\"}],\"bounceSubType\":\"General\",\"feedbackId\":\"01000162ba7ec05e-7eb6816c-9b8d-483a-917b-c284f6521772-000000\",\"timestamp\":\"2018-04-12T15:34:42.000Z\"}"
        };
        
        Random random = new Random();
        JSONObject bounce;
        try {
            bounce = (JSONObject) new JSONParser().parse(messages[random.nextInt(messages.length)]);
        
            JSONArray recipients = (JSONArray) bounce.get("bouncedRecipients");
            JSONObject recipient = (JSONObject) recipients.get(0);
            String errorString = "";
            
            if(recipient.containsKey("diagnosticCode")){
             errorString = (String) recipient.get("diagnosticCode")+"  ";   
            }
            
            errorString += bounce.toJSONString();
            return errorString;
        } catch (ParseException e) {
              return "COULDNT PARSE "+e.getMessage();
        }
       
    }
    

    
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.email.AwsQueuePollerService#getMessageJson(com.amazonaws.services.sqs.model.Message)
     */
    @Override
    public JSONObject getMessageJson(Message message) {
        JSONObject msg = new JSONObject();
        return msg;
    }

    private Message getMessage(Email email) {
        Message message = new Message();
        message.setBody("[{\"name\":\"" + EmailRawMessageHelper.X_TDAR_MESSAGE_ID + "\",\"value\":\"" + email.getMessageUuid() + "\"}]");
        return message;
    }

}
