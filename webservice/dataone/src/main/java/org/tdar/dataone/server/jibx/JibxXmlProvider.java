package org.tdar.dataone.server.jibx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

@Provider
public class JibxXmlProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {

        if (!MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            return false;
        }

        try {
            BindingDirectory.getFactory(type);
        } catch (JiBXException e) {
            return false;
        }
        return true;
    }

    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {

        if (!MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            return false;
        }

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

    public long getSize(Object obj, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

}