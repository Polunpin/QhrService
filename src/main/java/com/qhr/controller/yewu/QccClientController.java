package com.qhr.controller.yewu;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.dto.QccTaxCreateOrderRequest;
import com.qhr.service.QccClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/*企查查客户端*/
@ApplicationScoped
@Path("/api/qcc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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

    /**
     * 企业工商详情
     *
     * @param searchKey 统一社会信用代码
     * @return qcc结果
     */
    @GET
    @Path("/getInfo")
    public ApiResponse getInfo(@QueryParam("searchKey") String searchKey) {
        JsonNode result = qccClientService.getInfo(searchKey);
        return ApiResponse.ok(result);
    }

    /**
     * 财税步骤1:数据下单（获取验证码）
     * 逻辑：下单状态（P-已发送验证码，需要下一步操作，S-下单成功，F-下单失败）
     *
     * @param orderRequest qcc接口入参
     * @return qcc结果
     */
    @POST
    @Path("/createOrder")
    public ApiResponse createOrder(QccTaxCreateOrderRequest orderRequest) {
        JsonNode result = qccClientService.createTaxOrder(orderRequest);
        return ApiResponse.ok(result);
    }

    /**
     * 财税步骤2:验证码校验
     *
     * @param orderNo    qcc订单号
     * @param verifyCode 税务验证码
     * @return 订单结果
     */
    @GET
    @Path("/sendCode")
    public ApiResponse sendCode(@QueryParam("orderNo") String orderNo,
                                @QueryParam("verifyCode") String verifyCode) {
        JsonNode result = qccClientService.sendCode(orderNo, verifyCode);
        return ApiResponse.ok(result);
    }

    /**
     * 财税步骤3:数据获取
     *
     * @param orderNo qcc订单号
     * @return 财税数据
     */
    @GET
    @Path("/taxData")
    public ApiResponse taxData(@QueryParam("orderNo") String orderNo) {
        JsonNode result = qccClientService.getTaxData(orderNo);
        return ApiResponse.ok(result);
    }
}
