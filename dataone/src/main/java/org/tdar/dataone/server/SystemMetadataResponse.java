package org.tdar.dataone.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.dataone.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.dataone.service.DataOneService;

@Path(AbstractDataOneResponse.BASE_PATH + "meta")
@Component
@Scope("prototype")
public class SystemMetadataResponse extends AbstractDataOneResponse {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataOneService service;
    /*
     * Raises:
     * Exceptions.NotImplemented –
     * (errorCode=501, detailCode=2041)
     * Ping is a required operation and so an operational member node should never return this exception unless under development.
     * 
     * Exceptions.ServiceFailure –
     * (errorCode=500, detailCode=2042)
     * 
     * A ServiceFailure exception indicates that the node is not currently operational as a member node. A coordinating node or monitoring service may use this
     * as an indication that the member node should be taken out of the pool of active nodes, though ping should be called on a regular basis to determine when
     * the node might b ready to resume normal operations.
     * 
     * Exceptions.InsufficientResources –
     * (errorCode=413, detailCode=2045)
     * 
     * A ping response may return InsufficientResources if for example the system is in a state where normal DataONE operations may be impeded by an unusually
     * high load on the node.
     */

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("{id:.*}")
    @Produces(APPLICATION_XML)
    public Response meta(@PathParam("id") String id) {
        setupResponseContext(response, request);
        if (StringUtils.isBlank(id)) {
            return Response.serverError().entity(getNotFoundError()).status(Status.NOT_FOUND).build();
        }
        try {
            service.checkForChecksumConflict(id);
            SystemMetadata metadataRequest = service.metadataRequest(id);
            if (metadataRequest == null) {
                return Response.serverError().entity(getNotFoundError()).status(Status.NOT_FOUND).build();
            }
            return Response.ok(metadataRequest).build();
        } catch (Exception e) {
            logger.error("error in metadataResponse: {}", e, e);
            return Response.serverError().entity(getNotFoundError()).status(Status.NOT_FOUND).build();
        }
    }

}
