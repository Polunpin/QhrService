package com.qhr.vo.match;

import lombok.Data;

/**
 * 申请上下文。
 * 这类字段不是征信/财税事实，但会影响是否能直接进入申请动作。
 */
@Data
public class ApplicationContext {

    /**
     * 本次申请采用的渠道，如线下、线上、扫码
     */
    private String applyChannel;
    /** 本次申请可使用的放款账户类型，如对公户、个人户 */
    private String disbursementAccountType;
    /** 是否可补充合同、发票、收据等申请资料 */
    private Boolean canProvideContractInvoiceReceipt;
    /** 是否接受下户实地调查 */
    private Boolean acceptOnsiteInvestigation;
    /** 法人是否可作为连带责任人 */
    private Boolean legalRepJointLiabilityAvailable;
    /** 股东是否可作为连带责任人 */
    private Boolean shareholderJointLiabilityAvailable;
    /** 配偶是否可作为连带责任人 */
    private Boolean spouseJointLiabilityAvailable;
}
