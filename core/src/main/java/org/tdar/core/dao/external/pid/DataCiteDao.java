/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.external.pid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.transform.DataCiteTransformer;

import edu.asu.lib.datacite.DataCiteDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;

/**
 * @author Adam Brin
 * 
 */
public class DataCiteDao implements ExternalIDProvider {

    private static final String UTF_8 = "UTF-8";

    private static final String METADATA = "metadata";
    // DOCUMENTATION: https://support.datacite.org/docs/mds-api-guide

    private static final String DOI_PROVIDER_PASSWORD = "doi.provider.password";
    private static final String DOI_PROVIDER_USERNAME = "doi.provider.username";
    private static final String DOI_PROVIDER_HOSTNAME = "doi.provider.hostname";
    private static final String DOI_PROVIDER_SHOULDER = "doi.provider.shoulder";
    public static final String HTTPS = "https";
    public static final String EZID_URL = "https://mds.test.datacite.org/metadata";

    public final Logger logger = LoggerFactory.getLogger(getClass());

    CloseableHttpClient httpclient;

    private ConfigurationAssistant assistant = new ConfigurationAssistant();
    private String configIssue = "";

    public DataCiteDao() {
        try {
            assistant.loadProperties("datacite.properties");

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
            logger.error(t.getMessage(), t);
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
        // logger.debug("creating challenge/response authentication request for: {} ({} / *****)", getDOIProviderHostname(), getDOIProviderUsername());
        // HttpGet authenticationRequest = new HttpGet(getDOIProviderHostname() + "/login");
        // processHttpRequest(authenticationRequest);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.dao.DaoProvider#logout(java.lang.String)
     */
    @Override
    public boolean logout() throws IOException {
        // logger.info("logout: {} ", getDOIProviderHostname());
        // HttpGet authenticationRequest = new HttpGet(getDOIProviderHostname() + "/logout");
        // processHttpRequest(authenticationRequest);
        return true;
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
        return createOrModify(r, resourceUrl, getDOIProviderHostname(), getDOIShoulder());
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
        String uri = getDOIProviderHostname() + "/metadata/" + identifier;
        logger.trace("urL: {}", uri);
        HttpGet request = new HttpGet(uri);

        CloseableHttpResponse execute = httpclient.execute(request);
        logger.debug("done request");
        if (execute.getStatusLine().getStatusCode() != 200) {
            throw new TdarRecoverableRuntimeException("error getting doi metadata:" + execute.getStatusLine().toString());
        }
        String xml = IOUtils.toString(execute.getEntity().getContent());
        logger.trace("result: {}", xml);
        typeMap.put("xml", xml);
        typeMap.put("id", identifier);
        request.releaseConnection();
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
        return createOrModify(r, resourceUrl, getDOIProviderHostname(), identifier);
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
        /**
         * # DELETE /metadata/10.5072/JQX3-61AT
         * $ curl -H "Content-Type: application/plain;charset=UTF-8" -X DELETE -i --user username:password
         * https://mds.test.datacite.org/metadata/10.5072/JQX3-61AT
         */
        HttpDelete delete = new HttpDelete(constructDataCiteUrl(r, METADATA));
        CloseableHttpResponse execute = httpclient.execute(delete);
        logger.debug("{}",execute.getStatusLine());
        if (execute.getStatusLine().getStatusCode() != 200) {
            throw new TdarRecoverableRuntimeException("error deleting doi:" + execute.getStatusLine().toString());
        }
        delete.releaseConnection();
        return null;
    }

    private String constructDataCiteUrl(Resource r, String prefix) {
        return getDOIProviderHostname() + "/" + prefix + "/" + constructDoi(r);
    }

    private String constructDoi(Resource r) {
        if (StringUtils.isNotBlank(r.getExternalId())) {
            return r.getExternalId();
        }
        return getDOIShoulder() + r.getId();
    }

    protected Map<String, String> createOrModify(Resource r, String resourceUrl, String hostname, String path) throws ClientProtocolException,
            IOException {
        // # PUT /metadata/10.5072/JQX3-61AT
        // $ curl -H "Content-Type: application/xml;charset=UTF-8" -X POST -i --user username:password -d @10.5072/JQX3-61AT.xml
        // https://mds.test.datacite.org/metadata
        HttpPut put = new HttpPut(constructDataCiteUrl(r, METADATA));

        DataCiteDocument transformAny = DataCiteTransformer.transformAny(r);
        ByteArrayOutputStream sos = new ByteArrayOutputStream();
        try {
            JaxbDocumentWriter.write(transformAny, sos, true);
            String result = sos.toString(UTF_8);
            logger.trace("sending content:to {} \n{}", put.getURI(), result);
            HttpEntity entity_ = EntityBuilder.create().setText(result).setContentType(ContentType.APPLICATION_XML.withCharset(UTF_8)).build();
            put.setEntity(entity_);
            CloseableHttpResponse execute = httpclient.execute(put);
            put.releaseConnection();
            if (execute.getStatusLine().getStatusCode() != 201) {
                logger.debug(execute.toString());
                throw new TdarRecoverableRuntimeException("error registering doi:" + execute.getStatusLine().toString());
            }
            if (StringUtils.isBlank(r.getExternalId())) {
                registerUrl(constructDataCiteUrl(r, "doi"), r);
            }
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        map.put("DOI", constructDoi(r));
        return map;
    }

    public void registerUrl(String url, Resource r) throws ClientProtocolException, IOException {
        /**
         * PUT /doi
         * $ curl -H "Content-Type:text/plain;charset=UTF-8" -X PUT --user username:password -d
         * "$(printf 'doi=10.5072/JQX3-61AT\nurl=http://example.org/')" https://mds.test.datacite.org/doi/10.5072/JQX3-61AT
         */
        HttpPut put = new HttpPut(url);
        EntityBuilder e = EntityBuilder.create();
        e.setParameters(new BasicNameValuePair("doi", constructDoi(r)), new BasicNameValuePair("url", r.getAbsoluteUrl()));
        put.setEntity(e.build());
        CloseableHttpResponse execute = httpclient.execute(put);
        put.releaseConnection();
        if (isNotOkStatusCode(execute.getStatusLine().getStatusCode())) {
            throw new TdarRecoverableRuntimeException("error registering doi:" + execute.getStatusLine().toString());
        }

    }

    private boolean isNotOkStatusCode(int statusCode) {
        if (statusCode != 200 && statusCode != 201) {
            return true;
        }
        return false;
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
