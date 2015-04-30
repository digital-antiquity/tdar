package org.tdar.core.service.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.EntityService;

/**
 * $Id$
 * 
 * ScheduledProcess to update aggregate stats
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */

@Component
public class SalesforceSyncProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final long serialVersionUID = 4558666368084097084L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurationAssistant assistant = new ConfigurationAssistant();

    @Autowired
    private transient EntityService entityService;

    @Override
    public String getDisplayName() {
        return "Salesforce Sync";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        return HomepageGeographicKeywordCache.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public void execute() {
        assistant.loadProperties("salesforce.properties");

        List<TdarUser> people = new ArrayList<>();
        Date yesterday = DateTime.now().minusDays(1).toDate();
        for (TdarUser user : entityService.findAllRegisteredUsers(100)) {
            if (user != null && user.getDateUpdated() != null && yesterday.before(user.getDateUpdated())) {
                try {
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    for (TdarUser person : people) {
                        HttpPost post = new HttpPost(getPostUrl());
                        List<NameValuePair> postNameValuePairs = new ArrayList<>();
                        postNameValuePairs.add(new BasicNameValuePair("oid", getOid()));
                        postNameValuePairs.add(new BasicNameValuePair("retURL", getReturnUrl()));
                        postNameValuePairs.add(new BasicNameValuePair("first_name", person.getFirstName()));
                        postNameValuePairs.add(new BasicNameValuePair("last_name", person.getLastName()));
                        postNameValuePairs.add(new BasicNameValuePair("email", person.getEmail()));
                        postNameValuePairs.add(new BasicNameValuePair("company", person.getInstitutionName()));
                        postNameValuePairs.add(new BasicNameValuePair("phone", person.getPhone()));

                        post.setEntity(new UrlEncodedFormEntity(postNameValuePairs, "UTF-8"));
                        HttpResponse response = httpclient.execute(post);
                        HttpEntity entity = response.getEntity();
                        logger.debug("response:[{}] {}", response.getStatusLine(), entity);
                    }
                } catch (IOException e) {
                    logger.error("exception in salesforce sync:{}",e,e);
                }
            }
        }

    }

    private String getReturnUrl() {
        return assistant.getStringProperty("form.returnUrl","http://orspakdev.asu.edu/confirmation.html");
    }

    private String getOid() {
        return assistant.getStringProperty("form.oid","00D19000000DRze");
    }

    private String getPostUrl() {
        return assistant.getStringProperty("form.url","https://cs24.salesforce.com/servlet/servlet.WebToLead?encoding=UTF-8");
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
