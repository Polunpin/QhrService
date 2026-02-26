package com.tencent.vo;

/**
 * 用户列表。
 */
public record Users(
        /*用户ID*/
        String id,
        /*姓名*/
        String name,
        /*手机号*/
        String phone,
        /*关联企业*/
        String enterprises,
        /*匹配次数*/
        String matchCount,
        /*最近更新时间*/
        String lastMatch) {
}
