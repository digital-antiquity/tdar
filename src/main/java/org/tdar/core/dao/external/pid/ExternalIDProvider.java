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

    public abstract boolean connect() throws ClientProtocolException, IOException;

    public abstract boolean logout() throws ClientProtocolException, IOException;

    /*
     * returns a map of identifiers and values created by the system
     */
    public abstract Map<String, String> create(Resource r, String resourceUrl) throws ClientProtocolException, IOException;

    /*
     * returns a map of all of the information the EZID server has on the resource
     */
    public abstract Map<String, String> getMetadata(String identifier) throws ClientProtocolException, IOException;

    /*
     * returns a map of identifiers and values created by the system
     */
    public abstract Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException;

    /*
     * returns a map of identifiers and values created by the system
     */
    public abstract Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException;

}