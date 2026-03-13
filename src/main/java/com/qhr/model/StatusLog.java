package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务流转日志实体。
 */
public record StatusLog(Long id,
                        Long orderId,
                        String operatorType,
                        Long operatorId,
                        String preStage,
                        String postStage,
                        String remark,
                        LocalDateTime createdAt) implements Serializable, WithId<StatusLog> {

  /** 复制并替换id。 */
  public StatusLog withId(Long id) {
    return new StatusLog(id, orderId, operatorType, operatorId, preStage, postStage, remark, createdAt);
  }
}
