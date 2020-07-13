package org.gengine.content.dropwizard.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gengine.content.dropwizard.views.NodeView;

@Path("/status")
@Produces(MediaType.TEXT_HTML)
public class NodeResource
{

    @GET
    @Produces(MediaType.TEXT_HTML)
    public NodeView getStatusView() {
        return new NodeView();
    }

}
