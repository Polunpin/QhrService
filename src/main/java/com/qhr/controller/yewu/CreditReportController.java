package com.qhr.controller.yewu;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.service.CreditReportParseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * 征信报告解析入口。
 * 既支持直接传 PDF 内容，也支持通过微信云托管 fileId 拉取文件后解析。
 */
@ApplicationScoped
@Path("/api/credit-report")
@Produces(MediaType.APPLICATION_JSON)
public class CreditReportController {

    private final CreditReportParseService creditReportParseService;

    public CreditReportController(CreditReportParseService creditReportParseService) {
        this.creditReportParseService = creditReportParseService;
    }

    /**
     * 调试入口：直接接收 PDF 二进制并解析个人征信报告。
     */
    @POST
    @Path("/personal/parse")
    public ApiResponse parsePersonalPdf(byte[] pdfBytes) {
        ApiAssert.isTrue(pdfBytes != null && pdfBytes.length > 0, ApiCode.BAD_REQUEST, "PDF内容不能为空");
        return ApiResponse.ok(creditReportParseService.parsePersonalPdf(pdfBytes));
    }

    /**
     * 正式入口：根据前端传入的云文件 fileId 下载 PDF，解析后按配置决定是否删除远端文件。
     */
    @POST
    @Path("/personal/parse-cloud-file")
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse parsePersonalCloudFile(@QueryParam("fileId") String fileId) {
        ApiAssert.notNull(fileId, ApiCode.BAD_REQUEST, "请求体不能为空");
        return ApiResponse.ok(creditReportParseService.parsePersonalCloudFile(fileId));
    }

    /**
     * 调试入口：直接接收 PDF 二进制并解析企业征信报告。
     */
    @POST
    @Path("/enterprise/parse")
    public ApiResponse parseEnterprisePdf(byte[] pdfBytes) {
        ApiAssert.isTrue(pdfBytes != null && pdfBytes.length > 0, ApiCode.BAD_REQUEST, "PDF内容不能为空");
        return ApiResponse.ok(creditReportParseService.parseEnterprisePdf(pdfBytes));
    }

    /**
     * 正式入口：根据前端传入的云文件 fileId 下载 PDF，解析企业征信报告。
     */
    @POST
    @Path("/enterprise/parse-cloud-file")
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse parseEnterpriseCloudFile(@QueryParam("fileId") String fileId) {
        ApiAssert.notNull(fileId, ApiCode.BAD_REQUEST, "请求体不能为空");
        return ApiResponse.ok(creditReportParseService.parseEnterpriseCloudFile(fileId));
    }
}
