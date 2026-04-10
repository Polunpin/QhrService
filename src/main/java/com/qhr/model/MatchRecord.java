package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 匹配记录实体。
 */
@Data
@TableName(value = "yw_match_records", autoResultMap = true)
public class MatchRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    Long id;
    @TableField("user_open_id")
    String userOpenId;
    @TableField("enterprise_id")
    Long enterpriseId;
    @TableField("intention_id")
    Long intentionId;
    @TableField("amount_range")
    String amountRange;
    @TableField(value = "product_ids", typeHandler = Fastjson2TypeHandler.class)
    List<Long> productIds;
    @Deprecated
    @TableField("match_score")
    BigDecimal matchScore;
    @Deprecated
    @TableField("risk_type")
    String riskType;
    @Deprecated
    @TableField("risk_level")
    String riskLevel;
    @Deprecated
    String status;
    @TableField("created_at")
    LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    Integer deleted;

    public MatchRecord(String userOpenId, Long enterpriseId, Long intentionId, String amountRange, List<Long> productIds) {
        this.userOpenId = userOpenId;
        this.enterpriseId = enterpriseId;
        this.intentionId = intentionId;
        this.amountRange = amountRange;
        this.productIds = productIds;
    }
}
