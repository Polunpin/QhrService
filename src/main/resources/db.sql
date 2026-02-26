-- 1. 用户表 (小程序前端用户)
CREATE TABLE users
(
    id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    openid     VARCHAR(64) UNIQUE NOT NULL COMMENT '微信openid',
    unionid    VARCHAR(64) UNIQUE COMMENT '微信unionid',
    mobile     VARCHAR(20)        NOT NULL COMMENT '手机号',
    real_name  VARCHAR(50) COMMENT '姓名',
    status     TINYINT(1) DEFAULT 1 COMMENT '1:正常, 0:禁用',
    created_at DATETIME   DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 2. 企业主表
CREATE TABLE enterprises
(
    id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    full_name             VARCHAR(255)    NOT NULL COMMENT '企业全称',
    credit_code           CHAR(18) UNIQUE NOT NULL COMMENT '统一社会信用代码',
    industry              VARCHAR(100) COMMENT '所属行业',
    tax_rating            CHAR(1) COMMENT '纳税评级(A/B/M/C/D)',
    region_code           VARCHAR(50) COMMENT '地区编码',
    annual_turnover       DECIMAL(15, 2) COMMENT '年营业额(万元)',
    annual_tax_amount     DECIMAL(15, 2) COMMENT '年纳税额(万元)',
    existing_loan_balance DECIMAL(15, 2) COMMENT '现有贷款余额(万元)',
    match_status          ENUM ('待完善', '已匹配', '跟进中', '匹配失败') DEFAULT '待完善' COMMENT '匹配状态',
    profile_data          JSON COMMENT '画像详情(纳税、社保等)',
    created_at            DATETIME                                        DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='企业信息表';

-- 3. 用户-企业关联表 (解耦多对多关系)
CREATE TABLE user_enterprise_relation
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT UNSIGNED NOT NULL COMMENT '企业ID',
    user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role          VARCHAR(20) DEFAULT 'owner' COMMENT '成员角色: owner-所有者, finance-财务, operator-经办人, viewer-仅查看',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_ent (user_id, enterprise_id),
    INDEX idx_enterprise_id (enterprise_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户企业关联表';

-- 4. 信贷产品表
CREATE TABLE credit_products
(
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    bank_name           VARCHAR(100) NOT NULL COMMENT '银行/机构名称',
    product_name        VARCHAR(100) NOT NULL COMMENT '产品名称',
    product_type        VARCHAR(50) COMMENT '产品种类',
    min_amount          DECIMAL(15, 2) DEFAULT 0 COMMENT '最低额度(万元)',
    max_amount          DECIMAL(15, 2) DEFAULT 0 COMMENT '最高额度(万元)',
    interest_rate_range VARCHAR(50) COMMENT '利率范围(如: 3.5%-5%)',
    loan_term           INT COMMENT '期限(月)',
    repayment_method    VARCHAR(50) COMMENT '还款方式',
    region        VARCHAR(255) COMMENT '准入地区',
    criteria_json       JSON COMMENT '准入条件结构化描述',
    status              TINYINT        DEFAULT 2 COMMENT '0:下架, 1:上架, 2:草稿',
    success_rate        DECIMAL(5, 2)  DEFAULT 0.00 COMMENT '历史匹配成功率',
    created_at          DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='信贷产品表';

-- 5. 融资申请意向表
CREATE TABLE financing_intentions
(
    id                BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    application_no    VARCHAR(32) UNIQUE                                            NOT NULL COMMENT '申请编号',
    enterprise_id     BIGINT UNSIGNED                                               NOT NULL COMMENT '企业ID',
    user_id           BIGINT UNSIGNED                                               NOT NULL COMMENT '用户ID',
    expected_amount   DECIMAL(15, 2)                                                NOT NULL COMMENT '期望融资金额(万元)',
    expected_term     INT                                                           NOT NULL COMMENT '期望期限(月)',
    purpose           ENUM ('流动资金', '进货', '购房', '装修', '资产购置', '其他') NOT NULL COMMENT '融资用途',
    repayment_source  VARCHAR(255) COMMENT '还款来源说明',
    guarantee_type    ENUM ('信用', '抵押', '质押', '保证人', '其他')               NOT NULL COMMENT '期望担保方式',
    target_product_id BIGINT UNSIGNED                                                   DEFAULT NULL COMMENT '目标产品ID(若为空则为意向匹配)',
    contact_mobile    VARCHAR(20) COMMENT '申请联系电话',
    status            ENUM ('待处理', '审核中', '已对接', '已放款', '已拒绝', '已取消') DEFAULT '待处理' COMMENT '申请状态',
    refusal_reason    VARCHAR(255) COMMENT '拒绝/取消原因',
    urgency_level     TINYINT                                                           DEFAULT 2 COMMENT '紧急程度 1低 2中 3高',
    created_at        DATETIME                                                          DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME                                                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_enterprise (enterprise_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='融资申请意向表';

-- 6. 匹配记录表
CREATE TABLE match_records
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    match_no      VARCHAR(32) UNIQUE NOT NULL COMMENT '匹配编号',
    enterprise_id BIGINT UNSIGNED    NOT NULL COMMENT '企业ID',
    intention_id  BIGINT UNSIGNED COMMENT '关联的融资意向ID',
    product_ids   JSON               NOT NULL COMMENT '匹配到的产品ID数组',
    match_score   DECIMAL(5, 2) COMMENT '匹配度分数',
    risk_type     VARCHAR(100) COMMENT '风险类型',
    risk_level    ENUM ('提示', '警告', '严重')   DEFAULT '提示' COMMENT '风险级别',
    status        ENUM ('成功', '失败', '待人工') DEFAULT '待人工' COMMENT '匹配状态',
    created_at    DATETIME                        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise (enterprise_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='匹配记录表';

-- 7. 非标服务订单表
CREATE TABLE custom_service_orders
(
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    enterprise_id     BIGINT UNSIGNED NOT NULL COMMENT '企业ID',
    intention_id      BIGINT UNSIGNED COMMENT '关联融资意向ID',
    staff_id          BIGINT UNSIGNED COMMENT '负责员工ID',
    current_stage     ENUM ('方案设计', '孵化中', '风控审核', '授信签约', '放款落实', '贷后监管') DEFAULT '方案设计' COMMENT '当前阶段',
    service_status    ENUM ('待服务', '服务中', '已完成', '已失败')                               DEFAULT '待服务' COMMENT '状态',
    loan_amount       DECIMAL(15, 2) COMMENT '最终放款金额',
    commission_amount DECIMAL(15, 2) COMMENT '佣金金额',
    service_cost      DECIMAL(15, 2) COMMENT '内部服务成本',
    cost_details      VARCHAR(255) COMMENT '成本明细',
    settle_status     ENUM ('pending', 'paid')                                                    DEFAULT 'pending' COMMENT '结算状态',
    last_update_at    DATETIME                                                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at        DATETIME                                                                    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enterprise_order (enterprise_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='非标服务订单表';

-- 8. 业务流转日志表
CREATE TABLE status_logs
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT UNSIGNED                  NOT NULL COMMENT '关联业务单ID',
    operator_type ENUM ('staff', 'system', 'user') NOT NULL COMMENT '操作人类型',
    operator_id   BIGINT UNSIGNED COMMENT '操作者ID',
    pre_stage     VARCHAR(50) COMMENT '原阶段',
    post_stage    VARCHAR(50) COMMENT '新阶段',
    remark        TEXT COMMENT '说明',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务流转日志表';

-- 9. 内部员工表
CREATE TABLE staffs
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    openid         VARCHAR(64) UNIQUE COMMENT '微信openid',
    real_name      VARCHAR(50)        NOT NULL COMMENT '真实姓名',
    mobile         VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
    role           ENUM ('超级管理员', '业务员', '风控员') DEFAULT '业务员' COMMENT '角色',
    department     VARCHAR(50) COMMENT '部门',
    status         TINYINT(1)                              DEFAULT 1 COMMENT '1:正常, 0:禁用',
    last_update_at DATETIME                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at     DATETIME                                DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='内部员工表';
-- 10. 信贷产品申请路径配置表 TODO 后期业务优化
CREATE TABLE Product_Redirect_Config
(
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT NOT NULL COMMENT '关联信贷产品ID',
    config_type      VARCHAR(20)       DEFAULT 'GENERAL' COMMENT '配置类型：GENERAL-通用, PERSON-人员',
    external_user_id VARCHAR(128)      DEFAULT NULL COMMENT '承接对象ID（如企微UserID）',
    target_name      VARCHAR(100)  NOT NULL COMMENT '承接对象姓名',
    redirect_url     VARCHAR(1024) NOT NULL COMMENT '跳转链接',
    is_active        TINYINT(1)        DEFAULT 1 COMMENT '启用状态：1-启用，0-停用',
    click_count      INT               DEFAULT 0 COMMENT '累计点击次数',
    priority         INT               DEFAULT 0 COMMENT '排序优先级',
    last_update_at   DATETIME          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at       DATETIME          DEFAULT CURRENT_TIMESTAMP,
    -- 核心索引：用于看板统计和详情页查询
    INDEX idx_prod_active (product_id, is_active),
    INDEX idx_external_user (external_user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='信贷产品申请路径配置表';
