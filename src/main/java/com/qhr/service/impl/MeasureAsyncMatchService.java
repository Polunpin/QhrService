package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.MeasureAsyncMatchCommand;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.enums.MeasureProgressStage;
import com.qhr.model.Enterprise;
import com.qhr.model.MatchRecord;
import com.qhr.service.*;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.credit.PersonalCreditReportRaw;
import com.qhr.vo.match.ApplicationContext;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

import java.util.List;
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
    private final DmnDecisionService dmnDecisionService;
    private final ExecutorService executorService;
    private final MatchRecordService matchRecordService;
    private final EnterpriseService enterpriseService;
    private final MeasureProgressService measureProgressService;

    public MeasureAsyncMatchService(QccClientService qccClientService,
                                    CreditReportParseService creditReportParseService,
                                    ApplicantProfileAssembler applicantProfileAssembler,
                                    DmnDecisionService dmnDecisionService,
                                    MatchRecordService matchRecordService,
                                    EnterpriseService enterpriseService,
                                    MeasureProgressService measureProgressService) {
        this.qccClientService = qccClientService;
        this.creditReportParseService = creditReportParseService;
        this.applicantProfileAssembler = applicantProfileAssembler;
        this.dmnDecisionService = dmnDecisionService;
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
        //qcc-企业财税数据
        if ("P".equals(command.dataStatus())) {
            String status = qccClientService.sendCode(
                    command.orderNo(), command.verifyCode()).get("DataStatus").asText();
            //企业-更新qcc订单状态
            extracted(command.enterpriseId(), status);
        }
        //获取企业财税数据
        JsonNode taxData = qccClientService.getTaxData(command.orderNo());
        //企业-更新qcc订单状态
        extracted(command.enterpriseId(), taxData.get("DataStatus").asText(), taxData.get("Data"));
        //构建申请人画像
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.PROFILE_BUILDING, null);
        PersonalCreditReportRaw personalCreditReport = null;
        if (command.personalCreditCloudId() != null && !command.personalCreditCloudId().isBlank()) {
            personalCreditReport = creditReportParseService.parsePersonalCloudFile(
                    command.personalCreditCloudId());
        }
        Enterprise enterprise = enterpriseService.getById(command.enterpriseId());
        ApplicantProfile applicantProfile = applicantProfileAssembler.assemble(
                enterprise,
                taxData.path("Data"),
                personalCreditReport,
                null,
                new ApplicationContext());
        //DMN产品匹配
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.PRODUCT_MATCHING, null);
        //Object matchResult = dmnDecisionService.match(applicantProfile);
        //TODO-数据模拟测试
        List<Long> list = List.of(1L, 2L, 4L);

        LOGGER.log(System.Logger.Level.INFO,
                "申请人画像构建完成，enterpriseId=" + command.enterpriseId()
                        + ", companyName=" + applicantProfile.getCompanyName()
                        + ", tax12m=" + applicantProfile.getTaxAmount12m()
                        + ", invoice12m=" + applicantProfile.getInvoiceAmount12m()
                        + ", creditInquiry6m=" + applicantProfile.getCreditInquiryCount());

        //保存匹配结果
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.RECOMMENDATION_GENERATING, null);
        matchRecordService.create(
                new MatchRecord(command.openid(), command.enterpriseId(), command.intentionId(), "500-800万", list));
        measureProgressService.markStage(command.intentionId(), MeasureProgressStage.COMPLETED, null);


//        LOGGER.log(System.Logger.Level.INFO,
//                "测额异步匹配完成，enterpriseId=" + command.enterpriseId()
//                        + ", intentionId=" + command.intentionId()
//                        + ", matchResultType=" + (matchResult == null ? "null" : matchResult.getClass().getSimpleName()));
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
     * 更新企查查订单状态。
     */
    private void extracted(Long enterpriseId, String status) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setQccDataStatus(status);
        enterpriseService.update(enterprise);
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

//    private ApplicantProfile buildApplicantProfile(MeasureAsyncMatchCommand command, JsonNode qccTaxOrder) {
//        ApplicantProfile profile = new ApplicantProfile();
//        profile.setCompanyAge(calculateCompanyAge(profile.getEstablishDate()));

    // 当前 DMN 规则至少依赖这两个字段；企查查财税下单结果尚未进入“数据获取”阶段时，先用默认值跑通异步匹配链路。
//        profile.setLegalPersonShareRatio(extractDecimal(qccTaxOrder, "legalPersonShareRatio", BigDecimal.ZERO));
//        profile.setLegalPersonChangeCount(extractInteger(qccTaxOrder, "legalPersonChangeCount", 0));
//        return profile;
//    }

    /**
     * 应用关闭时，把这个线程池优雅地关掉。
     */
    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }
}
