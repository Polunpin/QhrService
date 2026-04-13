package com.qhr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.model.Enterprise;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.credit.EnterpriseCreditReportRaw;
import com.qhr.vo.credit.PersonalCreditReportRaw;
import com.qhr.vo.match.ApplicationContext;

/**
 * 将企业基础信息、财税数据、征信数据翻译成统一申请人画像。
 */
public interface ApplicantProfileAssembler {

    /**
     * 申请人画像-组装
     *
     * @param enterprise           企业基础信息
     * @param companyDetail        企业工商详情
     * @param taxData              财税数据
     * @param personalCreditReport 个人征信数据
     * @param enterpriseCreditData 企业征信数据
     * @param applicationContext
     * @return
     */
    ApplicantProfile assemble(Enterprise enterprise,
                              JsonNode companyDetail,
                              JsonNode taxData,
                              PersonalCreditReportRaw personalCreditReport,
                              EnterpriseCreditReportRaw enterpriseCreditData,
                              ApplicationContext applicationContext);
}
