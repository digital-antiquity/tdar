package org.tdar.core.dao.external.pid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.processes.DoiProcess;


import au.csiro.doiclient.AndsDoiClient;
import au.csiro.doiclient.AndsDoiResponse;
import au.csiro.doiclient.business.AndsDoiIdentity;
import au.csiro.doiclient.business.DoiDTO;

/**
 * Uses the ANDS "Cite my data" DOI service to mint doi's
 * Overview: http://ands.org.au/services/cite-my-data.html
 * FAQ's: http://ands.org.au/cite-data/doi_q_and_a.html
 * Technical documentation: http://ands.org.au/resource/r9-cite-my-data-v1.1-tech-doco.pdf
 * Client source code: http://andspidclient.sourceforge.net/
 * 
 * ANDS use the same server for test and production. Hence we have had to go to quite a bit of extra work to make sure that the default is, should anything 
 * go wrong, "TEST" !!! See: <a href="https://jira.ands.org.au/browse/SD-4420">SD-4420</a>
 * 
 * @author Martin Paulo
 */
@Service
public class AndsDoiExternalIdProviderImpl implements ExternalIDProvider {

    
    protected static final String IS_PRODUCTION_SERVER_KEY = "is.production.server";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    
    private boolean isEnabled = true; // the happy case
    private String configIssue;
    private boolean debug;
    private AndsDoiClient doiClient = new AndsDoiClient();
    
    /**
     * The ANDS documentation has the following clear requirement:
     * If in test mode, *you* have to remember to prefix the words "TEST" to your application key to let the world know that this isn't a proper DOI, if 
     * testing. Hence this factory class to make this a testable proposition.
     */
    protected static class IdentityFactory {
        public static final String TEST_PREFIX = "TEST";
        private String appId;
        private boolean productionServer;
        private String authDomain;
        private AndsDoiIdentity nullId = new AndsDoiIdentity();
        private AndsDoiIdentity applicationId;

        IdentityFactory(String appId, String authDomain, boolean productionServer) {
            this.appId = appId;
            this.authDomain = authDomain;
            this.productionServer = productionServer;
        }
        
        public AndsDoiIdentity getAppId() {
            if (applicationId == null) {
                applicationId = new AndsDoiIdentity((productionServer ? "" : TEST_PREFIX) + appId, authDomain);
            }
            return applicationId;
        }
        
        public AndsDoiIdentity getNullAppId() {
            return nullId;
        }
    }

    private IdentityFactory identityFactory;
    

    public AndsDoiExternalIdProviderImpl() {
        this("andsdoi.properties");
    }
    
    protected AndsDoiExternalIdProviderImpl(String propertyFileName) {
        try {
            assistant.loadProperties(propertyFileName);
            debug = assistant.getBooleanProperty("is.debug", false);
            // you have to have the key, and it has to be set to true for this to be considered a production server.
            boolean productionServer = assistant.getBooleanProperty(IS_PRODUCTION_SERVER_KEY, false);
            identityFactory = new IdentityFactory(getStringProperty("app.id"), getStringProperty("auth.domain"), productionServer);
            doiClient.setDoiServiceHost(getStringProperty("service.host"));
            doiClient.setDoiServicePath(getStringProperty("service.path"));
            doiClient.setDoiServicePort(assistant.getIntProperty("service.port", 80));
            isEnabled = assistant.getBooleanProperty("is.enabled", true);
        } catch (Throwable t) {
            isEnabled = false;
            configIssue = t.getMessage();
        }
    }

    /**
     * @param property The name of the property in the property file
     * @return The value of the property
     * @throws IllegalStateException the property was not found
     */
    private String getStringProperty(String property) {
        String result = assistant.getProperty(property, null);
        if (result == null) {
            throw new IllegalStateException("AndsDoi required property not set: " + property);
        }
        return result;
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
    public boolean connect() {
        logger.debug("Connecting to ANDS Doi Service");
        doiClient.setRequestorIdentity(identityFactory.getAppId());
        return true;
    }

    @Override
    public boolean logout() {
        logger.debug("Disconnecting from ANDS Doi Service");
        doiClient.setRequestorIdentity(identityFactory.getNullAppId());
        return true;
    }

    @Override
    public Map<String, String> create(Resource r, String resourceUrl) throws IOException {
        Map<String, String> result = new HashMap<>();
        AndsDoiResponse response = doiClient.mintDOI(resourceUrl, populateDTO(r), debug);
        validateResponse("create", response);
        result.put(DoiProcess.DOI_KEY, response.getDoi());
        return result;
    }

    @Override
    public Map<String, String> getMetadata(String identifier) {
        throw new TdarRecoverableRuntimeException("This method has yet to be writted.");
    }

    @Override
    public Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws IOException {
        AndsDoiResponse response = doiClient.updateDOI(identifier, resourceUrl, populateDTO(r), debug);
        validateResponse("modify", response);
        return new HashMap<>();
    }

    @Override
    public Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws IOException {
        AndsDoiResponse response = doiClient.deActivateDOI(identifier, debug);
        validateResponse("deactive", response);
        return new HashMap<>();
    }

    /**
     * Simply throws an exception if the operation did not succeed.
     * @param operation A string describing the operation attempted.
     * @param response The response received from the server
     * @throws TdarRecoverableRuntimeException if the operation did not succeed.
     */
    @SuppressWarnings("static-method")
    private void validateResponse(String operation, AndsDoiResponse response) {
        if (!response.isSuccess()) {
            throw new TdarRecoverableRuntimeException("Could not " + operation + " doi: " + response.getMessage());
        }
    }

    @SuppressWarnings("static-method")
    private DoiDTO populateDTO(Resource r) {
        DoiDTO doiDTO = new DoiDTO();
        java.util.List<String> creatorNames = new ArrayList<>();
        // was primary creator, but that was returning null : Daniel feels in Australia this should be the copyright holder.
        for (ResourceCreator creator : r.getResourceCreators()) {
            creatorNames.add(creator.getCreator().getName());
        }
        doiDTO.setCreators(creatorNames);
        // Ands mandate that we must list a publisher and a publication year. I suspect that Ands thus can only deal with documents...
        if (r instanceof Document) {
            Document document = (Document)r;
            doiDTO.setPublisher(document.getPublisherName());
            doiDTO.setPublicationYear(String.valueOf(document.getDate()));
        }
        doiDTO.setTitle(r.getTitle());
        return doiDTO;
    }

    /**
     * @return the string value of the app ID. Used for testing.
     */
    protected String getAppId() {
        return identityFactory.getAppId().getAppId();
    }

}
