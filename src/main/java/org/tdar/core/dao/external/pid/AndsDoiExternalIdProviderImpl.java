package org.tdar.core.dao.external.pid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
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
 * @author Martin Paulo
 */
@Service
public class AndsDoiExternalIdProviderImpl implements ExternalIDProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    private boolean isEnabled = true; // the happy case
    private String configIssue;
    private AndsDoiIdentity ourId;
    private AndsDoiClient doiClient = new AndsDoiClient();
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
        Map<String, String> result = new HashMap<>();
        AndsDoiResponse response = doiClient.mintDOI(resourceUrl, populateDTO(r), debug);
        validateResponse("create", response);
        result.put(DoiProcess.DOI_KEY, response.getDoi());
        return result;
    }

    @Override
    public Map<String, String> getMetadata(String identifier) throws ClientProtocolException, IOException {
        throw new TdarRecoverableRuntimeException("This method has yet to be writted.");
    }

    @Override
    public Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
        AndsDoiResponse response = doiClient.updateDOI(identifier, resourceUrl, populateDTO(r), debug);
        validateResponse("modify", response);
        return new HashMap<>();
    }

    @Override
    public Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
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
        for (ResourceCreator creator : r.getPrimaryCreators()) {
            creatorNames.add(creator.getCreator().getName());
        }
        doiDTO.setCreators(creatorNames);
        if (r instanceof Document) {
            doiDTO.setPublisher(((Document) r).getPublisherName());
        }
        doiDTO.setTitle(r.getTitle());
        return doiDTO;
    }


}
