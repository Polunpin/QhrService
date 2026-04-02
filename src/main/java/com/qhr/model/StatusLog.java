package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务流转日志实体。
 */
@Data
@TableName("yw_status_logs")
public class StatusLog implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;
  @TableField("order_id")
  private Long orderId;
  @TableField("operator_type")
  private String operatorType;
  @TableField("operator_id")
  private Long operatorId;
  @TableField("pre_stage")
  private String preStage;
  @TableField("post_stage")
  private String postStage;
  private String remark;
  @TableField("created_at")
  private LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
