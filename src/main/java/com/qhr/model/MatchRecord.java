package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.qhr.vo.match.ProductMatchReason;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 匹配记录实体。
 */
@Data
@TableName(value = "yw_match_records", autoResultMap = true)
public class MatchRecord implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    Long id;
    /** 用户 openId */
    @TableField("user_open_id")
    String userOpenId;
    /** 企业ID */
    @TableField("enterprise_id")
    Long enterpriseId;
    /** 融资意向ID */
    @TableField("intention_id")
    Long intentionId;
    /** 额度区间，当前仍由上游测算逻辑填充 */
    @TableField("amount_range")
    String amountRange;
    /** 明确命中的产品ID数组，仅保存 MATCH 产品 */
    @TableField(value = "product_ids", typeHandler = Fastjson2TypeHandler.class)
    List<Long> productIds;
    /**
     * REVIEW 原因列表，表示可做但需补件/确认/人工复核
     */
    @TableField(value = "review_reasons", typeHandler = Fastjson2TypeHandler.class)
    List<ProductMatchReason> reviewReasons;
    /**
     * 拒绝及数据缺失原因列表，用于回溯与诊断
     */
    @TableField(value = "reject_reasons", typeHandler = Fastjson2TypeHandler.class)
    List<ProductMatchReason> rejectReasons;
    /** 创建时间 */
    @TableField("created_at")
    LocalDateTime createdAt;
    /** 软删除标记 */
    @TableLogic(value = "0", delval = "1")
    Integer deleted;

    public MatchRecord() {
    }

    public MatchRecord(String userOpenId,
                       Long enterpriseId,
                       Long intentionId,
                       String amountRange,
                       List<Long> productIds,
                       List<ProductMatchReason> reviewReasons,
                       List<ProductMatchReason> rejectReasons) {
        this.userOpenId = userOpenId;
        this.enterpriseId = enterpriseId;
        this.intentionId = intentionId;
        this.amountRange = amountRange;
        this.productIds = productIds;
        this.reviewReasons = reviewReasons;
        this.rejectReasons = rejectReasons;
    }
}
