package com.qhr.controller.yewu;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.service.QccClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

/*企查查客户端*/
@ApplicationScoped
@Path("/api/qcc")
public class QccClientController {

    private final QccClientService qccClientService;

    public QccClientController(QccClientService qccClientService) {
        this.qccClientService = qccClientService;
    }


    /**
     * 模糊查询企业
     *
     * @param searchKey 企业关键字
     * @return qcc结果
     */
    @GET
    @Path("/getList")
    public ApiResponse getList(@QueryParam("searchKey") String searchKey) {
        JsonNode result = qccClientService.getList(searchKey);
        ApiAssert.isTrue(result != null && !result.isMissingNode(), ApiCode.NOT_FOUND, "企业不存在");
        return ApiResponse.ok(result);
    }
}
