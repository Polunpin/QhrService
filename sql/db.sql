-- auto-generated definition
create table yw_product_rule
(
    id                                           bigint unsigned auto_increment comment '主键'
        primary key,
    product_id                                   bigint unsigned                        not null comment '产品ID',
    rule_code              varchar(64)                  not null comment '稳定规则编码',
    rule_version                                 int unsigned default '1'               not null comment '规则版本号',
    rule_name              varchar(128)                 not null comment '规则名称',
    rule_status            varchar(16)  default 'DRAFT' not null comment 'DRAFT/ACTIVE/INACTIVE/ARCHIVED',
    priority               int          default 100     not null comment '优先级，越小越靠前',
    effective_start_time                         datetime                               null comment '生效开始时间',
    effective_end_time                           datetime                               null comment '生效结束时间',
    candidate_filter_json  json                         null comment '候选粗筛规则',
    match_rule_json        json                         null comment '详细匹配规则',
    amount_strategy_json   json                         null comment '额度策略',
    diagnosis_rule_json    json                         null comment '提额诊断规则',
    payload_schema_version int unsigned default '1'     not null comment 'payload结构版本',
    source_type            varchar(32)                  null comment '规则来源类型：EXCEL/MANUAL/API',
    source_ref             varchar(128)                 null comment '来源定位，如excel row',
    remark                                       varchar(500)                           null comment '备注',
    created_by             bigint unsigned              null comment '创建人',
    updated_by             bigint unsigned              null comment '更新人',
    created_at                                   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at                                   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_product_rule_product_version
        unique (product_id, rule_version),
    constraint uk_product_rule_code_version
        unique (rule_code, rule_version),
    constraint fk_product_rule_product
        foreign key (product_id) references jc_products (id)
            on update cascade
)
    comment '产品规则主表';

create index idx_product_rule_query
    on yw_product_rule (product_id, rule_status, effective_start_time, effective_end_time);

create index idx_product_rule_status_priority
    on yw_product_rule (rule_status, priority, effective_start_time, effective_end_time);

-- auto-generated definition
create table yw_custom_service_orders
(
    id                bigint unsigned auto_increment
        primary key,
    enterprise_id     bigint unsigned                                                                                       not null comment '企业ID',
    intention_id      bigint unsigned                                                                                       null comment '关联融资意向ID',
    staff_id          bigint unsigned                                                                                       null comment '负责员工ID',
    current_stage     enum ('方案设计', '孵化中', '风控审核', '授信签约', '放款落实', '贷后监管') default '方案设计'        null comment '当前阶段',
    service_status    enum ('待服务', '服务中', '已完成', '已失败')                               default '待服务'          null comment '状态',
    loan_amount       decimal(15, 2)                                                                                        null comment '最终放款金额',
    commission_amount decimal(15, 2)                                                                                        null comment '佣金金额',
    service_cost      decimal(15, 2)                                                                                        null comment '内部服务成本',
    cost_details      varchar(255)                                                                                          null comment '成本明细',
    settle_status     enum ('pending', 'paid')                                                    default 'pending'         null comment '结算状态',
    last_update_at    datetime                                                                    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    created_at        datetime                                                                    default CURRENT_TIMESTAMP null,
    deleted           tinyint(1)                                                                  default 0                 not null comment '软删除标记：0未删除，1已删除'
)
    comment '非标服务订单表';

create index idx_enterprise_order
    on yw_custom_service_orders (enterprise_id);

-- auto-generated definition
create table jc_users
(
    id         bigint unsigned auto_increment
        primary key,
    openid     varchar(128)                         not null comment '微信openid',
    unionid    varchar(128)                         not null comment '微信unionid',
    real_name  varchar(50)                          null comment '姓名',
    mobile     varchar(11)                          null comment '手机号',
    status     tinyint(1) default 1                 null comment '1:正常, 0:禁用',
    created_at datetime   default CURRENT_TIMESTAMP null,
    updated_at datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint openid
        unique (openid),
    constraint unionid
        unique (unionid)
)
    comment '用户表';

-- auto-generated definition
create table jc_staffs
(
    id             bigint auto_increment
        primary key,
    openid         varchar(64)                                                       null comment '微信openid',
    real_name      varchar(50)                                                       not null comment '真实姓名',
    mobile         varchar(20)                                                       not null comment '手机号',
    role           enum ('超级管理员', '业务员', '风控员') default '业务员'          null comment '角色',
    department     varchar(50)                                                       null comment '部门',
    status         tinyint(1)                              default 1                 null comment '1:正常, 0:禁用',
    last_update_at datetime                                default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    created_at     datetime                                default CURRENT_TIMESTAMP null,
    constraint mobile
        unique (mobile),
    constraint openid
        unique (openid)
)
    comment '内部员工表';

-- auto-generated definition
create table jc_products
(
    id                   bigint unsigned auto_increment
        primary key,
    bank_name            varchar(100)                             not null comment '银行/机构名称',
    product_name         varchar(100)                             not null comment '产品名称',
    product_type         varchar(50)                              null comment '产品种类',
    min_amount           decimal(15, 2) default 0.00              null comment '最低额度(万元)',
    max_amount           decimal(15, 2) default 0.00              null comment '最高额度(万元)',
    interest_rate_range  varchar(50)                              null comment '利率范围(如: 3.5%-5%)',
    loan_term            int                                      null comment '放款期限(月)',
    credit_validity      int                                      null comment '额度有效期(月)',
    disbursement_account varchar(10)                              null comment '放款账户',
    scene                int                                      null comment '是否下户',
    online               int                                      null comment '申请方式(1:线上 2:线下)',
    repayment_method     varchar(50)                              null comment '还款方式',
    status               tinyint        default 2                 null comment '0:下架, 1:上架, 2:草稿',
    created_at           datetime       default CURRENT_TIMESTAMP null,
    updated_at           datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '信贷产品表';

-- auto-generated definition
create table jc_enterprise
(
    id                  bigint unsigned auto_increment
        primary key,
    name                varchar(1000)                        not null comment '企业名称',
    credit_code         varchar(50)                          null comment '统一社会信用代码（查询企业为中国香港企业时，返回商业登记号码）',
    start_date          varchar(50)                          null comment '成立日期',
    oper_name           varchar(1000)                        null comment '法定代表人姓名',
    status              varchar(100)                         null comment '状态',
    address             varchar(1000)                        null comment '注册地址',
    qcc_order_no        varchar(100)                         null comment 'QCC财税订单号',
    qcc_data_status     varchar(50)                          null comment 'QCC财税数据状态：CREATED/WAIT_CAPTCHA/FETCHING/CONSUMED/FAILED/EXPIRED',
    qcc_tax_data        json                                 null comment 'QCC获取到的财税数据JSON',
    qcc_order_expire_at datetime                             null comment 'QCC订单过期时间（下单后7天）',
    created_at          datetime   default CURRENT_TIMESTAMP null,
    updated_at          datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted             tinyint(1) default 0                 not null comment '软删除标记：0未删除，1已删除',
    constraint uk_credit_code
        unique (credit_code)
)
    comment '企业基础信息表';

-- auto-generated definition
create table yw_status_logs
(
    id            bigint unsigned auto_increment
        primary key,
    order_id      bigint unsigned                      not null comment '关联业务单ID',
    operator_type enum ('staff', 'system', 'user')     not null comment '操作人类型',
    operator_id   bigint unsigned                      null comment '操作者ID',
    pre_stage     varchar(50)                          null comment '原阶段',
    post_stage    varchar(50)                          null comment '新阶段',
    remark        text                                 null comment '说明',
    created_at    datetime   default CURRENT_TIMESTAMP null,
    deleted       tinyint(1) default 0                 not null comment '软删除标记：0未删除，1已删除'
)
    comment '业务流转日志表';

-- auto-generated definition
create table zj_user_enterprise_relation
(
    id            bigint unsigned auto_increment
        primary key,
    enterprise_id bigint unsigned                       not null comment '企业ID',
    user_open_id  varchar(128)                          not null comment '用户openId',
    role          varchar(20) default 'owner'           null comment '成员角色: owner-所有者, finance-财务, operator-经办人, viewer-仅查看',
    created_at    datetime    default CURRENT_TIMESTAMP null,
    deleted       tinyint(1)  default 0                 not null comment '软删除标记：0未删除，1已删除',
    constraint uk_user_ent
        unique (user_open_id, enterprise_id)
)
    comment '用户企业关联表';

create index idx_enterprise_id
    on zj_user_enterprise_relation (enterprise_id);

-- auto-generated definition
create table yw_match_records
(
    id             bigint unsigned auto_increment
        primary key,
    user_open_id   varchar(128)                         not null comment '用户openId',
    enterprise_id  bigint unsigned                      not null comment '企业ID',
    intention_id   bigint unsigned                      null comment '关联的融资意向ID',
    amount_range   varchar(15)                          not null comment '额度区间',
    product_ids    json                                 not null comment '明确命中的产品ID数组，仅 MATCH',
    review_reasons json                                 null comment '可做但需补件/人工确认的结构化原因',
    reject_reasons json                                 null comment '拒绝和数据缺失原因，用于回溯',
    created_at     datetime   default CURRENT_TIMESTAMP null,
    deleted        tinyint(1) default 0                 not null comment '软删除标记：0未删除，1已删除'
)
    comment '匹配记录表';

create index idx_enterprise
    on yw_match_records (enterprise_id);

-- auto-generated definition
create table yw_financing_intentions
(
    id                         bigint unsigned auto_increment
        primary key,
    user_open_id               varchar(128)                         not null comment '用户openId',
    enterprise_id              bigint unsigned                      not null comment '企业ID',
    amount_range               varchar(10)                          null comment '贷款金额(万 区间)',
    personal_credit_name       varchar(255)                         null comment '个人征信名称',
    personal_credit_cloud_id   varchar(128)                         null comment '个人征信cloudId',
    enterprise_credit_name     varchar(255)                         null comment '企业征信名称',
    enterprise_credit_cloud_id varchar(128)                         null comment '企业征信cloudId',
    tax_account                varchar(128)                         null comment '税务账号',
    tax_password               varchar(128)                         null comment '税务密码',
    created_at                 datetime   default CURRENT_TIMESTAMP null,
    updated_at                 datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted                    tinyint(1) default 0                 not null comment '软删除标记：0未删除，1已删除'
)
    comment '融资需求表';

create index idx_financing_intention_enterprise_id
    on yw_financing_intentions (enterprise_id);
