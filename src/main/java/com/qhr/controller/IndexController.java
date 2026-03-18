package com.qhr.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexController {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String index() {
    return "qhr-api is running";
  }
}
