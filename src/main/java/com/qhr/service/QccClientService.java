package com.qhr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.QccTaxCreateOrderRequest;

public interface QccClientService {

  /*企业模糊查询*/
  JsonNode getList(String searchKey);


    /**
     * 接口：企业财税数据
     * 1.数据下单
     * 2.验证码发送
     * 3.数据获取
     *
     * @param request 工商帐号密码
     * @return 产品匹配结果
     */
    JsonNode taxData(QccTaxCreateOrderRequest request);
}
