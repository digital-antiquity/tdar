/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.external.pid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.ConfigurationAssistant;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.transform.DcTransformer;

import edu.asu.lib.dc.DublinCoreDocument;

/**
 * @author Adam Brin
 * 
 */
public class EZIDDao implements ExternalIDProvider {

    // DOCUMENTATION: http://n2t.net/ezid/doc/apidoc.html#operation-get-identifier-metadata

    private static final String DATACITE_UNAV = "(:unav)";
    private static final String DOI_PROVIDER_PASSWORD = "doi.provider.password";
    private static final String DOI_PROVIDER_USERNAME = "doi.provider.username";
    private static final String DOI_PROVIDER_HOSTNAME = "doi.provider.hostname";
    private static final String DOI_PROVIDER_SHOULDER = "doi.provider.shoulder";
    public static final String HTTPS = "https";
    public static final String EZID_URL = "https://n2t.net/ezid";

    public static final String DATACITE_PROFILE_NAME = "datacite";
    public static final String DATACITE_TITLE = DATACITE_PROFILE_NAME + ".title";
    public static final String DATACITE_CREATOR = DATACITE_PROFILE_NAME + ".creator";
    public static final String DATACITE_PUBLISHER = DATACITE_PROFILE_NAME + ".publisher";
    public static final String DATACITE_RESOURCE_TYPE = DATACITE_PROFILE_NAME + ".resourcetype";
    public static final String DATACITE_PUBLICATIONYEAR = DATACITE_PROFILE_NAME + ".publicationyear";
    public static final String _PROFILE = "_profile";
    public static final String _TARGET = "_target";
    public static final String SUCCESS = "success";
    public static final String DOI_ARK_CREATION_REGEX = "\\s((\\w+)\\:(?:[^\\s]+))";
    public static final String _SHADOWED_BY = "_shadowedby";
    public static final String _STATUS = "_status";
    public static final String _EXPORT = "_export";
    public static final String _STATUS_UNAVAILABLE = "unavailable";
    private static final String _STATUS_AVAILABLE = "public";
    public final Logger logger = LoggerFactory.getLogger(getClass());

    CloseableHttpClient httpclient;

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    private String configIssue = "";

    public EZIDDao() {
        try {
            assistant.loadProperties("ezid.properties");

            URL url = new URL(getDOIProviderHostname());
            int port = TdarConfiguration.DEFAULT_PORT;
            if (url.getPort() == -1) {
                if (getDOIProviderHostname().toLowerCase().startsWith(HTTPS)) {
                    port = TdarConfiguration.HTTPS_PORT_DEFAULT;
                }
            }
            AuthScope scope = new AuthScope(url.getHost(), port);
            logger.trace("using port: {}", port);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(scope, new UsernamePasswordCredentials(getDOIProviderUsername(), getDOIProviderPassword()));
            httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        } catch (Throwable t) {
            configIssue = t.getMessage();
        }
    }

    @Override
    public boolean isConfigured() {
        if (httpclient == null) {
            return false;
        }
        if (StringUtils.isNotBlank(getDOIProviderHostname()) && StringUtils.isNotBlank(getDOIShoulder()) &&
                StringUtils.isNotBlank(getDOIProviderUsername()) && StringUtils.isNotBlank(getDOIProviderPassword())) {
            return true;
        }
        logger.warn("a required parameter for the EzidDao was not provided. " + configIssue);
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#connect(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean connect() throws IOException {
        logger.debug("creating challenge/response authentication request for: {} ({} / *****)", getDOIProviderHostname(), getDOIProviderUsername());
        HttpGet authenticationRequest = new HttpGet(getDOIProviderHostname() + "/login");
        processHttpRequest(authenticationRequest);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#logout(java.lang.String)
     */
    @Override
    public boolean logout() throws IOException {
        logger.info("logout: {} ", getDOIProviderHostname());
        HttpGet authenticationRequest = new HttpGet(getDOIProviderHostname() + "/logout");
        processHttpRequest(authenticationRequest);
        return true;
    }

    @SuppressWarnings("deprecation")
    private String processHttpRequest(HttpRequestBase authenticationRequest) throws IOException, ClientProtocolException {
        // authenticationRequest.con
        authenticationRequest.setHeader("Accept", "text/plain");
        logger.trace("RequestLine: {} ", authenticationRequest.getRequestLine());
        HttpResponse response = httpclient.execute(authenticationRequest);
        HttpEntity recievedEntity = response.getEntity();
        logger.trace("Login form get: " + response.getStatusLine());
        InputStream content = recievedEntity.getContent();
        String result = IOUtils.toString(content);
        content.close();

        if ((response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) && (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED)) {
            logger.error("StatusCode:{}", response.getStatusLine().getStatusCode());
            logger.trace(result);
            throw new TdarRecoverableRuntimeException("ezidDao.could_not_connect", Arrays.asList(result, authenticationRequest.getRequestLine().toString()));
        }
        recievedEntity.consumeContent();
        return result;
    }

    /*
     * returns a map of identifiers and values created by the system
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#create(org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, String> create(Resource r, String resourceUrl) throws IOException {
        return createOrModify(r, resourceUrl, getDOIProviderHostname(), "shoulder", getDOIShoulder());
    }

    /*
     * returns a map of all of the information the EZID server has on the resource
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#getMetadata(java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, String> getMetadata(String identifier) throws IOException {
        Map<String, String> typeMap = new HashMap<String, String>();
        HttpGet request = new HttpGet(getDOIProviderHostname() + "/id/" + identifier);
        String result = processHttpRequest(request);
        for (String line : result.split("[\r\n]")) {
            if (line.contains(":")) {
                String key = line.substring(0, line.indexOf(":"));
                String val = line.substring(line.indexOf(":") + 1);
                typeMap.put(key.trim(), val.trim());
                logger.trace("line: {} => {}", key, val);
            }
        }
        logger.trace(result);
        logger.trace("result: {}", typeMap);
        return typeMap;
    }

    /*
     * returns a map of identifiers and values created by the system
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#modify(org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws IOException {
        return createOrModify(r, resourceUrl, getDOIProviderHostname(), "id", identifier);
    }

    /*
     * returns a map of identifiers and values created by the system
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#delete(org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws IOException {
        return createOrModify(r, resourceUrl, getDOIProviderHostname(), "id", identifier, true);
    }

    public Map<String, String> forceDelete(Resource r, String resourceUrl, String identifier) throws IOException {
        HttpDelete post = new HttpDelete(getDOIProviderHostname() + "/id/" + identifier);
        return processRequest(post);
    }

    private Map<String, String> processRequest(HttpRequestBase post) throws IOException, ClientProtocolException {
        Map<String, String> typeMap = new HashMap<String, String>();
        String result = processHttpRequest(post);
        logger.trace("result: {}", result);
        Pattern pattern = Pattern.compile(DOI_ARK_CREATION_REGEX);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            typeMap.put(m.group(2).toUpperCase().trim(), m.group(1).trim());
        }

        logger.trace(result);
        if (!StringUtils.containsIgnoreCase(result, SUCCESS)) {
            throw new TdarRecoverableRuntimeException("ezidDao.could_not_create_doi", Arrays.asList(result));
        }
        return typeMap;
    }

    protected Map<String, String> createOrModify(Resource r, String resourceUrl, String hostname, String path, String shoulder) throws ClientProtocolException,
            IOException {
        return createOrModify(r, resourceUrl, hostname, path, shoulder, false);
    }

    protected Map<String, String> createOrModify(Resource r, String resourceUrl, String hostname, String path, String shoulder, boolean delete)
            throws ClientProtocolException,
            IOException {
        HttpPost post = new HttpPost(hostname + "/" + path + "/" + shoulder);
        String anvlContent = generateAnvlMetadata(r, resourceUrl, delete);
        logger.trace("sending content:to {} \n{}", post.getURI(), anvlContent);
        HttpEntity entity_ = EntityBuilder.create().setText(anvlContent).setContentType(ContentType.TEXT_PLAIN.withCharset("UTF-8")).build();
        post.setEntity(entity_);
        return processRequest(post);
    }

    @SuppressWarnings("incomplete-switch")
    protected String generateAnvlMetadata(Resource r, String url, boolean delete) {
        StringBuilder responseBuilder = new StringBuilder();
        DublinCoreDocument doc = DcTransformer.transformAny(r);
        String status = _STATUS_AVAILABLE;

        if (r.getStatus() == Status.ACTIVE) {
            buildAnvlLine(responseBuilder, DATACITE_CREATOR, aNVLEscape(StringUtils.join(doc.getCreator(), "; ")), DATACITE_UNAV);
            buildAnvlLine(responseBuilder, DATACITE_TITLE, aNVLEscape(r.getTitle()), DATACITE_UNAV);

            String resourceType = r.getResourceType().toDcmiTypeString();
            // as PER API-DOC, http://n2t.net/ezid/doc/apidoc.html , a subset of these do not exactly MATCH DCMI TYPES?
            switch (r.getResourceType()) {
                case PROJECT:
                    resourceType = "Collection";
                case SENSORY_DATA:
                    resourceType = resourceType.replace(" ", "");
                    break;
                case VIDEO:
                    resourceType = "Film";
                    break;
                case IMAGE:
                    resourceType = "Image";
                    break;
            }

            buildAnvlLine(responseBuilder, DATACITE_RESOURCE_TYPE, aNVLEscape(resourceType));
            if (r instanceof InformationResource) {
                buildAnvlLine(responseBuilder, DATACITE_PUBLISHER, aNVLEscape(((InformationResource) r).getPublisherName()), DATACITE_UNAV);
                buildAnvlLine(responseBuilder, DATACITE_PUBLICATIONYEAR, (((InformationResource) r).getDate()).toString(), DATACITE_UNAV);
            } else {
                buildAnvlLine(responseBuilder, DATACITE_PUBLISHER, DATACITE_UNAV);
                buildAnvlLine(responseBuilder, DATACITE_PUBLICATIONYEAR, DATACITE_UNAV);
            }
        }

        if ((r.getStatus() != Status.ACTIVE) || delete) {
            // EZID does not support DELETION, instead SETTING ALL VALUES TO EMPTY
            // responseBuilder.append(DATACITE_CREATOR).append(":").append("\n");
            // responseBuilder.append(DATACITE_RESOURCE_TYPE).append(":").append("\n");
            // responseBuilder.append(DATACITE_TITLE).append(":").append("\n");
            // responseBuilder.append(DATACITE_PUBLISHER).append(":").append("\n");
            // responseBuilder.append(DATACITE_PUBLICATIONYEAR).append(":").append("\n");
            status = _STATUS_UNAVAILABLE;
        }

        buildAnvlLine(responseBuilder, _STATUS, status);
        // buildAnvlLine(responseBuilder, _EXPORT, "no");
        buildAnvlLine(responseBuilder, _TARGET, url);
        buildAnvlLine(responseBuilder, _PROFILE, DATACITE_PROFILE_NAME);
        return responseBuilder.toString();
    }

    public StringBuilder buildAnvlLine(StringBuilder sb, String key, String val_, String fallback) {
        String val = val_;
        if (StringUtils.isBlank(val) && StringUtils.isNotBlank(fallback)) {
            val = fallback;
        }
        return buildAnvlLine(sb, key, val);
    }

    public StringBuilder buildAnvlLine(StringBuilder sb, String key, String val) {
        return sb.append(key).append(": ").append(val).append("\n");
    }

    protected String aNVLEscape(String s) {
        if (StringUtils.isEmpty(s)) {
            return "";
        }
        return s.replace("%", "%25").replace("\n", "%0A").replace("\r", "%0D").replace(":", "%3A");
    }

    /**
     * @return
     * 
     */
    public String getDOIShoulder() {
        return assistant.getStringProperty(DOI_PROVIDER_SHOULDER, "");
    }

    /**
     * @return
     * 
     */
    public String getDOIProviderPassword() {
        return assistant.getStringProperty(DOI_PROVIDER_PASSWORD);
    }

    /**
     * @return
     * 
     */
    public String getDOIProviderUsername() {
        return assistant.getStringProperty(DOI_PROVIDER_USERNAME);
    }

    /**
     * @return
     * 
     */
    public String getDOIProviderHostname() {
        return assistant.getStringProperty(DOI_PROVIDER_HOSTNAME);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
