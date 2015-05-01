package org.tdar.dataone.server.jibx;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

// http://www.javacodegeeks.com/2014/04/jibx-jersey2-integration.html
// https://xpapad.wordpress.com/2010/11/25/using-jibx-with-jersey/

@Provider
public class JIBXBodyReader implements MessageBodyReader<Object> {
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        try {
            BindingDirectory.getFactory(type);
        } catch (JiBXException e) {
            return false;
        }
        return true;
    }

    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        try {
            IBindingFactory factory = BindingDirectory.getFactory(type);
            IUnmarshallingContext context = factory.createUnmarshallingContext();
            return context.unmarshalDocument(entityStream, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}