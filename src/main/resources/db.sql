-- 1. 用户表 (小程序前端用户)
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

-- 2. 企业基础表
create table jc_enterprise_basic_info
(
    id           bigint unsigned auto_increment
        primary key,
    name         varchar(1000)                       not null comment '企业名称',
    credit_code  varchar(50)                        null comment '统一社会信用代码（查询企业为中国香港企业时，返回商业登记号码）',
    start_date   varchar(50)                        null comment '成立日期',
    oper_name    varchar(1000)                      null comment '法定代表人姓名',
    status       varchar(100)                       null comment '状态',
    address      varchar(1000)                      null comment '注册地址',
    created_at   datetime default CURRENT_TIMESTAMP null,
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    unique key uk_credit_code (credit_code)
)
    engine = InnoDB
    default charset = utf8mb4
    comment = '企业基础信息表';





-- 3. 用户-企业关联表 (解耦多对多关系)
create table zj_user_enterprise_relation
(
    id            bigint unsigned auto_increment
        primary key,
    enterprise_id bigint unsigned                       not null comment '企业ID',
    user_open_id  varchar(128)                          not null comment '用户openId',
    role          varchar(20) default 'owner'           null comment '成员角色: owner-所有者, finance-财务, operator-经办人, viewer-仅查看',
    created_at    datetime    default CURRENT_TIMESTAMP null,
    constraint uk_user_ent
        unique (user_open_id, enterprise_id)
)
    comment '用户企业关联表';

create index idx_enterprise_id
    on zj_user_enterprise_relation (enterprise_id);


-- 4. 融资需求表
create table yw_financing_intentions
(
    id                bigint unsigned auto_increment
        primary key,
    enterprise_id     bigint unsigned                    not null comment '企业ID',
    amount_range       varchar(10)                     not null comment '贷款金额(万)',
    property          boolean  default false             not null comment '是否有房产',
    property_mortgage boolean  default false             not null comment '是否提供房产抵押',
    spouse_support   boolean  default false             not null comment '是否配偶共担',
    tax_account        varchar(128)                       null comment '工商帐号',
    tax_password       varchar(128)                       null comment '工商密码',
    created_at        datetime default CURRENT_TIMESTAMP null,
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '融资需求表';

create index idx_financing_intention_enterprise_id
    on yw_financing_intentions (enterprise_id);


-- 5. 信贷产品表
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

-- 6. 匹配记录表
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
    created_at    datetime                        default CURRENT_TIMESTAMP null
)
    comment '匹配记录表';

create index idx_enterprise
    on z_match_records (enterprise_id);


-- 7. 非标服务订单表
create table yw_custom_service_orders
(
    id                bigint unsigned auto_increment
        primary key,
    enterprise_id     bigint unsigned                                                                                       not null comment '企业ID',
    intention_id      bigint unsigned                                                                                       null comment '关联融资意向ID',
    staff_id          bigint unsigned                                                                                       null comment '负责员工ID',
    current_stage     enum ('方案设计', '孵化中', '风控审核', '授信签约', '放款落实', '贷后监管') default '方案设计'        null comment '当前阶段',
    service_status    enum ('待服务', '服务中', '已完成', '已失败')                               default '待服务'          null comment '状态',
    amount_range       decimal(15, 2)                                                                                        null comment '最终放款金额',
    commission_amount decimal(15, 2)                                                                                        null comment '佣金金额',
    service_cost      decimal(15, 2)                                                                                        null comment '内部服务成本',
    cost_details      varchar(255)                                                                                          null comment '成本明细',
    settle_status     enum ('pending', 'paid')                                                    default 'pending'         null comment '结算状态',
    last_update_at    datetime                                                                    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    created_at        datetime                                                                    default CURRENT_TIMESTAMP null
)
    comment '非标服务订单表';

create index idx_enterprise_order
    on yw_custom_service_orders (enterprise_id);


-- 8. 业务流转日志表
create table yw_status_logs
(
    id            bigint unsigned auto_increment
        primary key,
    order_id      bigint unsigned                    not null comment '关联业务单ID',
    operator_type enum ('staff', 'system', 'user')   not null comment '操作人类型',
    operator_id   bigint unsigned                    null comment '操作者ID',
    pre_stage     varchar(50)                        null comment '原阶段',
    post_stage    varchar(50)                        null comment '新阶段',
    remark        text                               null comment '说明',
    created_at    datetime default CURRENT_TIMESTAMP null
)
    comment '业务流转日志表';


-- 9. 内部员工表
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
