package controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("")
public class NetIdController {
  final String netId;
  public NetIdController(String netId) {
    this.netId = netId;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getRoot() {
    return netId;
  }

  @Path("/netid")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getNetId() {
    return netId;
  }
}
