package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配记录实体。
 */
@Data
@TableName("z_match_records")
public class MatchRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    Long id;
    @TableField("user_open_id")
    String userOpenId;
    @TableField("enterprise_id")
    Long enterpriseId;
    @TableField("intention_id")
    Long intentionId;
    @TableField("product_ids")
    String productIds;
    @TableField("match_score")
    BigDecimal matchScore;
    @TableField("risk_type")
    String riskType;
    @TableField("risk_level")
    String riskLevel;
    String status;
    @TableField("created_at")
    LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    Integer deleted;
}
