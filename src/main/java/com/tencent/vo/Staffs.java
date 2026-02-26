package com.tencent.vo;

/**
 * 用户列表。
 */
public record Staffs(
        /*用户ID*/
        String id,
        /*姓名*/
        String name,
        /*手机号*/
        String phone,
        /*角色*/
        String role,
        /*部门*/
        String department,
        /*账号状态*/
        String status,
        /*最近登录*/
        String lastLogin) {
}
