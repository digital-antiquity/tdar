package org.tdar.dataone.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class GetCapabilities {

    
    
        @GET
        @Produces("application/xml")
        public String nodeinfo() {
            return "";
        }
     
        @GET
        @Path("node")
        @Produces("application/xml")
        public String getNodeInfo() {
            return nodeinfo();
        }

}
