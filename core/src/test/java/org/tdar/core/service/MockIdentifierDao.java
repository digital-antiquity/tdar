/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.client.ClientProtocolException;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.pid.ExternalIDProvider;
import org.tdar.core.service.processes.daily.DoiProcess;

/**
 * @author Adam Brin
 * 
 */
public class MockIdentifierDao implements ExternalIDProvider {

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public boolean connect() throws ClientProtocolException, IOException {
        return true;
    }

    @Override
    public boolean logout() throws ClientProtocolException, IOException {
        return true;
    }

    @Override
    public Map<String, String> create(Resource r, String resourceUrl) throws ClientProtocolException, IOException {
        Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put(DoiProcess.DOI_KEY, "doi:" + System.currentTimeMillis());
        return toReturn;
    }

    @Override
    public Map<String, String> getMetadata(String identifier) throws ClientProtocolException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> modify(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
        Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put(DoiProcess.DOI_KEY, identifier);
        return toReturn;
    }

    @Override
    public Map<String, String> delete(Resource r, String resourceUrl, String identifier) throws ClientProtocolException, IOException {
        return modify(r, resourceUrl, identifier);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
