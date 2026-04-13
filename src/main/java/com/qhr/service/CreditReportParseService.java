package com.qhr.service;

import com.qhr.vo.credit.EnterpriseCreditReportRaw;
import com.qhr.vo.credit.PersonalCreditReportRaw;

/**
 * 征信报告解析服务。
 */
public interface CreditReportParseService {

    /**
     * 直接解析个人征信 PDF 字节流。
     */
    PersonalCreditReportRaw parsePersonalPdf(byte[] pdfBytes);

    /**
     * 通过云文件 fileId 下载个人征信 PDF，解析完成后可按需删除远端文件。
     */
    PersonalCreditReportRaw parsePersonalCloudFile(String fileId);

    /**
     * 直接解析企业征信 PDF 字节流。
     */
    EnterpriseCreditReportRaw parseEnterprisePdf(byte[] pdfBytes);

    /**
     * 通过云文件 fileId 下载企业征信 PDF，解析完成后可按需删除远端文件。
     */
    EnterpriseCreditReportRaw parseEnterpriseCloudFile(String fileId);
}
