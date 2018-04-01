package org.tdar.struts_base.result;

import java.io.InputStream;

import org.tdar.utils.json.JacksonView;

public interface HasJsonDocumentResult {

    /**
     * returns the java object that will be serialized to JSON. Use this along with getJsonView to specify how jackson should filter or transform the result.
     * Alternately, use getJsonInputStream to define the exact json directly
     * 
     * @return
     */
    default Object getResultObject() {
        return null;
    }

    /**
     * specify the exact JSON for the result
     * 
     * @return
     */
    default InputStream getJsonInputStream() {
        return null;
    }

    /**
     * if you're transforming a java object, this will specify the "Jackson View" class
     * 
     * @return
     */
    default Class<? extends JacksonView> getJsonView() {
        return null;
    }

}
