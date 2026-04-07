-- auto-generated definition
create table yw_product_rule
(
    id                                           bigint unsigned auto_increment comment '主键'
        primary key,
    product_id                                   bigint unsigned                        not null comment '产品ID',
    rule_version                                 int unsigned default '1'               not null comment '规则版本号',
    rule_name                                    varchar(128) default 'default'         not null comment '规则名称',
    is_active                                    tinyint(1)   default 1                 not null comment '是否生效',
    effective_start_time                         datetime                               null comment '生效开始时间',
    effective_end_time                           datetime                               null comment '生效结束时间',
    remark                                       varchar(500)                           null comment '备注',
    gs_min_establish_months                      int unsigned                           null comment '工商-最低成立月数',
    gs_min_registered_capital                    decimal(18, 2)                         null comment '工商-最低注册资本',
    gs_min_paid_in_capital                       decimal(18, 2)                         null comment '工商-最低实缴资本',
    gs_max_legal_rep_change_count_2y             int unsigned                           null comment '工商-近2年法定代表人变更次数上限',
    gs_min_legal_rep_change_gap_months           int unsigned                           null comment '工商-法人最近一次变更距今最少月数',
    gs_min_legal_rep_share_ratio                 decimal(5, 2)                          null comment '工商-法人最低持股比例，按百分比存',
    gs_allow_legal_rep_no_joint_liability        tinyint(1)                             null comment '工商-是否允许法人不连带，1允许 0不允许 NULL不限制',
    gs_allow_shareholder_replace_joint_liability tinyint(1)                             null comment '工商-是否允许股东代替法人连带，1允许 0不允许 NULL不限制',
    gs_min_shareholder_replace_share_ratio       decimal(5, 2)                          null comment '工商-股东代替连带时最低持股比例，按百分比存',
    tax_min_tax_months_12m                       tinyint unsigned                       null comment '税务-近12个月最少纳税月份数',
    tax_min_tax_months_24m                       tinyint unsigned                       null comment '税务-近24个月最少纳税月份数',
    tax_max_zero_declare_streak                  tinyint unsigned                       null comment '税务-连续0申报次数上限',
    tax_min_tax_amount_12m                       decimal(18, 2)                         null comment '税务-近12个月最低纳税金额',
    tax_min_tax_amount_ytd                       decimal(18, 2)                         null comment '税务-当年最低纳税金额',
    tax_min_tax_amount_last_year                 decimal(18, 2)                         null comment '税务-上一完整自然年最低纳税金额',
    tax_max_tax_burden_ratio                     decimal(8, 4)                          null comment '税务-税负率上限，按百分比存',
    tax_min_tax_burden_ratio_yoy                 decimal(8, 4)                          null comment '税务-当年税负率/上年税负率比值下限',
    tax_min_total_declares                       int unsigned                           null comment '税务-累计纳税申报次数下限',
    jud_reject_if_major_lawsuit                  tinyint(1)                             null comment '司法-是否因重大诉讼拒绝',
    jud_reject_if_executed                       tinyint(1)                             null comment '司法-是否因被执行拒绝',
    jud_reject_if_dishonest_person               tinyint(1)                             null comment '司法-是否因失信被执行拒绝',
    jud_reject_if_equity_frozen                  tinyint(1)                             null comment '司法-是否因股权冻结拒绝',
    jud_max_execution_count_24m                  int unsigned                           null comment '司法-近24个月被执行次数上限',
    jud_max_court_announcement_count_12m         int unsigned                           null comment '司法-近12个月法院公告次数上限',
    jud_max_admin_penalty_count_12m              int unsigned                           null comment '司法-近12个月行政处罚次数上限',
    jud_reject_if_major_judicial_risk            tinyint(1)                             null comment '司法-是否因重大司法风险拒绝',
    jud_reject_if_restriction_high_consumption   tinyint(1)                             null comment '司法-是否因限制高消费拒绝',
    ind_match_mode_code                          varchar(16)                            null comment '行业风险-行业匹配模式，ALLOW/BLOCK',
    ind_max_risk_level                           tinyint unsigned                       null comment '行业风险-行业风险等级上限，1低-5高',
    ind_require_enterprise_tag                   tinyint(1)                             null comment '行业风险-是否要求企业标签',
    ind_min_tag_valid_months                     int unsigned                           null comment '行业风险-标签最少剩余有效月数',
    ind_require_region_match                     tinyint(1)                             null comment '行业风险-是否要求地区匹配',
    ind_require_actual_business_address          tinyint(1)                             null comment '行业风险-是否要求经营地可核验',
    ind_reject_if_high_pollution                 tinyint(1)                             null comment '行业风险-是否拒绝高污染行业',
    ind_reject_if_high_energy_consumption        tinyint(1)                             null comment '行业风险-是否拒绝高耗能行业',
    ind_reject_if_sensitive_industry             tinyint(1)                             null comment '行业风险-是否拒绝敏感行业',
    ec_max_total_liability                       decimal(18, 2)                         null comment '企业征信-企业总负债上限',
    ec_max_credit_liability                      decimal(18, 2)                         null comment '企业征信-企业信贷负债上限',
    ec_max_mortgage_liability                    decimal(18, 2)                         null comment '企业征信-企业抵押负债上限',
    ec_max_external_guarantee_amount             decimal(18, 2)                         null comment '企业征信-企业对外担保余额上限',
    ec_max_loan_org_count                        int unsigned                           null comment '企业征信-企业贷款机构数上限',
    ec_max_query_count_6m                        int unsigned                           null comment '企业征信-近6个月企业征信查询次数上限',
    ec_max_overdue_count_12m                     int unsigned                           null comment '企业征信-近12个月逾期次数上限',
    ec_max_overdue_months_24m                    int unsigned                           null comment '企业征信-近24个月最大逾期月数上限',
    ec_min_no_overdue_days                       int unsigned                           null comment '企业征信-最短无逾期天数下限',
    ec_allow_abnormal_credit_account             tinyint(1)                             null comment '企业征信-是否允许征信账户状态异常',
    pc_max_query_count_1m                        int unsigned                           null comment '个人征信-近1个月查询次数上限',
    pc_max_query_count_3m                        int unsigned                           null comment '个人征信-近3个月查询次数上限',
    pc_max_query_count_6m                        int unsigned                           null comment '个人征信-近6个月查询次数上限',
    pc_max_loan_query_count_1m                   int unsigned                           null comment '个人征信-近1个月贷款审批查询次数上限',
    pc_max_credit_card_query_count_1m            int unsigned                           null comment '个人征信-近1个月信用卡审批查询次数上限',
    pc_max_overdue_count_12m                     int unsigned                           null comment '个人征信-近12个月逾期次数上限',
    pc_max_overdue_months_24m                    int unsigned                           null comment '个人征信-近24个月最大逾期月数上限',
    pc_max_total_overdue_terms_5y                int unsigned                           null comment '个人征信-近5年累计逾期期数上限',
    pc_max_non_bank_loan_count                   int unsigned                           null comment '个人征信-非银行网贷/小贷笔数上限',
    pc_max_credit_card_utilization               decimal(5, 2)                          null comment '个人征信-信用卡使用率上限，按百分比存',
    ext_json                                     json                                   null comment '低频个性化扩展字段，不建议通用DMN直接依赖',
    created_at                                   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at                                   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_product_rule_version
        unique (product_id, rule_version),
    constraint fk_product_rule_product
        foreign key (product_id) references jc_products (id)
            on update cascade
)
    comment '产品规则主表';

create index idx_product_rule_active
    on yw_product_rule (is_active);

create index idx_product_rule_match_mode
    on yw_product_rule (ind_match_mode_code);

create index idx_product_rule_query
    on yw_product_rule (product_id, is_active, effective_start_time, effective_end_time);

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
    id          bigint unsigned auto_increment
        primary key,
    name        varchar(1000)                        not null comment '企业名称',
    credit_code varchar(50)                          null comment '统一社会信用代码（查询企业为中国香港企业时，返回商业登记号码）',
    start_date  varchar(50)                          null comment '成立日期',
    oper_name   varchar(1000)                        null comment '法定代表人姓名',
    status      varchar(100)                         null comment '状态',
    address     varchar(1000)                        null comment '注册地址',
    created_at  datetime   default CURRENT_TIMESTAMP null,
    updated_at  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted     tinyint(1) default 0                 not null comment '软删除标记：0未删除，1已删除',
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
create table z_match_records
(
    id            bigint unsigned auto_increment
        primary key,
    user_open_id  varchar(128)                                              not null comment '用户openId',
    enterprise_id bigint unsigned                                           not null comment '企业ID',
    intention_id  bigint unsigned                                           null comment '关联的融资意向ID',
    product_ids   json                                                      not null comment '匹配到的产品ID数组',
    match_score   decimal(5, 2)                                             null comment '匹配度分数',
    risk_type     varchar(100)                                              null comment '风险类型',
    risk_level    enum ('提示', '警告', '严重')   default '提示'            null comment '风险级别',
    status        enum ('成功', '失败', '待人工') default '待人工'          null comment '匹配状态',
    created_at    datetime                        default CURRENT_TIMESTAMP null,
    deleted       tinyint(1)                      default 0                 not null comment '软删除标记：0未删除，1已删除'
)
    comment '匹配记录表';

create index idx_enterprise
    on z_match_records (enterprise_id);

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
