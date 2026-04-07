package com.qhr.controller.yewu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiResponse;
import com.qhr.service.QccClientService;
import io.vertx.core.json.JsonArray;
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
    public ApiResponse getList(@QueryParam("searchKey") String searchKey) throws JsonProcessingException {
//        JsonNode result = qccClientService.getList(searchKey);
//        ApiAssert.isTrue(result != null && !result.isMissingNode(), ApiCode.NOT_FOUND, "企业不存在");
        //TODO 数据测试。
        String str1 = "{\"KeyNo\":\"0si5hyjhbliespkjb9vi5ias5askla5jl5\",\"Name\":\"北京护安行教育科技有限公司\",\"CreditCode\":\"91110108MADH4BC12G\",\"StartDate\":\"2024-04-02\",\"OperName\":\"兰一平\",\"Status\":\"存续\",\"No\":\"110108041266562\",\"Address\":\"北京市海淀区白家疃尚峰园2号楼3层302\"}";
        String str2 = "{\"KeyNo\":\"0si5hyjhbliespkjb9vi5ias5askla5jl5\",\"Name\":\"上海企会融科技有限公司\",\"CreditCode\":\"91110108MADH4BC13G\",\"StartDate\":\"2026-04-02\",\"OperName\":\"兰一平\",\"Status\":\"暂存\",\"No\":\"110108041266562\",\"Address\":\"北京市海淀区白家疃尚峰园2号楼3层302\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonArray result = new JsonArray().add(mapper.readTree(str1)).add(mapper.readTree(str2));
        return ApiResponse.ok(result);
    }
}
