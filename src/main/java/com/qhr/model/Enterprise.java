package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业实体。
 */
@Data
@TableName("jc_enterprise")
public class Enterprise implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /*企业名称*/
    private String name;
    /*统一社会信用代码*/
    @TableField("credit_code")
    private String creditCode;
    /*成立日期*/
    @TableField("start_date")
    private String startDate;
    /*法定代表人姓名*/
    @TableField("oper_name")
    private String operName;
    /*状态*/
    private String status;
    /*注册地址*/
    private String address;
    /*QCC财税订单号*/
    @TableField("qcc_order_no")
    private String qccOrderNo;
    /*QCC财税数据状态*/
    @TableField("qcc_data_status")
    private String qccDataStatus;
    /*QCC财税数据JSON*/
    @TableField("qcc_tax_data")
    private String qccTaxData;
    /*QCC财税订单过期时间*/
    @TableField("qcc_order_expire_at")
    private LocalDateTime qccOrderExpireAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
