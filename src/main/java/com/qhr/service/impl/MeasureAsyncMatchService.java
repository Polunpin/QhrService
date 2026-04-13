package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.MeasureAsyncMatchCommand;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.enums.MeasureProgressStage;
import com.qhr.model.Enterprise;
import com.qhr.model.MatchRecord;
import com.qhr.service.*;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.credit.EnterpriseCreditReportRaw;
import com.qhr.vo.credit.PersonalCreditReportRaw;
import com.qhr.vo.match.ApplicationContext;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测额异步执行器。
 * 负责串联企查查取数、画像构建、产品匹配和结果落库，并同步更新进度。
 */
@ApplicationScoped
public class MeasureAsyncMatchService {

    private static final System.Logger LOGGER = System.getLogger(MeasureAsyncMatchService.class.getName());

    private final QccClientService qccClientService;
    private final CreditReportParseService creditReportParseService;
    private final ApplicantProfileAssembler applicantProfileAssembler;
    private final ProductMatchService productMatchService;
    private final ExecutorService executorService;
    private final MatchRecordService matchRecordService;
    private final EnterpriseService enterpriseService;
    private final MeasureProgressService measureProgressService;

    public MeasureAsyncMatchService(QccClientService qccClientService,
                                    CreditReportParseService creditReportParseService,
                                    ApplicantProfileAssembler applicantProfileAssembler,
                                    ProductMatchService productMatchService,
                                    MatchRecordService matchRecordService,
                                    EnterpriseService enterpriseService,
                                    MeasureProgressService measureProgressService) {
        this.qccClientService = qccClientService;
        this.creditReportParseService = creditReportParseService;
        this.applicantProfileAssembler = applicantProfileAssembler;
        this.productMatchService = productMatchService;
        this.matchRecordService = matchRecordService;
        this.enterpriseService = enterpriseService;
        this.measureProgressService = measureProgressService;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 将测额任务提交到异步线程池执行。
     */
    public void trigger(MeasureSubmitRequest request, String openid, Long enterpriseId, Long intentionId) {
        //入参
        MeasureAsyncMatchCommand command = new MeasureAsyncMatchCommand(openid, enterpriseId, intentionId,
                request.personalCreditCloudId(), request.enterpriseCreditCloudId(),
                request.orderNo(), request.verifyCode(), request.dataStatus());
        //异步处理
        CompletableFuture.runAsync(() -> process(command), executorService)
                .exceptionally(exception -> {
                    measureProgressService.markFailed(intentionId, buildFailureRemark(exception));
                    LOGGER.log(System.Logger.Level.ERROR,
                            "测额提交流程异步匹配失败，融资需求-intentionId=" + intentionId + ", 用户-openid=" + openid,
                            exception);
                    return null;
                });
    }

    /**
     * 执行异步匹配主流程，并在关键节点写入阶段进度。
     */
    @SneakyThrows
    public void process(MeasureAsyncMatchCommand command) {
        //构建申请人画像
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.PROFILE_BUILDING, null);
        /*---- QCC逻辑处理 start-----*/
        //qcc-企业财税数据短信验证
        if ("P".equals(command.dataStatus())) {
            String status = qccClientService.sendCode(
                    command.orderNo(), command.verifyCode()).get("DataStatus").asText();
            //企业-更新qcc订单状态
            extracted(command.enterpriseId(), status, null);
        }

        Enterprise enterprise = enterpriseService.getById(command.enterpriseId());

        //qcc-获取企业财税数据
        JsonNode taxData = qccClientService.getTaxData(command.orderNo());
        //企业-更新qcc订单状态
        extracted(command.enterpriseId(), taxData.get("DataStatus").asText(), taxData.get("Data"));

        //qcc-获取企业工商详情
        JsonNode companyDetail = qccClientService.getInfo(enterprise.getCreditCode());
        /*---- QCC逻辑处理 end-----*/

        //个人征信
        PersonalCreditReportRaw personalInfo = creditReportParseService.parsePersonalCloudFile(command.personalCreditCloudId());
        //企业征信
        EnterpriseCreditReportRaw enterpriseCreditInfo = null;
        if (command.enterpriseCreditCloudId() != null && !command.enterpriseCreditCloudId().isBlank()) {
            enterpriseCreditInfo = creditReportParseService.parseEnterpriseCloudFile(command.enterpriseCreditCloudId());
        }

        ApplicationContext applicationContext = new ApplicationContext();
        //画像组装
        ApplicantProfile applicantProfile = applicantProfileAssembler.assemble(
                enterprise,
                companyDetail,
                taxData.path("Data"),
                personalInfo,
                enterpriseCreditInfo,
                applicationContext);
        //产品匹配
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.PRODUCT_MATCHING, null);
        var matchSummary = productMatchService.match(applicantProfile, applicationContext);

        LOGGER.log(System.Logger.Level.INFO,
                "申请人画像构建完成，enterpriseId=" + command.enterpriseId()
                        + ", companyName=" + applicantProfile.getCompanyName()
                        + ", region=" + applicantProfile.getCompanyRegion()
                        + ", legalShareRatio=" + applicantProfile.getLegalPersonShareRatio()
                        + ", tax12m=" + applicantProfile.getTaxAmount12m()
                        + ", invoice12m=" + applicantProfile.getInvoiceAmount12m()
                        + ", creditInquiry6m=" + applicantProfile.getCreditInquiryCount()
                        + ", matchedProducts=" + matchSummary.getProductIds().size()
                        + ", reviewReasons=" + matchSummary.getReviewReasons().size()
                        + ", rejectReasons=" + matchSummary.getRejectReasons().size());

        //保存匹配结果
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.RECOMMENDATION_GENERATING, null);
        matchRecordService.create(
                new MatchRecord(command.openid(), command.enterpriseId(), command.intentionId(),
                        "待测算",
                        matchSummary.getProductIds(),
                        matchSummary.getReviewReasons(),
                        matchSummary.getRejectReasons()));
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.COMPLETED, null);

        LOGGER.log(System.Logger.Level.INFO,
                "测额异步匹配完成，enterpriseId=" + command.enterpriseId() + ", intentionId=" + command.intentionId());
    }

    /**
     * 提取最内层异常信息，生成可供前端展示的失败说明。
     */
    private String buildFailureRemark(Throwable exception) {
        Throwable root = exception;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return MeasureProgressStage.FAILED.description();
        }
        return "处理失败：" + message;
    }


    /**
     * 更新企查查订单状态并保存取回的财税数据。
     */
    private void extracted(Long enterpriseId, String status, JsonNode data) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setQccDataStatus(status);
        enterprise.setQccTaxData(data);
        enterpriseService.update(enterprise);
    }

    /**
     * 应用关闭时，把这个线程池优雅地关掉。
     */
    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }
}
