package org.tdar.dataone.server;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path(AbstractDataOneResponse.BASE_PATH + "error")
@Component
@Scope("prototype")
public class SynchronizationFailedResponse extends AbstractDataOneResponse {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    
    @POST
    @Produces("text/plain")
    @Consumes("application/xml")
    public Response synchronizationFailed(@QueryParam("session") String session, String message) {
        setupResponseContext(response, request);
        logger.debug(session + ": " + message);
        return Response.ok().build();
    }

}
