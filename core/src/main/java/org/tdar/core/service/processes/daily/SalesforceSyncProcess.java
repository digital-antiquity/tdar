package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.configuration.ConfigurationAssistant;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.processes.AbstractScheduledProcess;

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
@Scope("prototype")
public class SalesforceSyncProcess extends AbstractScheduledProcess {

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
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public void execute() {

        Date yesterday = DateTime.now().minusDays(1).toDate();
        for (TdarUser user : entityService.findAllRegisteredUsers(100)) {
            if (user != null && user.getDateUpdated() != null && yesterday.before(user.getDateUpdated())) {
                try {
                    logger.debug("sending ... {}", user);
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpPost post = new HttpPost(getPostUrl());
                    List<NameValuePair> postNameValuePairs = new ArrayList<>();
                    postNameValuePairs.add(new BasicNameValuePair("oid", getOid()));
                    postNameValuePairs.add(new BasicNameValuePair("retURL", getReturnUrl()));
                    postNameValuePairs.add(new BasicNameValuePair("first_name", user.getFirstName()));
                    postNameValuePairs.add(new BasicNameValuePair("last_name", user.getLastName()));
                    postNameValuePairs.add(new BasicNameValuePair("email", user.getEmail()));
                    postNameValuePairs.add(new BasicNameValuePair("description", String.format(
                            "Tdar Link: %s\nAffiliation: %s\nContributor: %s\nContributor Reason: %s",
                            UrlService.absoluteUrl(user), user.getAffiliation(), user.isContributor(), user.getContributorReason())));
                    postNameValuePairs.add(new BasicNameValuePair("company", user.getInstitutionName()));
                    postNameValuePairs.add(new BasicNameValuePair("phone", user.getPhone()));
                    postNameValuePairs.add(new BasicNameValuePair("lead_source", "tdar-app"));

                    post.setEntity(new UrlEncodedFormEntity(postNameValuePairs, "UTF-8"));
                    HttpResponse response = httpclient.execute(post);
                    HttpEntity entity = response.getEntity();
                    logger.debug("response:[{}] {}", response, response.getStatusLine());
                    String content = IOUtils.toString(entity.getContent());
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        logger.error("error processing: {} -- {}", user, content);
                    }
                } catch (Exception e) {
                    logger.error("exception in salesforce sync:{}", e, e);
                }
            }
        }

    }

    private String getReturnUrl() {
        return assistant.getStringProperty("form.returnUrl", "http://orspakdev.asu.edu/confirmation.html");
    }

    private String getOid() {
        return assistant.getStringProperty("form.oid");
    }

    private String getPostUrl() {
        return assistant.getStringProperty("form.url");
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
        try {
            assistant.loadProperties("salesforce.properties");
            return true;
        } catch (Exception e) {
            logger.warn("salesforce not configured properly");
        }
        return false;
    }

}
