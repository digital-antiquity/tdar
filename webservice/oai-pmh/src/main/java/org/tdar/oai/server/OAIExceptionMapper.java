package org.tdar.oai.server;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class OAIExceptionMapper implements ExceptionMapper<Exception> {
    private transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(Exception e) {
        logger.error("{}",e,e);
        return Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_XML)
                .entity(e.getCause())
                .build();
    }

}
