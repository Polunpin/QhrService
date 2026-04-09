package com.qhr.service;

import com.qhr.dto.EnterprisePayload;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.vo.PrecheckResult;

public interface MeasureService {

  /**
   * 预审
   *
   * @param request 企业信息
   * @param openid  用户openid
   * @param unionid 用户unionid
   * @return 企业预审结果
   */
  PrecheckResult precheck(EnterprisePayload request, String openid, String unionid);

  String submit(MeasureSubmitRequest request, String openid, String unionid);

}
