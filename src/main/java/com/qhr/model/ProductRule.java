package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.qhr.vo.match.AmountStrategyPayload;
import com.qhr.vo.match.CandidateFilterPayload;
import com.qhr.vo.match.DiagnosisRulePayload;
import com.qhr.vo.match.MatchRulePayload;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品规则主表。
 * 采用 payload-first 设计：主表保留规则身份、版本、生命周期与 4 个语义化 payload。
 */
@Data
@TableName(value = "yw_product_rule", autoResultMap = true)
public class ProductRule implements Serializable {

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private Long id;
  /**
   * 关联产品ID，对应 jc_products.id
   */
  private Long productId;
  /**
   * 稳定规则编码，跨环境同步和版本升级时用于唯一标识规则
   */
  private String ruleCode;
  /**
   * 规则版本号，同一产品允许存在多版规则
   */
  private Integer ruleVersion;
  /**
   * 规则名称，便于后台展示和人工识别
   */
  private String ruleName;
  /**
   * 规则状态，如 DRAFT/ACTIVE/INACTIVE/ARCHIVED
   */
  private String ruleStatus;
  /**
   * 规则优先级，值越小越优先参与匹配
   */
  private Integer priority;
  /**
   * 规则生效开始时间，null 表示立即可用
   */
  private LocalDateTime effectiveStartTime;
  /**
   * 规则生效结束时间，null 表示长期有效
   */
  private LocalDateTime effectiveEndTime;

  /**
   * 候选粗筛 payload，放地区、年龄、成立时长、行业、申请方式等前置过滤条件
   */
  @TableField(value = "candidate_filter_json", typeHandler = Fastjson2TypeHandler.class)
  private CandidateFilterPayload candidateFilter;
  /**
   * 详细匹配 payload，放税务、征信、负债、REVIEW 条件等精判规则
   */
  @TableField(value = "match_rule_json", typeHandler = Fastjson2TypeHandler.class)
  private MatchRulePayload matchRule;
  /**
   * 额度策略 payload，放额度公式、策略 key、封顶值等额度测算规则
   */
  @TableField(value = "amount_strategy_json", typeHandler = Fastjson2TypeHandler.class)
  private AmountStrategyPayload amountStrategy;
  /**
   * 提额诊断 payload，放可优化项、动作建议、预期影响等诊断规则
   */
  @TableField(value = "diagnosis_rule_json", typeHandler = Fastjson2TypeHandler.class)
  private DiagnosisRulePayload diagnosisRule;

  /**
   * payload 结构版本号，用于后续 JSON schema 演进
   */
  private Integer payloadSchemaVersion;
  /**
   * 规则来源类型，如 EXCEL/MANUAL/API/LEGACY_MIGRATION
   */
  private String sourceType;
  /**
   * 规则来源定位，如 Excel 行号、接口来源标识、同步批次号
   */
  private String sourceRef;
  /**
   * 备注，仅作人工说明，不参与规则计算
   */
  private String remark;

  /** 创建人 */
  private Long createdBy;
  /** 更新人 */
  private Long updatedBy;
  /** 创建时间 */
  private LocalDateTime createdAt;
  /** 更新时间 */
  private LocalDateTime updatedAt;
}
