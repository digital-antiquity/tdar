package org.tdar.dataone.server.jibx;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

// http://www.javacodegeeks.com/2014/04/jibx-jersey2-integration.html
// https://xpapad.wordpress.com/2010/11/25/using-jibx-with-jersey/

@Provider
public class JIBXBodyWriter implements MessageBodyWriter<Object> {
    public long getSize(Object obj, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        try {
            BindingDirectory.getFactory(type);
        } catch (JiBXException e) {
            return false;
        }
        return true;
    }

    public void writeTo(Object obj, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream outputStream)
            throws IOException, WebApplicationException {
        try {
            IBindingFactory factory = BindingDirectory.getFactory(type);
            IMarshallingContext context = factory.createMarshallingContext();
            context.marshalDocument(obj, "UTF-8", null, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}