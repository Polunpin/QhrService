package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.MeasureAsyncMatchCommand;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.model.Enterprise;
import com.qhr.model.MatchRecord;
import com.qhr.service.DmnDecisionService;
import com.qhr.service.EnterpriseService;
import com.qhr.service.MatchRecordService;
import com.qhr.service.QccClientService;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MeasureAsyncMatchService {

    private static final System.Logger LOGGER = System.getLogger(MeasureAsyncMatchService.class.getName());

    private final QccClientService qccClientService;
    private final DmnDecisionService dmnDecisionService;
    private final ExecutorService executorService;
    private final MatchRecordService matchRecordService;
    private final EnterpriseService enterpriseService;

    public MeasureAsyncMatchService(QccClientService qccClientService,
                                    DmnDecisionService dmnDecisionService,
                                    MatchRecordService matchRecordService, EnterpriseService enterpriseService) {
        this.qccClientService = qccClientService;
        this.dmnDecisionService = dmnDecisionService;
        this.matchRecordService = matchRecordService;
        this.enterpriseService = enterpriseService;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void trigger(MeasureSubmitRequest request, String openid, Long enterpriseId, Long intentionId) {
        //入参
        MeasureAsyncMatchCommand command = new MeasureAsyncMatchCommand(openid, enterpriseId, intentionId,
                request.orderNo(), request.verifyCode(), request.dataStatus());
        //异步处理
        CompletableFuture.runAsync(() -> process(command), executorService)
                .exceptionally(exception -> {
                    LOGGER.log(System.Logger.Level.ERROR,
                            "测额提交流程异步匹配失败，融资需求-intentionId=" + intentionId + ", 用户-openid=" + openid,
                            exception);
                    return null;
                });
    }

    @SneakyThrows
    private void process(MeasureAsyncMatchCommand command) {
        //qcc-企业财税数据
        if (command.dataStatus().equals("P")) {
            String status = qccClientService.sendCode(
                    command.orderNo(), command.verifyCode()).get("data").get("DataStatus").asText();
            //企业-更新qcc订单状态
            extracted(command.enterpriseId(), status);
        }
        //获取企业财税数据
        JsonNode taxData = qccClientService.getTaxData(command.orderNo());
        //企业-更新qcc订单状态
        extracted(command.enterpriseId(), taxData.get("DataStatus").asText(), taxData.get("Data").asText());
        //构建申请人画像
//            ApplicantProfile applicantProfile = buildApplicantProfile(command, taxData);
        //DMN产品匹配
//            Object matchResult = dmnDecisionService.match(applicantProfile);
        //TODO-数据模拟测试
        List<Long> list = List.of(1L, 2L, 4L);

        //保存匹配结果
        matchRecordService.create(
                new MatchRecord(command.openid(), command.enterpriseId(), command.intentionId(), list));


//        LOGGER.log(System.Logger.Level.INFO,
//                "测额异步匹配完成，enterpriseId=" + command.enterpriseId()
//                        + ", intentionId=" + command.intentionId()
//                        + ", matchResultType=" + (matchResult == null ? "null" : matchResult.getClass().getSimpleName()));
    }


    private void extracted(Long enterpriseId, String status) {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(enterpriseId);
        enterprise.setQccDataStatus(status);
        enterpriseService.update(enterprise);
    }

    private void extracted(Long enterpriseId, String status, String data) {
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
