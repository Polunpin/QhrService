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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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
        MeasureAsyncMatchCommand command = new MeasureAsyncMatchCommand(
                openid,
                enterpriseId,
                intentionId,
                request.spouseSupport(),
                request.taxAccount(),
                request.taxPassword(),
                request.verifyCode(),
                request.enterprise()
        );
        CompletableFuture.runAsync(() -> process(command), executorService)
                .exceptionally(exception -> {
                    LOGGER.log(System.Logger.Level.ERROR,
                            "测额提交流程异步匹配失败，enterpriseId=" + enterpriseId + ", openid=" + openid,
                            exception);
                    return null;
                });
    }

    private void process(MeasureAsyncMatchCommand command) {
        JsonNode qccTaxOrder = createQccTaxOrder(command);
        ApplicantProfile applicantProfile = buildApplicantProfile(command, qccTaxOrder);
        Object matchResult = dmnDecisionService.match(applicantProfile);
        LOGGER.log(System.Logger.Level.INFO,
                "测额异步匹配完成，enterpriseId=" + command.enterpriseId()
                        + ", intentionId=" + command.intentionId()
                        + ", matchResultType=" + (matchResult == null ? "null" : matchResult.getClass().getSimpleName()));
    }

    private JsonNode createQccTaxOrder(MeasureAsyncMatchCommand command) {
        if (!hasText(command.taxAccount()) || !hasText(command.taxPassword()) || !hasText(command.verifyCode())) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "跳过企查查财税数据获取，税务账号、密码或验证码为空，enterpriseId=" + command.enterpriseId());
            return null;
        }

        String searchKey = resolveSearchKey(command.enterprise());
        if (searchKey == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "跳过企查查财税下单，企业信用代码和名称均为空，enterpriseId=" + command.enterpriseId());
            return null;
        }

        try {
            JsonNode result = qccClientService.taxData(
                    new QccTaxCreateOrderRequest(
                            searchKey,
                            command.taxAccount(),
                            command.taxPassword(),
                            command.verifyCode()
                    )
            );
            LOGGER.log(System.Logger.Level.INFO,
                    "企查查财税数据流程完成，enterpriseId=" + command.enterpriseId() + ", searchKey=" + searchKey);
            return result;
        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.ERROR,
                    "企查查财税数据流程失败，enterpriseId=" + command.enterpriseId() + ", searchKey=" + searchKey,
                    exception);
            return null;
        }
    }

    private ApplicantProfile buildApplicantProfile(MeasureAsyncMatchCommand command, JsonNode qccTaxOrder) {
        EnterprisePayload enterprise = command.enterprise();
        ApplicantProfile profile = new ApplicantProfile();
        profile.setCompanyName(enterprise == null ? null : enterprise.name());
        profile.setRegisterAddress(enterprise == null ? null : enterprise.address());
        profile.setEstablishDate(parseDate(enterprise == null ? null : enterprise.startDate()));
        profile.setCompanyAge(calculateCompanyAge(profile.getEstablishDate()));
        profile.setSpouseGuarantee(Boolean.TRUE.equals(command.spouseSupport()));

        // 当前 DMN 规则至少依赖这两个字段；企查查财税下单结果尚未进入“数据获取”阶段时，先用默认值跑通异步匹配链路。
        profile.setLegalPersonShareRatio(extractDecimal(qccTaxOrder, "legalPersonShareRatio", BigDecimal.ZERO));
        profile.setLegalPersonChangeCount(extractInteger(qccTaxOrder, "legalPersonChangeCount", 0));
        return profile;
    }

    private String resolveSearchKey(EnterprisePayload enterprise) {
        if (enterprise == null) {
            return null;
        }
        if (hasText(enterprise.creditCode())) {
            return enterprise.creditCode();
        }
        if (hasText(enterprise.name())) {
            return enterprise.name();
        }
        return null;
    }

    private LocalDate parseDate(String startDate) {
        if (!hasText(startDate)) {
            return null;
        }
        try {
            return LocalDate.parse(startDate);
        } catch (DateTimeParseException exception) {
            LOGGER.log(System.Logger.Level.WARNING, "企业成立日期格式不正确，startDate=" + startDate, exception);
            return null;
        }
    }

    private Integer calculateCompanyAge(LocalDate establishDate) {
        if (establishDate == null) {
            return null;
        }
        long years = ChronoUnit.YEARS.between(establishDate, LocalDate.now());
        return (int) Math.max(years, 0);
    }

    private BigDecimal extractDecimal(JsonNode source, String fieldName, BigDecimal defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        JsonNode current = getTaxDataNode(source).path(fieldName);
        if (current.isMissingNode() || current.isNull() || !current.isNumber()) {
            return defaultValue;
        }
        return current.decimalValue();
    }

    private Integer extractInteger(JsonNode source, String fieldName, Integer defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        JsonNode current = getTaxDataNode(source).path(fieldName);
        if (current.isMissingNode() || current.isNull() || !current.canConvertToInt()) {
            return defaultValue;
        }
        return current.intValue();
    }

    private JsonNode getTaxDataNode(JsonNode source) {
        JsonNode result = source.path("Result");
        JsonNode data = result.path("Data");
        return data.isMissingNode() || data.isNull() ? result : data;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }
}
