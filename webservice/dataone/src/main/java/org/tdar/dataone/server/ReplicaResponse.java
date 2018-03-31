package org.tdar.dataone.server;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.dataone.service.types.v1.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path(AbstractDataOneResponse.BASE_PATH + "replica")
@Component
@Scope("prototype")
public class ReplicaResponse extends AbstractDataOneResponse {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(APPLICATION_XML)
    @Path("{pid:.*}")
    public Response replica(@PathParam("pid") String id) {
        setupResponseContext(response, request);
        return constructObjectResponse(id, request, Event.REPLICATE);
    }

}
