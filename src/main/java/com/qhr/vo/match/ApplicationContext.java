package com.qhr.vo.match;

import lombok.Data;

/**
 * 申请上下文。
 * 这类字段不是征信/财税事实，但会影响是否能直接进入申请动作。
 */
@Data
public class ApplicationContext {

    private String applyChannel;
    private String disbursementAccountType;
    private Boolean canProvideContractInvoiceReceipt;
    private Boolean acceptOnsiteInvestigation;
    private Boolean legalRepJointLiabilityAvailable;
    private Boolean shareholderJointLiabilityAvailable;
    private Boolean spouseJointLiabilityAvailable;
}
