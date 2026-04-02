package com.qhr.controller.yewu;

import com.qhr.config.ApiResponse;
import com.qhr.service.HomeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/* 小程序：首页、提额查询、我的 */
@ApplicationScoped
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GET
    @Path("/home")
    public ApiResponse home(@Context HttpHeaders headers,
                            @QueryParam("enterpriseId") Long enterpriseId) {
        return ApiResponse.ok(homeService.home(headers.getHeaderString("x-wx-openid"), enterpriseId));
    }

    @GET
    @Path("/increase")
    public ApiResponse increase(@Context HttpHeaders headers) {
        return ApiResponse.ok(homeService.increase(headers.getHeaderString("x-wx-openid")));
    }

    @GET
    @Path("/mine")
    public ApiResponse mine(@Context HttpHeaders headers,
                            @QueryParam("enterpriseId") Long enterpriseId) {
        return ApiResponse.ok(homeService.mine(headers.getHeaderString("x-wx-openid"), enterpriseId));
    }


}
