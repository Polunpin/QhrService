package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.EnterprisePayload;
import com.qhr.dto.MeasureAsyncMatchCommand;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.dto.QccTaxCreateOrderRequest;
import com.qhr.service.DmnDecisionService;
import com.qhr.service.QccClientService;
import com.qhr.vo.ApplicantProfile;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MeasureAsyncMatchService {

    private static final System.Logger LOGGER = System.getLogger(MeasureAsyncMatchService.class.getName());

    private final QccClientService qccClientService;
    private final DmnDecisionService dmnDecisionService;
    private final ExecutorService executorService;

    public MeasureAsyncMatchService(QccClientService qccClientService,
                                    DmnDecisionService dmnDecisionService) {
        this.qccClientService = qccClientService;
        this.dmnDecisionService = dmnDecisionService;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void trigger(MeasureSubmitRequest request, String openid, Long enterpriseId, Long intentionId) {
        //入参
        MeasureAsyncMatchCommand command = new MeasureAsyncMatchCommand(
                openid, enterpriseId, intentionId, request.taxAccount(),
                request.taxPassword(), request.verifyCode(), request.enterprise()
        );
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
        Thread.sleep(3000);
        LOGGER.log(System.Logger.Level.INFO, "测额异步匹配完成(Test)=" + command);
//        //qcc-企业财税数据
//        JsonNode qccTaxOrder = createQccTaxOrder(command);
//        //构建申请人画像
//        ApplicantProfile applicantProfile = buildApplicantProfile(command, qccTaxOrder);
//        //DMN产品匹配
//        Object matchResult = dmnDecisionService.match(applicantProfile);

//        LOGGER.log(System.Logger.Level.INFO,
//                "测额异步匹配完成，enterpriseId=" + command.enterpriseId()
//                        + ", intentionId=" + command.intentionId()
//                        + ", matchResultType=" + (matchResult == null ? "null" : matchResult.getClass().getSimpleName()));
    }

    private JsonNode createQccTaxOrder(MeasureAsyncMatchCommand command) {
        try {
            JsonNode result = qccClientService.taxData(
                    new QccTaxCreateOrderRequest(
                            command.enterprise().creditCode(),
                            command.taxAccount(),
                            command.taxPassword(),
                            command.verifyCode()
                    )
            );
            LOGGER.log(System.Logger.Level.INFO,
                    "企查查财税数据流程完成，openid=" + command.openid() + ", searchKey=" + command.enterprise().creditCode());
            return result;
        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.ERROR,
                    "企查查财税数据流程失败，openid=" + command.openid() + ", searchKey=" + command.enterprise().creditCode(),
                    exception);
            return null;
        }
    }

    private ApplicantProfile buildApplicantProfile(MeasureAsyncMatchCommand command, JsonNode qccTaxOrder) {
        EnterprisePayload enterprise = command.enterprise();
        ApplicantProfile profile = new ApplicantProfile();
        profile.setCompanyName(enterprise.name());
        profile.setRegisterAddress(enterprise.address());
        profile.setEstablishDate(LocalDate.parse(enterprise.startDate()));
//        profile.setCompanyAge(calculateCompanyAge(profile.getEstablishDate()));

        // 当前 DMN 规则至少依赖这两个字段；企查查财税下单结果尚未进入“数据获取”阶段时，先用默认值跑通异步匹配链路。
//        profile.setLegalPersonShareRatio(extractDecimal(qccTaxOrder, "legalPersonShareRatio", BigDecimal.ZERO));
//        profile.setLegalPersonChangeCount(extractInteger(qccTaxOrder, "legalPersonChangeCount", 0));
        return profile;
    }

    /**
     * 应用关闭时，把这个线程池优雅地关掉。
     */
    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }
}
