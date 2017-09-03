package controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("/netid")
public class NetIdController {
  final String netId;
  public NetIdController(String netId) {
    this.netId = netId;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getNetId() {
    return netId;
  }
}
