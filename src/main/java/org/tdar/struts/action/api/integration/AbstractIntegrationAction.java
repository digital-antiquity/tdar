package org.tdar.struts.action.api.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;

@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream" })
})
public class AbstractIntegrationAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -1603470633052691056L;
    private InputStream jsonInputStream;

    @Autowired
    protected transient SerializationService serializationService;

    public InputStream getJsonInputStream() {
        return jsonInputStream;
    }

    protected final void setJsonInputStream(InputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

    /**
     * Convenience method for serializing the specified object and converting it to an inputStream.
     * @param obj object to stringify
     * @param jsonFilter JSON filter view to use during serialization
     * @throws IOException
     */
    protected final void setJsonObject(Object obj, Class<?> jsonFilter) throws IOException {
        setJsonInputStream(new ByteArrayInputStream(serializationService.convertToFilteredJson(obj, jsonFilter).getBytes()));
    }

    /**
     * Convenience method for serializing the specified object and converting it to an inputStream.
     * @param obj object to stringify
     * @throws IOException
     */
    protected final void setJsonObject(Object obj) throws IOException {
        setJsonInputStream(new ByteArrayInputStream(serializationService.convertToJson(obj).getBytes()));
    }

}
