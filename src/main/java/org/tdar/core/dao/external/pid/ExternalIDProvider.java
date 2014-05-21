/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.external.pid;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.Configurable;

/**
 * Interface managing all external ID lookups, this would be for an ARK, DOI, Handle , etc.
 * 
 * @author Adam Brin
 * 
 */
public interface ExternalIDProvider extends Configurable {

    /**
     * Connect to the external id provider
     * 
     * @return
     * @throws IOException
     */
    boolean connect() throws IOException;

    /**
     * Logout from the external id provider
     * 
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    boolean logout() throws IOException;

    /**
     * returns a map of identifiers and values created by the system
     * 
     * @param r
     * @param resourceUrl
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    Map<String, String> create(Resource r, String resourceUrl) throws IOException;

    /**
     * returns a map of all of the information the ID server has on the resource
     * 
     * @param identifier
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    Map<String, String> getMetadata(String identifier) throws IOException;

    /**
     * returns a map of identifiers and values created by the system
     * 
     * @param r
     * @param resourceUrl
     * @param identifier
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws IOException;

    /**
     * returns a map of identifiers and values created by the system
     * 
     * @param r
     * @param resourceUrl
     * @param identifier
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws IOException;

}