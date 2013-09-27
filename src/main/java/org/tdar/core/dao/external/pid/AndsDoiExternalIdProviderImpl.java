package org.tdar.core.dao.external.pid;

import java.io.IOException;
import java.util.Map;

import org.apache.http.auth.AUTH;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.ConfigurationAssistant;

import au.csiro.doiclient.AndsDoiClient;
import au.csiro.doiclient.AndsDoiResponse;
import au.csiro.doiclient.business.AndsDoiIdentity;
import au.csiro.doiclient.business.DoiDTO;
import au.csiro.pidclient.business.AndsPidIdentity;

/**
 * DOCUMENTATION: http://andspidclient.sourceforge.net/ and http://services.ands.org.au/documentation/pids/pids_text.pdf
 * @author Martin Paulo
 */
@Service
public class AndsDoiExternalIdProviderImpl implements ExternalIDProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    private boolean isEnabled = true; // the happy case
    private String configIssue;
    private AndsDoiClient doiClient = new AndsDoiClient();
    private AndsDoiIdentity ourId;  
    private AndsDoiIdentity nullId = new AndsDoiIdentity();

    private boolean debug;
    
    public AndsDoiExternalIdProviderImpl() {
        try {
            assistant.loadProperties("andsdoi.properties");
            debug = assistant.getBooleanProperty("debug", false);
            ourId = new AndsDoiIdentity(assistant.getProperty("app.id"), assistant.getProperty("auth.domain"));
            doiClient.setDoiServiceHost(assistant.getProperty("service.host"));
            doiClient.setDoiServicePath(assistant.getProperty("service.path"));
            doiClient.setDoiServicePort(assistant.getIntProperty("service.port"));
        } catch (Throwable t) {
            isEnabled = false;
            configIssue = t.getMessage();
        }
    }

    
    @Override
    public boolean isConfigured() {
        if (isEnabled) {
            return true;
        }
        logger.debug("a required parameter for the AndsDoiExternalIdProviderImpl was not provided. " + configIssue);
        return false;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean connect() throws ClientProtocolException, IOException {
        logger.debug("Connecting to ANDS Doi Service");
        doiClient.setRequestorIdentity(ourId);
        return true;
    }

    @Override
    public boolean logout() throws ClientProtocolException, IOException {
        logger.debug("Disconnecting from ANDS Doi Service");
        doiClient.setRequestorIdentity(nullId);
        return true;
    }

    @Override
    public Map<String, String> create(Resource r, String resourceUrl) throws ClientProtocolException, IOException {
        DoiDTO doiDTO = new DoiDTO();
        AndsDoiResponse response = doiClient.mintDOI(resourceUrl, doiDTO , debug);
        return null;
    }

    @Override
    public Map<String, String> getMetadata(String identifier) throws ClientProtocolException, IOException {
        AndsDoiResponse response = doiClient.requestMetaDataOfDOI(identifier);
        return null;
    }

    @Override
    public Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
        DoiDTO doiDTO = new DoiDTO();
        AndsDoiResponse response = doiClient.updateDOI(identifier, resourceUrl, doiDTO, debug);
        return null;
    }

    @Override
    public Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
        return null;
    }

}
