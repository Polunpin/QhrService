package com.qhr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.dto.QccTaxCreateOrderRequest;

public interface QccClientService {

    /*企业模糊查询*/
    JsonNode getList(String searchKey);

    /**
     * 企业财税数据
     * 1.数据下单
     * 逻辑：下单状态（P-已发送验证码，需要下一步操作，S-下单成功，F-下单失败）
     *
     * @param qccTaxCreateOrder qcc接口入参
     * @return qcc下单结果，确认是否需要验证短信
     */
    JsonNode createTaxOrder(QccTaxCreateOrderRequest qccTaxCreateOrder);

    /**
     * 2.验证码发送
     * 确认下单（有些地区税务不需要验证码）
     *
     * @param orderNo qcc订单号
     * @param verifyCode 税务验证码
     * @return 订单结果
     */
    JsonNode sendCode(String orderNo, String verifyCode);

    /**
     * 3.数据获取
     *
     * @param orderNo qcc订单号
     * @return 数据
     */
    JsonNode getTaxData(String orderNo);

}
