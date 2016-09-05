package org.tdar.dataone.server;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.SerializationService;
import org.tdar.dataone.service.DataOneService;

@Component
@Scope("prototype")
@Path(AbstractDataOneResponse.BASE_PATH)
public class CapabilitiesResponse extends AbstractDataOneResponse {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SerializationService serialize;

    @Autowired
    private DataOneService service;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(APPLICATION_XML)
    public Response nodeinfo() {
        setupResponseContext(response, request);
        try {
            logger.debug("d1s: {}, ss: {}", serialize, service);
            return Response.ok().entity(service.getNodeResponse()).build();
        } catch (Exception e) {
            logger.error("error in nodeInfo: {}", e, e);
        }

        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("/node")
    @Produces(APPLICATION_XML)
    public Response getNodeInfo() {
        return nodeinfo();
    }

}
